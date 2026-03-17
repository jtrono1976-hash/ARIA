# ARIA — Adaptive Reasoning and Intelligence Assistant

A Java 17 + JavaFX 21 desktop AI companion application with full chat UI, module system, world context management, and AI Base Export.

## Setup

### 1. Prerequisites
- Java 17+
- Maven 3.8+

### 2. Configure API Keys
Edit `aria/.env` (or copy from `.env.example`):

```
ANTHROPIC_API_KEY=your_anthropic_key_here
OPENAI_API_KEY=your_openai_key_here
LLM_PROVIDER=claude
```

At least one key is required. Claude is used by default with OpenAI as fallback.

### 3. Build

```bash
cd aria
mvn clean package -q
```

### 4. Run

```bash
# Via Maven plugin
mvn javafx:run

# Or via the built jar
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar target/aria-companion-1.0.0.jar
```

## Features

- **Chat Window** — Natural conversation with ARIA, a human-raised AI companion
- **Setup Wizard** — Conversational first-run onboarding
- **Module Control Panel** — Toggle ARIA's behavioral modes
- **World Context Editor** — Define ARIA's world, role, and knowledge
- **Settings Panel** — API keys, model selection, theme, persistence
- **AI Base Export** — Package any world context as a portable `.json` file for use in any LLM application

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
