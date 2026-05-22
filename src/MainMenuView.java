import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MainMenuView {

    public static Scene create(FXMain app, User user, Stage stage) {

        VBox root = new VBox(10);

        Label welcome = new Label("USER: " + user.getUsername());
        Label output = new Label();

        // =========================
        // USER BUTTONS
        // =========================

        Button balance = new Button("Show Balance");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        Button transactions = new Button("Transactions");
        Button transfer = new Button("Transfer");
        Button logout = new Button("Logout");

        balance.setOnAction(e -> {
            try {
                double b = app.getBank().getBalance(user.getId());
                output.setText("Balance: " + b);
            } catch (SQLException ex) {
                output.setText("Error");
            }
        });

        deposit.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Amount");

            d.showAndWait().ifPresent(val -> {
                try {
                    app.getBank().deposit(user.getId(), Double.parseDouble(val));
                    output.setText("Deposited");
                } catch (Exception ex) {
                    output.setText("Error");
                }
            });
        });

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

        transactions.setOnAction(e -> {
            try {
                output.setText(app.getBank().printTransactions(user.getId()));
            } catch (SQLException ex) {
                output.setText("Error loading transactions");
            }
        });

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

        logout.setOnAction(e ->
                stage.setScene(LoginView.create(app, stage))
        );

        root.getChildren().addAll(
                welcome,
                balance,
                deposit,
                withdraw,
                transactions,
                transfer
        );

        // =========================
        // ADMIN LOGIC
        // =========================

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSuper = user.getRole() == Role.SUPER_ADMIN;
        boolean privileged = isAdmin || isSuper;

        if (privileged) {

            Label adminLabel = new Label("=== ADMIN PANEL ===");

            Button listUsers = new Button("List Users");
            Button totalMoney = new Button("Total Bank Money");
            Button blockUser = new Button("Block User");
            Button unblockUser = new Button("Unblock User");

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
                    output.setText("Total: " + app.getBank().totalBankMoney());
                } catch (SQLException ex) {
                    output.setText("Error");
                }
            });

            blockUser.setOnAction(e -> {
                TextInputDialog d = new TextInputDialog();
                d.setHeaderText("User to block");

                d.showAndWait().ifPresent(u -> {
                    try {
                        app.getBank().blockUser(u);
                        output.setText("Blocked");
                    } catch (Exception ex) {
                        output.setText("Error");
                    }
                });
            });

            unblockUser.setOnAction(e -> {
                TextInputDialog d = new TextInputDialog();
                d.setHeaderText("User to unblock");

                d.showAndWait().ifPresent(u -> {
                    try {
                        app.getBank().unblockUser(u);
                        output.setText("Unblocked");
                    } catch (Exception ex) {
                        output.setText("Error");
                    }
                });
            });

            root.getChildren().addAll(
                    new Separator(),
                    adminLabel,
                    listUsers,
                    totalMoney,
                    blockUser,
                    unblockUser
            );
        }

        // =========================
        // SUPER ADMIN
        // =========================

        if (isSuper) {

            Button promote = new Button("Promote User");

            promote.setOnAction(e -> {
                TextInputDialog d = new TextInputDialog();
                d.setHeaderText("User to promote");

                d.showAndWait().ifPresent(u -> {
                    try {
                        app.getBank().promoteToAdmin(u);
                        output.setText("Promoted");
                    } catch (Exception ex) {
                        output.setText("Error");
                    }
                });
            });

            root.getChildren().add(promote);
        }

        // logout always last
        root.getChildren().addAll(logout, output);

        return new Scene(root, 400, 500);
    }
}