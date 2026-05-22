import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MainMenuView {

    // =========================
    // DIALOG STYLING HELPER
    // =========================
    private static void styleDialog(Dialog<?> d) {

        DialogPane pane = d.getDialogPane();

        pane.getStyleClass().add("bank-dialog");

        pane.getStylesheets().add(
                "file:src/resources/style.css"
        );
    }

    public static Scene create(FXMain app, User user, Stage stage) {

        VBox root = new VBox(10);

        // =========================
        // APPLY ROOT STYLE (IMPORTANT)
        // =========================
        root.setStyle(
                "-fx-background-color: #1a1a1a;" +
                        "-fx-padding: 20;" +
                        "-fx-spacing: 8;"
        );

        // =========================
        // LABELS
        // =========================
        Label welcome = new Label("USER: " + user.getUsername());
        Label output = new Label();

        welcome.getStyleClass().add("header");
        output.getStyleClass().add("output");

        // =========================
        // USER BUTTONS
        // =========================

        Button balance = new Button("Show Balance");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        Button transactions = new Button("Transactions");
        Button transfer = new Button("Transfer");
        Button logout = new Button("Logout");

        // =========================
        // BALANCE
        // =========================
        balance.setOnAction(e -> {
            try {
                double b = app.getBank().getBalance(user.getId());
                output.setText("Balance: " + b);
            } catch (SQLException ex) {
                output.setText("Error");
            }
        });

        // =========================
        // DEPOSIT
        // =========================
        deposit.setOnAction(e -> {

            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Amount");
            styleDialog(d);

            d.showAndWait().ifPresent(val -> {
                try {
                    app.getBank().deposit(user.getId(), Double.parseDouble(val));
                    output.setText("Deposited");
                } catch (Exception ex) {
                    output.setText("Error");
                }
            });
        });

        // =========================
        // WITHDRAW
        // =========================
        withdraw.setOnAction(e -> {

            TextInputDialog amount = new TextInputDialog();
            amount.setHeaderText("Amount");
            styleDialog(amount);

            amount.showAndWait().ifPresent(val -> {

                TextInputDialog pin = new TextInputDialog();
                pin.setHeaderText("PIN");
                styleDialog(pin);

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

        // =========================
        // TRANSACTIONS
        // =========================
        transactions.setOnAction(e -> {
            try {
                output.setText(app.getBank().printTransactions(user.getId()));
            } catch (SQLException ex) {
                output.setText("Error loading transactions");
            }
        });

        // =========================
        // TRANSFER
        // =========================
        transfer.setOnAction(e -> {

            TextInputDialog r = new TextInputDialog();
            r.setHeaderText("Receiver");
            styleDialog(r);

            r.showAndWait().ifPresent(receiver -> {

                TextInputDialog amount = new TextInputDialog();
                amount.setHeaderText("Amount");
                styleDialog(amount);

                amount.showAndWait().ifPresent(val -> {

                    TextInputDialog pin = new TextInputDialog();
                    pin.setHeaderText("PIN");
                    styleDialog(pin);

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

        // =========================
        // LOGOUT
        // =========================
        logout.setOnAction(e ->
                stage.setScene(LoginView.create(app, stage))
        );

        // =========================
        // ADD USER COMPONENTS
        // =========================
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
            adminLabel.getStyleClass().add("admin-label");

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
                styleDialog(d);

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
                styleDialog(d);

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
                styleDialog(d);

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

        // =========================
        // FINAL LAYOUT
        // =========================

        root.getChildren().addAll(logout, output);

        Scene scene = new Scene(root, 400, 500);

        scene.getStylesheets().add(
                "file:src/resources/style.css"
        );

        return scene;
    }
}