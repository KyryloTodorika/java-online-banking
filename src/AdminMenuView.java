import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminMenuView {

    public static Scene create(FXMain app, User user, Stage stage) {

        VBox root = new VBox(10);
        Label label = new Label("ADMIN: " + user.getUsername());
        Label output = new Label();

        // reuse user menu buttons concept
        Button listUsers = new Button("List Users");
        Button blockUser = new Button("Block User");
        Button unblockUser = new Button("Unblock User");
        Button totalMoney = new Button("Total Bank Money");

        Button logout = new Button("Logout");

        listUsers.setOnAction(e -> {
            try {
                app.getBank().listAllUsers();
                output.setText("Printed to console");
            } catch (SQLException ex) {
                output.setText("Error");
            }
        });

        totalMoney.setOnAction(e -> {
            try {
                output.setText("Total: " +
                        app.getBank().totalBankMoney());
            } catch (SQLException ex) {
                output.setText("Error");
            }
        });

        root.getChildren().addAll(
                label,
                listUsers,
                blockUser,
                unblockUser,
                totalMoney,
                logout,
                output
        );

        logout.setOnAction(e ->
                stage.setScene(LoginView.create(app, stage))
        );

        return new Scene(root, 400, 400);
    }
}