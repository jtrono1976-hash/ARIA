========================================
  ARIA — AI Companion
  Quick Reference
========================================

--- SIMPLE COMMANDS (Windows) ---

  Double-click  run.bat     — start ARIA
  Double-click  build.bat   — rebuild after updating files

  Or from the aria folder in Command Prompt:
    run       — start ARIA
    build     — rebuild ARIA


--- IF YOU PREFER TYPING MAVEN ---

  Start ARIA:
    mvn javafx:run

  Rebuild (after copying updated files from Replit):
    mvn clean package -DskipTests

  Rebuild + start in one go:
    mvn clean package -DskipTests && mvn javafx:run


--- WHEN TO REBUILD ---

  Only rebuild when you copy updated .java files from Replit.
  If you only change .env or config files, just restart normally.


--- KEY FILES ---

  .env                        — API keys and settings
  config/modules.json         — saved module on/off states
  config/world_context.json   — world context data
  prompts/aria_system_prompt.txt — ARIA's personality prompt (edit freely)
  exports/                    — AI Base export files


--- API KEYS ---

  Get a free Groq key at: https://console.groq.com
  Add it to .env:
    GROQ_API_KEY=gsk_your_key_here
    LLM_PROVIDER=groq

  Optional paid providers:
    ANTHROPIC_API_KEY=sk-ant-...   (claude.ai)
    OPENAI_API_KEY=sk-...          (openai.com)


--- MODULES ---

  ASSISTANT MODE  — direct, task-focused responses (default)
  SCHOLAR MODE    — deep reasoning, shows thinking
  FOOL MODE       — playful, confused, chaotic
  NPC MODE        — stays in character as world context
  MATH ENGINE     — step-by-step math working
  MEMORY          — remembers earlier in the conversation

  Toggle anytime from the sidebar or Module Control panel.

========================================
