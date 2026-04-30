import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;

public class FXMain extends Application {

    private BankService bank;
    private User currentUser;
    private ExportService exportService;

    @Override
    public void start(Stage stage) {

        Connection conn = DatabaseConnection.getConnection();
        bank = new Bank(conn);
        exportService = new ExportService(conn);

        stage.setTitle("Bank System");

        stage.setOnCloseRequest(event -> {

            System.out.println("App closing... exporting data");

            try {
                exportService.exportUsers();
            } catch (Exception e) {
                System.out.println("Export failed on exit");
            }
        });


        stage.setScene(LoginView.create(this, stage));
        stage.show();
    }

    public BankService getBank() {
        return bank;
    }

    public void setUser(User user, Stage stage) {
        this.currentUser = user;
        stage.setScene(MainMenuView.create(this, user, stage));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ExportService getExportService() {
        return exportService;
    }



    static void main(String[] args) {
        launch(args);
    }
}