package aria.ui;

import aria.core.AriaCore;
import aria.core.ConfigStore;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class SettingsPanel {

    private final AriaCore core;
    private final Stage owner;
    private final MainWindow mainWindow;

    public SettingsPanel(AriaCore core, Stage owner, MainWindow mainWindow) {
        this.core = core;
        this.owner = owner;
        this.mainWindow = mainWindow;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: -color-bg-default;");

        VBox header = new VBox(4);
        header.setPadding(new Insets(16, 20, 14, 20));
        header.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Label subtitle = new Label("Configure API keys, model settings, and appearance.");
        subtitle.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        header.getChildren().addAll(title, subtitle);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: -color-bg-default; -fx-background-color: -color-bg-default;");

        VBox form = new VBox(18);
        form.setPadding(new Insets(20));

        ConfigStore config = core.getConfigStore();

        Label apiSection = sectionHeader("API Keys");
        PasswordField claudeKeyField = maskedField("Anthropic API Key", config.get("ANTHROPIC_API_KEY", ""));
        PasswordField openaiKeyField = maskedField("OpenAI API Key", config.get("OPENAI_API_KEY", ""));
        PasswordField groqKeyField = maskedField("Groq API Key (free)", config.get("GROQ_API_KEY", ""));

        Label groqHint = new Label("Get a free Groq key at console.groq.com");
        groqHint.setStyle("-fx-text-fill: #3fb950; -fx-font-size: 11px;");

        Label providerSection = sectionHeader("LLM Provider");
        ToggleGroup providerGroup = new ToggleGroup();
        RadioButton claudeRadio = new RadioButton("Claude (Anthropic)");
        claudeRadio.setToggleGroup(providerGroup);
        RadioButton openaiRadio = new RadioButton("OpenAI");
        openaiRadio.setToggleGroup(providerGroup);
        RadioButton groqRadio = new RadioButton("Groq (Free)");
        groqRadio.setToggleGroup(providerGroup);

        String provider = config.get("LLM_PROVIDER", "claude");
        claudeRadio.setSelected("claude".equalsIgnoreCase(provider));
        openaiRadio.setSelected("openai".equalsIgnoreCase(provider));
        groqRadio.setSelected("groq".equalsIgnoreCase(provider));

        Label claudeModelSection = sectionHeader("Claude Model");
        ComboBox<String> claudeModelBox = new ComboBox<>();
        claudeModelBox.getItems().addAll(
            "claude-haiku-4-5-20251001",
            "claude-sonnet-4-5-20251101",
            "claude-3-haiku-20240307",
            "claude-3-sonnet-20240229",
            "claude-3-5-sonnet-20241022"
        );
        claudeModelBox.setValue(config.get("CLAUDE_MODEL", "claude-haiku-4-5-20251001"));
        claudeModelBox.setMaxWidth(Double.MAX_VALUE);

        Label openaiModelSection = sectionHeader("OpenAI Model");
        ComboBox<String> openaiModelBox = new ComboBox<>();
        openaiModelBox.getItems().addAll("gpt-4o-mini", "gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo");
        openaiModelBox.setValue(config.get("OPENAI_MODEL", "gpt-4o-mini"));
        openaiModelBox.setMaxWidth(Double.MAX_VALUE);

        Label groqModelSection = sectionHeader("Groq Model (Free)");
        ComboBox<String> groqModelBox = new ComboBox<>();
        groqModelBox.getItems().addAll(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "llama3-70b-8192",
            "llama3-8b-8192",
            "mixtral-8x7b-32768",
            "gemma2-9b-it"
        );
        groqModelBox.setValue(config.get("GROQ_MODEL", "llama-3.3-70b-versatile"));
        groqModelBox.setMaxWidth(Double.MAX_VALUE);

        Label tokensSection = sectionHeader("Max Tokens");
        Slider tokensSlider = new Slider(100, 4000, Integer.parseInt(config.get("MAX_TOKENS", "1000")));
        tokensSlider.setMajorTickUnit(500);
        tokensSlider.setShowTickMarks(true);
        tokensSlider.setShowTickLabels(true);
        tokensSlider.setSnapToTicks(false);
        Label tokensValue = new Label((int) tokensSlider.getValue() + " tokens");
        tokensValue.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        tokensSlider.valueProperty().addListener((obs, old, val) ->
            tokensValue.setText((int) val.doubleValue() + " tokens"));

        Label historySection = sectionHeader("Conversation History");
        CheckBox persistHistory = new CheckBox("Persist history between sessions");
        persistHistory.setSelected(Boolean.parseBoolean(config.get("PERSIST_HISTORY", "false")));

        Label themeSection = sectionHeader("Theme");
        ToggleGroup themeGroup = new ToggleGroup();
        RadioButton darkTheme = new RadioButton("Primer Dark");
        darkTheme.setToggleGroup(themeGroup);
        RadioButton lightTheme = new RadioButton("Primer Light");
        lightTheme.setToggleGroup(themeGroup);
        String currentTheme = config.get("THEME", "dark");
        darkTheme.setSelected("dark".equalsIgnoreCase(currentTheme));
        lightTheme.setSelected("light".equalsIgnoreCase(currentTheme));

        Label authorSection = sectionHeader("Author Name");
        TextField authorField = new TextField(config.get("AUTHOR_NAME", "Natt"));
        authorField.setPromptText("Your name (used in AI Base exports)");

        form.getChildren().addAll(
            apiSection,
            labeledNode("Anthropic API Key", claudeKeyField),
            labeledNode("OpenAI API Key", openaiKeyField),
            labeledNode("Groq API Key", groqKeyField),
            groqHint,
            providerSection,
            new HBox(16, claudeRadio, openaiRadio, groqRadio),
            claudeModelSection,
            claudeModelBox,
            openaiModelSection,
            openaiModelBox,
            groqModelSection,
            groqModelBox,
            tokensSection,
            tokensSlider,
            tokensValue,
            historySection,
            persistHistory,
            themeSection,
            new HBox(16, darkTheme, lightTheme),
            authorSection,
            labeledNode("Author Name", authorField)
        );

        scrollPane.setContent(form);

        HBox footer = new HBox(10);
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("accent");
        saveBtn.setStyle("-fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            config.set("ANTHROPIC_API_KEY", claudeKeyField.getText().trim());
            config.set("OPENAI_API_KEY", openaiKeyField.getText().trim());
            config.set("GROQ_API_KEY", groqKeyField.getText().trim());

            String selectedProvider = "claude";
            if (openaiRadio.isSelected()) selectedProvider = "openai";
            else if (groqRadio.isSelected()) selectedProvider = "groq";
            config.set("LLM_PROVIDER", selectedProvider);

            config.set("CLAUDE_MODEL", claudeModelBox.getValue());
            config.set("OPENAI_MODEL", openaiModelBox.getValue());
            config.set("GROQ_MODEL", groqModelBox.getValue());
            config.set("MAX_TOKENS", String.valueOf((int) tokensSlider.getValue()));
            config.set("PERSIST_HISTORY", String.valueOf(persistHistory.isSelected()));
            config.set("AUTHOR_NAME", authorField.getText().trim());

            String newTheme = darkTheme.isSelected() ? "dark" : "light";
            config.set("THEME", newTheme);
            if ("dark".equals(newTheme)) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            }

            config.saveToFile();
            dialog.close();
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);

        root.getChildren().addAll(header, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 520, 680);
        dialog.setScene(scene);
        dialog.setMinWidth(420);
        dialog.setMinHeight(500);
        dialog.show();
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: -color-fg-muted; -fx-letter-spacing: 1px; -fx-padding: 8 0 0 0;");
        return l;
    }

    private PasswordField maskedField(String prompt, String value) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setText(value);
        return field;
    }

    private VBox labeledNode(String label, javafx.scene.Node node) {
        VBox box = new VBox(5);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");
        box.getChildren().addAll(l, node);
        return box;
    }
}
