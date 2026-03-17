package aria.core;

import com.google.gson.JsonObject;

import java.io.File;

public class AriaCore {

    private final ContextManager contextManager;
    private final ModuleManager moduleManager;
    private final ConversationHistory conversationHistory;
    private final LLMClient llmClient;
    private final AIBaseExporter aiBaseExporter;
    private final ConfigStore configStore;

    public AriaCore() {
        this.configStore = new ConfigStore();
        this.contextManager = new ContextManager();
        this.moduleManager = new ModuleManager();
        this.conversationHistory = new ConversationHistory(
            Boolean.parseBoolean(getConfig("PERSIST_HISTORY", "false"))
        );
        this.llmClient = new LLMClient(configStore);
        this.aiBaseExporter = new AIBaseExporter(contextManager, moduleManager);
    }

    public boolean needsSetup() {
        JsonObject ctx = contextManager.getWorldContext();
        return ctx == null || ctx.size() == 0 || !ctx.has("world_name");
    }

    public String chat(String userMessage) {
        String systemPrompt = buildSystemPrompt();
        if (moduleManager.isEnabled("MEMORY")) {
            conversationHistory.addUser(userMessage);
        }
        String response = llmClient.sendMessage(
            systemPrompt,
            conversationHistory.getHistory(),
            userMessage,
            moduleManager.isEnabled("MEMORY")
        );
        if (moduleManager.isEnabled("MEMORY")) {
            conversationHistory.addAssistant(response);
        }
        return response;
    }

    public String buildSystemPrompt() {
        String basePrompt = readPromptFile();
        String modulesText = moduleManager.toPromptText();
        String contextText = contextManager.toPromptText();
        return basePrompt
            .replace("{MODULES_INJECT_HERE}", modulesText)
            .replace("{WORLD_CONTEXT_INJECT_HERE}", contextText);
    }

    private String readPromptFile() {
        try {
            File f = new File("prompts/aria_system_prompt.txt");
            if (!f.exists()) f = new File("aria/prompts/aria_system_prompt.txt");
            if (f.exists()) {
                return new String(java.nio.file.Files.readAllBytes(f.toPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getDefaultPrompt();
    }

    private String getDefaultPrompt() {
        return "YOU ARE ARIA.\n\nYou are a personal AI companion raised by Natt. " +
               "Talk naturally. Be direct. No bullet points in conversation.\n\n" +
               "ACTIVE MODULES\n{MODULES_INJECT_HERE}\n\n" +
               "WORLD CONTEXT\n{WORLD_CONTEXT_INJECT_HERE}";
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConversationHistory getConversationHistory() {
        return conversationHistory;
    }

    public LLMClient getLLMClient() {
        return llmClient;
    }

    public AIBaseExporter getAIBaseExporter() {
        return aiBaseExporter;
    }

    public ConfigStore getConfigStore() {
        return configStore;
    }

    public String getConfig(String key, String defaultValue) {
        return configStore.get(key, defaultValue);
    }

    public void setConfig(String key, String value) {
        configStore.set(key, value);
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public void shutdown() {
        if (Boolean.parseBoolean(getConfig("PERSIST_HISTORY", "false"))) {
            conversationHistory.saveToFile();
        }
    }
}
