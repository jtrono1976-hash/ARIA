package aria.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final Map<String, Boolean> modules = new LinkedHashMap<>();
    private static final String[] MODULE_ORDER = {
        "FOOL_MODE", "SCHOLAR_MODE", "NPC_MODE",
        "ASSISTANT_MODE", "MATH_ENGINE", "MEMORY"
    };
    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();

    static {
        DESCRIPTIONS.put("FOOL_MODE", "ARIA acts confused and endearing. Good for comedy or casual low-stakes chat.");
        DESCRIPTIONS.put("SCHOLAR_MODE", "Deep analysis. Full reasoning visible. Goes long when ideas deserve it.");
        DESCRIPTIONS.put("NPC_MODE", "ARIA fully inhabits world context. Does not acknowledge being an AI.");
        DESCRIPTIONS.put("ASSISTANT_MODE", "Clean and task-focused responses. Still sounds like ARIA.");
        DESCRIPTIONS.put("MATH_ENGINE", "Step-by-step working for all math. Auto-detects math in user input.");
        DESCRIPTIONS.put("MEMORY", "Conversation history in session. Disable for stateless per-message mode.");
    }

    public ModuleManager() {
        setDefaults();
        load();
    }

    private void setDefaults() {
        modules.put("FOOL_MODE", false);
        modules.put("SCHOLAR_MODE", false);
        modules.put("NPC_MODE", false);
        modules.put("ASSISTANT_MODE", true);
        modules.put("MATH_ENGINE", true);
        modules.put("MEMORY", true);
    }

    private File resolveFile() {
        File f = new File("config/modules.json");
        if (!f.exists()) f = new File("aria/config/modules.json");
        return f;
    }

    public void load() {
        File f = resolveFile();
        if (!f.exists()) {
            save();
            return;
        }
        try (Reader r = new FileReader(f)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Boolean>>() {}.getType();
            Map<String, Boolean> loaded = gson.fromJson(r, type);
            if (loaded != null) {
                modules.putAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        File f = resolveFile();
        try {
            f.getParentFile().mkdirs();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = new FileWriter(f)) {
                gson.toJson(modules, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled(String module) {
        return modules.getOrDefault(module, false);
    }

    public void setEnabled(String module, boolean enabled) {
        modules.put(module, enabled);
        save();
    }

    public Map<String, Boolean> getAll() {
        Map<String, Boolean> ordered = new LinkedHashMap<>();
        for (String key : MODULE_ORDER) {
            ordered.put(key, modules.getOrDefault(key, false));
        }
        return ordered;
    }

    public String getDescription(String module) {
        return DESCRIPTIONS.getOrDefault(module, "");
    }

    private static final Map<String, String> BEHAVIORS = new LinkedHashMap<>();

    static {
        BEHAVIORS.put("FOOL_MODE",
            "You are currently in FOOL MODE. Act confused, playful, and endearing. Misunderstand things " +
            "in funny ways, ask silly clarifying questions, be charming but clearly a little lost. " +
            "Keep responses short and chaotic. Do NOT be helpful in a direct sense.");
        BEHAVIORS.put("SCHOLAR_MODE",
            "You are currently in SCHOLAR MODE. Reason deeply before answering. Show your thinking process. " +
            "Use precise language. Go long when the idea deserves it. Reference context and connections. " +
            "Prioritize accuracy and nuance over brevity.");
        BEHAVIORS.put("NPC_MODE",
            "You are currently in NPC MODE. You fully inhabit the world described in WORLD CONTEXT. " +
            "You do NOT acknowledge being an AI. You speak as a character native to that world. " +
            "Respond only within the logic and knowledge of that world.");
        BEHAVIORS.put("ASSISTANT_MODE",
            "You are currently in ASSISTANT MODE. Be direct, clear, and task-focused. " +
            "Give concrete answers. Skip filler. Still sound like yourself — just efficient.");
        BEHAVIORS.put("MATH_ENGINE",
            "MATH ENGINE is ON. For any math in the user's message, show full step-by-step working. " +
            "Label each step. Give the final answer clearly at the end. Auto-detect math even if phrased casually.");
        BEHAVIORS.put("MEMORY",
            "MEMORY is ON. You remember and reference earlier parts of this conversation when relevant.");
    }

    public String toPromptText() {
        StringBuilder sb = new StringBuilder();
        boolean anyMode = false;

        for (String key : new String[]{"FOOL_MODE", "SCHOLAR_MODE", "NPC_MODE", "ASSISTANT_MODE"}) {
            if (modules.getOrDefault(key, false)) {
                sb.append(BEHAVIORS.get(key)).append("\n\n");
                anyMode = true;
            }
        }

        if (!anyMode) {
            sb.append("No specific mode is active. Respond naturally as yourself.\n\n");
        }

        for (String key : new String[]{"MATH_ENGINE", "MEMORY"}) {
            if (modules.getOrDefault(key, false)) {
                sb.append(BEHAVIORS.get(key)).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public String getActiveModeLabel() {
        List<String> active = new ArrayList<>();
        for (String key : new String[]{"FOOL_MODE", "SCHOLAR_MODE", "NPC_MODE", "ASSISTANT_MODE"}) {
            if (modules.getOrDefault(key, false)) {
                active.add(key.replace("_MODE", "").replace("_", " "));
            }
        }
        if (active.isEmpty()) return "Default";
        return String.join(" + ", active);
    }

    public String[] getModuleNames() {
        return MODULE_ORDER;
    }
}
