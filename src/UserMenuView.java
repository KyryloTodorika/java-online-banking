import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UserMenuView {

    public static Scene create(FXMain app, User user, Stage stage) {

        Label welcome = new Label("USER: " + user.getUsername());
        Label output = new Label();

        Button balance = new Button("Show Balance");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        Button transactions = new Button("Transactions");
        Button transfer = new Button("Transfer");
        Button logout = new Button("Logout");

        // BALANCE
        balance.setOnAction(e -> {
            try {
                output.setText("Balance: " +
                        app.getBank().getBalance(user.getId()));
            } catch (SQLException ex) {
                output.setText("Error");
            }
        });

        // DEPOSIT
        deposit.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Amount");

            d.showAndWait().ifPresent(val -> {
                try {
                    app.getBank().deposit(user.getId(),
                            Double.parseDouble(val));
                    output.setText("Deposited");
                } catch (Exception ex) {
                    output.setText("Error");
                }
            });
        });

        // WITHDRAW
        withdraw.setOnAction(e -> {
            TextInputDialog amount = new TextInputDialog();
            amount.setHeaderText("Amount");

            amount.showAndWait().ifPresent(val -> {

                TextInputDialog pin = new TextInputDialog();
                pin.setHeaderText("PIN");

                pin.showAndWait().ifPresent(p -> {
                    try {
                        app.getBank().withdraw(
                                user.getId(),
                                Double.parseDouble(val),
                                p
                        );
                        output.setText("Withdraw success");
                    } catch (Exception ex) {
                        output.setText("Error");
                    }
                });
            });
        });

        // TRANSACTIONS
        transactions.setOnAction(e -> {
            try {
                output.setText(
                        app.getBank().printTransactions(user.getId())
                );
            } catch (SQLException ex) {
                output.setText("Error loading transactions");
            }
        });

        // TRANSFER
        transfer.setOnAction(e -> {

            TextInputDialog r = new TextInputDialog();
            r.setHeaderText("Receiver");

            r.showAndWait().ifPresent(receiver -> {

                TextInputDialog amount = new TextInputDialog();
                amount.setHeaderText("Amount");

                amount.showAndWait().ifPresent(val -> {

                    TextInputDialog pin = new TextInputDialog();
                    pin.setHeaderText("PIN");

                    pin.showAndWait().ifPresent(p -> {
                        try {
                            app.getBank().transfer(
                                    user.getId(),
                                    receiver,
                                    Double.parseDouble(val),
                                    p
                            );
                            output.setText("Transfer done");
                        } catch (Exception ex) {
                            output.setText("Error");
                        }
                    });
                });
            });
        });

        // LOGOUT
        logout.setOnAction(e ->
                stage.setScene(LoginView.create(app, stage))
        );

        return new Scene(new VBox(10,
                welcome, balance, deposit,
                withdraw, transactions,
                transfer, logout, output
        ), 400, 400);
    }
}