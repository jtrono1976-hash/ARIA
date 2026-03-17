# ARIA — Adaptive Reasoning and Intelligence Assistant

A Java 17 + JavaFX 21 desktop AI companion application with full chat UI, module system, world context management, and AI Base Export.

## Setup

### 1. Prerequisites
- Java 17+
- Maven 3.8+

### 2. Configure API Keys
Edit `aria/.env` (or copy from `.env.example`):

```bash
# Get a free Groq key at: https://console.groq.com
# Add it to .env:
    GROQ_API_KEY=gsk_your_key_here
    LLM_PROVIDER=groq

# Optional paid providers:

    ANTHROPIC_API_KEY=sk-ant-...   (claude.ai)
    OPENAI_API_KEY=sk-...          (openai.com)

# Note: At least one key is required. Go to settings to set it up.
```

### 3. Build

```bash
# Double-click
build.bat to build ARIA

or

# Build Command:
    mvn clean package -DskipTests

# Start all in one:
Build + start in one go:
    mvn clean package -DskipTests && mvn javafx:run
```

### 4. Run

```bash
# Double-click
run.bat to start ARIA

or

# Run Command:
mvn javafx:run
```

## Features

- **Chat Window** — Natural conversation with ARIA, a human-raised AI companion
- **Setup Wizard** — Conversational first-run onboarding
- **Module Control Panel** — Toggle ARIA's behavioral modes
- **World Context Editor** — Define ARIA's world, role, and knowledge
- **Settings Panel** — API keys, model selection, theme, persistence
- **AI Base Export** — Package any world context as a portable `.json` file for use in any LLM application

### Rebuilding
```bash
  # Rebuild (after updating the files):
    mvn clean package -DskipTests
 
  # Rebuild + start in one go:
    mvn clean package -DskipTests && mvn javafx:run

  # Start ARIA:
    mvn javafx:run

## WHEN TO REBUILD ##

  Only rebuild when you updated the .java files.
  If you only change .env or config files, just restart normally.
```

### Key Files
```bash
  .env                        — API keys and settings
  config/modules.json         — saved module on/off states
  config/world_context.json   — world context data
  prompts/aria_system_prompt.txt — ARIA's personality prompt (edit freely)
  exports/                    — AI Base export files
```

## Modules

| Module | Default | Description |
|---|---|---|
| FOOL MODE | OFF | Comedy mode — confused and endearing |
| SCHOLAR MODE | OFF | Deep analysis with visible reasoning |
| NPC MODE | OFF | Full character immersion, no AI acknowledgment |
| ASSISTANT MODE | ON | Task-focused but still sounds like ARIA |
| MATH ENGINE | ON | Step-by-step math with auto-detection |
| MEMORY | ON | Conversation history in session |

## AI Base Export

Export any world context as a self-contained `.json` file:
- Includes assembled system prompt (ready to paste into any LLM API)
- Importable by any program, app, or developer
- Supports JSON (full package) and TXT (prompt only) formats
- Saved to `exports/` directory

## Project Structure

```
aria/
├── pom.xml
├── .env
├── config/
│   ├── modules.json         # Module states (persisted)
│   └── world_context.json   # Active world context
├── data/
│   └── conversation_history.json
├── exports/                 # AI Base export files
├── prompts/
│   └── aria_system_prompt.txt
└── src/main/java/aria/
    ├── Main.java
    ├── core/
    │   ├── AriaCore.java
    │   ├── ConfigStore.java
    │   ├── LLMClient.java
    │   ├── ModuleManager.java
    │   ├── ContextManager.java
    │   ├── ConversationHistory.java
    │   └── AIBaseExporter.java
    └── ui/
        ├── MainWindow.java
        ├── SetupWizard.java
        ├── ModulePanel.java
        ├── WorldContextEditor.java
        ├── SettingsPanel.java
        └── AIBaseExportPanel.java
```
