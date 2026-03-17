package aria.ui;

import aria.core.AriaCore;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainWindow {

    private final AriaCore core;
    private final Stage stage;

    private VBox chatBox;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Button sendButton;
    private HBox typingIndicator;
    private Label worldContextLabel;
    private VBox moduleStatusBox;
    private Label modeBadge;

    public MainWindow(AriaCore core, Stage stage) {
        this.core = core;
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-window");

        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());
        root.setCenter(buildChatArea());

        Scene scene = new Scene(root, 1100, 720);
        applyStyles(scene);

        stage.setScene(scene);
        stage.setTitle("ARIA v" + aria.core.ConfigStore.VERSION + " — AI Companion");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.show();

        Platform.runLater(() -> inputField.requestFocus());
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 10, 16));
        topBar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 0 0 1 0;");

        Label ariaLabel = new Label("ARIA");
        ariaLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-accent-fg;");

        Label dash = new Label("—");
        dash.setStyle("-fx-text-fill: -color-fg-muted;");

        String worldName = core.getContextManager().getWorldName();
        worldContextLabel = new Label(worldName.isEmpty() ? "Real World" : worldName);
        worldContextLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");

        modeBadge = new Label();
        updateModeBadge();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsBtn = new Button("⚙ Settings");
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -color-fg-muted; -fx-cursor: hand;");
        settingsBtn.setOnAction(e -> openSettings());

        Button clearBtn = new Button("Clear Chat");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -color-fg-muted; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            chatBox.getChildren().clear();
            core.clearHistory();
        });

        topBar.getChildren().addAll(ariaLabel, dash, worldContextLabel, modeBadge, spacer, clearBtn, settingsBtn);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 0 1 0 0;");

        VBox avatarArea = new VBox(8);
        avatarArea.setAlignment(Pos.CENTER);
        avatarArea.setPadding(new Insets(24, 12, 16, 12));
        avatarArea.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 0 0 1 0;");

        Label avatar = new Label("◈");
        avatar.setStyle("-fx-font-size: 42px; -fx-text-fill: -color-accent-fg;");

        Label nameLabel = new Label("ARIA");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("AI Companion");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;");

        avatarArea.getChildren().addAll(avatar, nameLabel, subtitleLabel);

        VBox modulesSection = new VBox(0);
        modulesSection.setPadding(new Insets(12, 0, 0, 0));

        Label modulesHeader = new Label("MODULES");
        modulesHeader.setPadding(new Insets(0, 12, 6, 12));
        modulesHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: -color-fg-muted; -fx-letter-spacing: 1px;");

        moduleStatusBox = new VBox(2);
        moduleStatusBox.setPadding(new Insets(0, 8, 0, 8));
        refreshModuleStatus();

        modulesSection.getChildren().addAll(modulesHeader, moduleStatusBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox bottomButtons = new VBox(6);
        bottomButtons.setPadding(new Insets(12));
        bottomButtons.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1 0 0 0;");

        Button modulesBtn = new Button("⊞ Module Control");
        modulesBtn.setMaxWidth(Double.MAX_VALUE);
        modulesBtn.setStyle("-fx-cursor: hand;");
        modulesBtn.setOnAction(e -> openModulePanel());

        Button worldBtn = new Button("⊕ World Context");
        worldBtn.setMaxWidth(Double.MAX_VALUE);
        worldBtn.setStyle("-fx-cursor: hand;");
        worldBtn.setOnAction(e -> openWorldContextEditor());

        Button exportBtn = new Button("⇥ Export AI Base");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setStyle("-fx-cursor: hand;");
        exportBtn.setOnAction(e -> openExportPanel());

        bottomButtons.getChildren().addAll(modulesBtn, worldBtn, exportBtn);

        sidebar.getChildren().addAll(avatarArea, modulesSection, spacer, bottomButtons);
        return sidebar;
    }

    private void refreshModuleStatus() {
        moduleStatusBox.getChildren().clear();
        core.getModuleManager().getAll().forEach((name, enabled) -> {
            HBox row = new HBox(6);
            row.setPadding(new Insets(3, 6, 3, 6));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-cursor: hand; -fx-background-radius: 4;");

            Label indicator = new Label(enabled ? "●" : "○");
            indicator.setStyle("-fx-font-size: 9px; -fx-text-fill: " + (enabled ? "#3fb950" : "-color-fg-subtle") + ";");

            Label nameLabel = new Label(name.replace("_", " "));
            nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (enabled ? "-color-fg-default" : "-color-fg-muted") + ";");

            row.getChildren().addAll(indicator, nameLabel);

            row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: -color-bg-inset; -fx-background-radius: 4;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-background-radius: 4;"));
            row.setOnMouseClicked(e -> {
                boolean newState = !core.getModuleManager().isEnabled(name);
                core.getModuleManager().setEnabled(name, newState);
                refreshModuleStatus();
                updateModeBadge();
            });

            moduleStatusBox.getChildren().add(row);
        });
    }

    private BorderPane buildChatArea() {
        BorderPane chatPane = new BorderPane();

        chatBox = new VBox(10);
        chatBox.setPadding(new Insets(16));
        chatBox.setFillWidth(true);

        scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: -color-bg-default; -fx-background-color: -color-bg-default;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        typingIndicator = buildTypingIndicator();
        typingIndicator.setVisible(false);
        typingIndicator.setManaged(false);

        VBox chatContainer = new VBox();
        chatContainer.getChildren().addAll(scrollPane, typingIndicator);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        chatPane.setCenter(chatContainer);
        chatPane.setBottom(buildInputArea());

        addAriaMessage("hey. I'm here. what's up?");

        return chatPane;
    }

    private HBox buildTypingIndicator() {
        HBox indicator = new HBox(10);
        indicator.setAlignment(Pos.CENTER_LEFT);
        indicator.setPadding(new Insets(8, 16, 8, 16));
        indicator.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 1 0 0 0;");

        Label avatarDot = new Label("◈");
        avatarDot.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-accent-fg;");

        Label dot1 = new Label("●");
        Label dot2 = new Label("●");
        Label dot3 = new Label("●");
        for (Label dot : new Label[]{dot1, dot2, dot3}) {
            dot.setStyle("-fx-font-size: 8px; -fx-text-fill: -color-fg-muted;");
        }

        animateDot(dot1, 0);
        animateDot(dot2, 200);
        animateDot(dot3, 400);

        indicator.getChildren().addAll(avatarDot, dot1, dot2, dot3);
        return indicator;
    }

    private void animateDot(Label dot, int delayMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(600), dot);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.setCycleCount(Timeline.INDEFINITE);
        ft.setAutoReverse(true);
        ft.setDelay(Duration.millis(delayMs));
        ft.play();
    }

    private HBox buildInputArea() {
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(12, 16, 12, 16));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 1 0 0 0;");

        inputField = new TextField();
        inputField.setPromptText("Message ARIA...");
        inputField.setStyle("-fx-font-size: 14px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> sendMessage());

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setStyle("-fx-cursor: hand;");
        sendButton.getStyleClass().add("accent");
        sendButton.setOnAction(e -> sendMessage());

        inputArea.getChildren().addAll(inputField, sendButton);
        return inputArea;
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        inputField.clear();
        inputField.setDisable(true);
        sendButton.setDisable(true);

        addUserMessage(text);
        showTypingIndicator(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return core.chat(text);
            }
        };

        task.setOnSucceeded(e -> {
            showTypingIndicator(false);
            String response = task.getValue();
            addAriaMessage(response);
            inputField.setDisable(false);
            sendButton.setDisable(false);
            Platform.runLater(() -> inputField.requestFocus());
        });

        task.setOnFailed(e -> {
            showTypingIndicator(false);
            addAriaMessage("something went wrong on my end — " + task.getException().getMessage());
            inputField.setDisable(false);
            sendButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void addUserMessage(String text) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(500);
        bubble.setPadding(new Insets(9, 14, 9, 14));
        bubble.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white; -fx-background-radius: 14 14 4 14; -fx-font-size: 14px;");

        wrapper.getChildren().add(bubble);

        FadeTransition ft = new FadeTransition(Duration.millis(200), wrapper);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        chatBox.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void addAriaMessage(String text) {
        HBox wrapper = new HBox(10);
        wrapper.setAlignment(Pos.TOP_LEFT);

        Label avatarLabel = new Label("◈");
        avatarLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: -color-accent-fg; -fx-padding: 6 0 0 0;");

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(520);
        bubble.setPadding(new Insets(9, 14, 9, 14));
        bubble.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 14 14 14 4; -fx-font-size: 14px; -fx-border-color: -color-border-default; -fx-border-radius: 14 14 14 4; -fx-border-width: 1;");

        wrapper.getChildren().addAll(avatarLabel, bubble);

        FadeTransition ft = new FadeTransition(Duration.millis(250), wrapper);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        chatBox.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void showTypingIndicator(boolean show) {
        Platform.runLater(() -> {
            typingIndicator.setVisible(show);
            typingIndicator.setManaged(show);
        });
    }

    private void openSettings() {
        SettingsPanel panel = new SettingsPanel(core, stage, this);
        panel.show();
    }

    private void openModulePanel() {
        ModulePanel panel = new ModulePanel(core, stage, this);
        panel.show();
    }

    private void openWorldContextEditor() {
        WorldContextEditor editor = new WorldContextEditor(core, stage, this);
        editor.show();
    }

    private void openExportPanel() {
        AIBaseExportPanel panel = new AIBaseExportPanel(core, stage, this);
        panel.show();
    }

    public void refreshAfterContextChange() {
        String worldName = core.getContextManager().getWorldName();
        worldContextLabel.setText(worldName.isEmpty() ? "Real World" : worldName);
        refreshModuleStatus();
        updateModeBadge();
    }

    private void updateModeBadge() {
        String label = core.getModuleManager().getActiveModeLabel();
        modeBadge.setText("[ " + label + " ]");
        boolean isDefault = label.equals("Default");
        modeBadge.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 8 2 8; " +
            "-fx-background-radius: 10; " +
            (isDefault
                ? "-fx-text-fill: -color-fg-muted; -fx-background-color: -color-bg-inset;"
                : "-fx-text-fill: #3fb950; -fx-background-color: #1f6329;")
        );
    }

    private void applyStyles(Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/aria-styles.css") != null
            ? getClass().getResource("/aria-styles.css").toExternalForm()
            : "");
    }
}
