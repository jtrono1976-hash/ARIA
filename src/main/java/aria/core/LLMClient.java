package aria.core;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.google.gson.*;
import okhttp3.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LLMClient {

    private final ConfigStore config;
    private final OkHttpClient httpClient;

    public LLMClient(ConfigStore config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public String sendMessage(String systemPrompt, List<ConversationHistory.Message> history, String userMessage, boolean includeHistory) {
        String preferred = config.get("LLM_PROVIDER", "groq").toLowerCase();

        List<String> order = new ArrayList<>();
        order.add(preferred);
        for (String p : new String[]{"groq", "claude", "openai"}) {
            if (!p.equals(preferred)) order.add(p);
        }

        List<String> tried = new ArrayList<>();
        for (String provider : order) {
            if (!hasKey(provider)) continue;
            try {
                switch (provider) {
                    case "claude": return sendClaude(systemPrompt, history, userMessage, includeHistory);
                    case "groq":   return sendGroq(systemPrompt, history, userMessage, includeHistory);
                    case "openai": return sendOpenAI(systemPrompt, history, userMessage, includeHistory);
                }
            } catch (Exception e) {
                System.err.println(provider + " failed: " + e.getMessage());
                tried.add(provider + " (" + e.getMessage() + ")");
            }
        }

        if (tried.isEmpty()) {
            return "no API keys configured — open Settings and add a Groq, Anthropic, or OpenAI key.";
        }
        return "all APIs failed — " + String.join(", then ", tried) + ". Check your keys in Settings.";
    }

    private boolean hasKey(String provider) {
        String key = switch (provider) {
            case "claude" -> config.get("ANTHROPIC_API_KEY", "");
            case "groq"   -> config.get("GROQ_API_KEY", "");
            case "openai" -> config.get("OPENAI_API_KEY", "");
            default -> "";
        };
        return !key.isEmpty() && !key.equals("your_key_here");
    }

    private String sendClaude(String systemPrompt, List<ConversationHistory.Message> history, String userMessage, boolean includeHistory) throws Exception {
        String apiKey = config.get("ANTHROPIC_API_KEY", "");
        if (apiKey.isEmpty() || apiKey.equals("your_key_here")) {
            throw new Exception("No Anthropic API key configured. Go to Settings to add one.");
        }

        AnthropicClient client = AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .build();

        String model = config.get("CLAUDE_MODEL", "claude-haiku-4-5-20251001");
        int maxTokens = Integer.parseInt(config.get("MAX_TOKENS", "1000"));

        List<MessageParam> messages = new ArrayList<>();
        if (includeHistory && history != null) {
            for (ConversationHistory.Message msg : history) {
                if ("user".equals(msg.role)) {
                    messages.add(MessageParam.builder().role(MessageParam.Role.USER).content(msg.content).build());
                } else if ("assistant".equals(msg.role)) {
                    messages.add(MessageParam.builder().role(MessageParam.Role.ASSISTANT).content(msg.content).build());
                }
            }
        }
        messages.add(MessageParam.builder().role(MessageParam.Role.USER).content(userMessage).build());

        Message response = client.messages().create(
            MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(systemPrompt)
                .messages(messages)
                .build()
        );

        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : response.content()) {
            block.text().ifPresent(textBlock -> sb.append(textBlock.text()));
        }
        return sb.toString().trim();
    }

    private String sendGroq(String systemPrompt, List<ConversationHistory.Message> history, String userMessage, boolean includeHistory) throws Exception {
        String apiKey = config.get("GROQ_API_KEY", "");
        if (apiKey.isEmpty() || apiKey.equals("your_key_here")) {
            throw new Exception("No Groq API key configured. Go to Settings to add one.");
        }
        String model = config.get("GROQ_MODEL", "llama-3.3-70b-versatile");
        return sendOpenAICompatible(
            "https://api.groq.com/openai/v1/chat/completions",
            apiKey, model, systemPrompt, history, userMessage, includeHistory
        );
    }

    private String sendOpenAI(String systemPrompt, List<ConversationHistory.Message> history, String userMessage, boolean includeHistory) throws Exception {
        String apiKey = config.get("OPENAI_API_KEY", "");
        if (apiKey.isEmpty() || apiKey.equals("your_key_here")) {
            throw new Exception("No OpenAI API key configured. Go to Settings to add one.");
        }
        String model = config.get("OPENAI_MODEL", "gpt-4o-mini");
        return sendOpenAICompatible(
            "https://api.openai.com/v1/chat/completions",
            apiKey, model, systemPrompt, history, userMessage, includeHistory
        );
    }

    private String sendOpenAICompatible(String url, String apiKey, String model,
                                         String systemPrompt, List<ConversationHistory.Message> history,
                                         String userMessage, boolean includeHistory) throws Exception {
        int maxTokens = Integer.parseInt(config.get("MAX_TOKENS", "1000"));

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        if (includeHistory && history != null) {
            for (ConversationHistory.Message msg : history) {
                JsonObject m = new JsonObject();
                m.addProperty("role", msg.role);
                m.addProperty("content", msg.content);
                messages.add(m);
            }
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("max_tokens", maxTokens);

        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new Exception("API error " + response.code() + ": " + errorBody);
            }
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString().trim();
        }
    }
}
