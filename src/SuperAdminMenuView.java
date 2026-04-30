import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SuperAdminMenuView {

    public static Scene create(FXMain app, User user, Stage stage) {

        VBox root = new VBox(10);

        Label label = new Label("SUPER ADMIN: " + user.getUsername());
        Label output = new Label();

        Button promote = new Button("Promote User");
        Button export = new Button("Export Users");
        Button system = new Button("System Status");
        Button logout = new Button("Logout");

        // PROMOTE
        promote.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Username");

            d.showAndWait().ifPresent(u -> {
                try {
                    app.getBank().promoteToAdmin(u);
                    output.setText("Promoted");
                } catch (Exception ex) {
                    output.setText("Error");
                }
            });
        });

        // EXPORT
        export.setOnAction(e -> {
            app.getExportService().exportUsers();
            output.setText("Export done");
        });

        // SYSTEM CHECK
        system.setOnAction(e ->
                output.setText("System OK")
        );

        logout.setOnAction(e ->
                stage.setScene(LoginView.create(app, stage))
        );

        root.getChildren().addAll(
                label,
                promote,
                export,
                system,
                logout,
                output
        );

        return new Scene(root, 400, 400);
    }
}