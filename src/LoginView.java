import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    public static Scene create(FXMain app, Stage stage) {

        TextField username = new TextField();
        PasswordField password = new PasswordField();

        Label status = new Label();

        Button login = new Button("Login");
        Button goRegister = new Button("Go to Register");

        // LOGIN
        login.setOnAction(_ -> {

            User user = app.getBank().login(
                    username.getText(),
                    password.getText()
            );

            if (user != null) {
                app.setUser(user, stage);
            } else {
                status.setText("Login failed");
            }
        });

        // NAVIGATE TO REGISTER
        goRegister.setOnAction(_ ->
                stage.setScene(RegisterView.create(app, stage))
        );

        VBox root = new VBox(10,
                new Label("LOGIN"),
                username,
                password,
                login,
                goRegister,
                status
        );

        root.setPadding(new Insets(20));

        return new Scene(root, 300, 250);
    }
}