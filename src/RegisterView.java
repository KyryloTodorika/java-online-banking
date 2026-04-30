import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterView {

    public static Scene create(FXMain app, Stage stage) {

        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField pin = new TextField();

        Label status = new Label();

        Button register = new Button("Register");
        Button backToLogin = new Button("Back to Login");

        // REGISTER
        register.setOnAction(_ -> {

            if (pin.getText().length() != 4) {
                status.setText("PIN must be 4 digits");
                return;
            }

            boolean ok = app.getBank().register(
                    username.getText(),
                    password.getText(),
                    pin.getText()
            );

            if (ok) {
                status.setText("Registered successfully!");
            } else {
                status.setText("Registration failed");
            }
        });

        // BACK
        backToLogin.setOnAction(_ ->
                stage.setScene(LoginView.create(app, stage))
        );

        VBox root = new VBox(10,
                new Label("REGISTER"),
                username,
                password,
                new Label("PIN (4 digits)"),
                pin,
                register,
                backToLogin,
                status
        );

        root.setPadding(new Insets(20));

        return new Scene(root, 300, 300);
    }
}