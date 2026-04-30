import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    public static Scene create(FXMain app, Stage stage) {

        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField pin = new TextField();

        Label status = new Label();

        Button login = new Button("Login");
        Button register = new Button("Register");

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

        register.setOnAction(_ -> {

            app.getBank().register(
                    username.getText(),
                    password.getText(),
                    pin.getText()
            );

            status.setText("Registered!");
        });

        VBox root = new VBox(10,
                new Label("Username"), username,
                new Label("Password"), password,
                new Label("PIN (register)"), pin,
                login,
                register,
                status
        );

        root.setPadding(new Insets(20));

        return new Scene(root, 300, 300);
    }
}