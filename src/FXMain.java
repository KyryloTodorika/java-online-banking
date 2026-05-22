import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;

public class FXMain extends Application {

    private BankService bank;
    private ExportService exportService;
    private User currentUser;

    @Override
    public void start(Stage stage) {

        Connection conn = DatabaseConnection.getConnection();

        if (conn == null) {
            System.out.println("Database connection failed");
            return;
        }

        // =========================
        // SERVICES INIT
        // =========================
        bank = new Bank(conn);
        exportService = new ExportService(conn);

        stage.setTitle("Bank System");

        // =========================
        // LOGIN SCREEN FIRST
        // =========================
        stage.setScene(LoginView.create(this, stage));
        stage.show();

        // =========================
        // EXPORT ON EXIT
        // =========================
        stage.setOnCloseRequest(event -> {
            try {
                System.out.println("Exporting data before exit...");
                exportService.exportUsers();
            } catch (Exception e) {
                System.out.println("Export failed on exit");
            }
        });
    }

    // =========================
    // LOGIN SUCCESS HANDLER
    // =========================
    public void setUser(User user, Stage stage) {
        this.currentUser = user;
        openMainScene(user, stage);
    }

    // =========================
    // SINGLE MAIN SCENE (NEW APPROACH)
    // =========================
    private void openMainScene(User user, Stage stage) {
        stage.setScene(MainMenuView.create(this, user, stage));
    }

    // =========================
    // SERVICE ACCESSORS
    // =========================
    public BankService getBank() {
        return bank;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // =========================
    // MAIN METHOD
    // =========================
    public static void main(String[] args) {
        launch(args);
    }
}