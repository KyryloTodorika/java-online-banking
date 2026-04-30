import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MainMenuView {

    public static Scene create(FXMain app, User user, Stage stage) {

        Label welcome = new Label("Welcome " + user.getUsername());
        Label output = new Label();

        Button balance = new Button("Show Balance");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        Button transactions = new Button("Transactions");
        Button transfer = new Button("Transfer");
        Button logout = new Button("Logout");

        // BALANCE
        balance.setOnAction(_ -> {
            double b;
            try {
                b = app.getBank().getBalance(user.getId());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            output.setText("Balance: " + b);
        });

        // DEPOSIT
        deposit.setOnAction(_ -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Deposit amount");

            dialog.showAndWait().ifPresent(val -> {
                double amount = Double.parseDouble(val);
                try {
                    app.getBank().deposit(user.getId(), amount);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                output.setText("Deposited: " + amount);
            });
        });

        // WITHDRAW
        withdraw.setOnAction(_ -> {
            TextInputDialog amountDialog = new TextInputDialog();
            amountDialog.setHeaderText("Withdraw amount");

            amountDialog.showAndWait().ifPresent(val -> {

                TextInputDialog pinDialog = new TextInputDialog();
                pinDialog.setHeaderText("PIN");

                pinDialog.showAndWait().ifPresent(pin -> {
                    try {
                        app.getBank().withdraw(user.getId(),
                                Double.parseDouble(val),
                                pin);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    output.setText("Withdraw success");
                });
            });
        });

        // TRANSACTIONS
        transactions.setOnAction(_ -> {

            try {
                String result = app.getBank().printTransactions(user.getId());
                output.setText(result);

            } catch (SQLException ex) {
                output.setText("Error loading transactions");
            }
        });

        // TRANSFER
        transfer.setOnAction(_ -> {

            TextInputDialog receiver = new TextInputDialog();
            receiver.setHeaderText("Receiver username");

            receiver.showAndWait().ifPresent(r -> {

                TextInputDialog amountDialog = new TextInputDialog();
                amountDialog.setHeaderText("Amount");

                amountDialog.showAndWait().ifPresent(val -> {

                    TextInputDialog pinDialog = new TextInputDialog();
                    pinDialog.setHeaderText("PIN");

                    pinDialog.showAndWait().ifPresent(pin -> {

                        try {
                            app.getBank().transfer(
                                    user.getId(),
                                    r,
                                    Double.parseDouble(val),
                                    pin
                            );
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                        output.setText("Transfer done");
                    });
                });
            });
        });

        // LOGOUT
        logout.setOnAction(_ -> stage.setScene(LoginView.create(app, stage)));

        VBox root = new VBox(10,
                welcome,
                balance,
                deposit,
                withdraw,
                transactions,
                transfer,
                logout,
                output
        );

        root.setStyle("-fx-padding: 20");

        return new Scene(root, 350, 400);
    }
}