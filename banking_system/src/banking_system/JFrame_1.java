package banking_system4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class JFrame_1 extends JFrame {
    private JTextField cardIdField;
    private JPasswordField passwordField;
    private JTextArea transactionArea;
    private JButton loginButton;
    private JButton checkBalanceButton;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton transferButton;
    private JButton exitButton;

    private DatabaseHandler dbHandler;

    private String cardId;
    private String password;
    private double balance;
    private double depositAmount;
    private double withdrawalAmount;
    private double transferAmount;
    private String userIdQuery;
    private String recipientCardId;

    public JFrame_1() throws SQLException, ClassNotFoundException {
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardIdField = new JTextField(20);
        cardIdField.setBounds(121, 5, 224, 22);
        passwordField = new JPasswordField(20);
        passwordField.setBounds(121, 35, 224, 22);
        loginButton = new JButton("Login");
        loginButton.setBounds(195, 57, 65, 28);
        checkBalanceButton = new JButton("Check Balance");
        checkBalanceButton.setBounds(17, 97, 119, 28);
        depositButton = new JButton("Deposit");
        depositButton.setBounds(140, 97, 77, 28);
        withdrawButton = new JButton("Withdraw");
        withdrawButton.setBounds(219, 97, 89, 28);
        transferButton = new JButton("Transfer");
        transferButton.setBounds(310, 97, 83, 28);
        exitButton = new JButton("Exit");
        exitButton.setBounds(405, 97, 55, 28);

        checkBalanceButton.setEnabled(false);
        depositButton.setEnabled(false);
        withdrawButton.setEnabled(false);
        transferButton.setEnabled(false);
        exitButton.setEnabled(false);

        getContentPane().setLayout(null);

        JLabel label = new JLabel("Card ID:");
        label.setBounds(72, 7, 44, 18);
        getContentPane().add(label);
        getContentPane().add(cardIdField);
        JLabel label_1 = new JLabel("Password:");
        label_1.setBounds(55, 37, 61, 18);
        getContentPane().add(label_1);
        getContentPane().add(passwordField);
        getContentPane().add(loginButton);
        getContentPane().add(checkBalanceButton);
        getContentPane().add(depositButton);
        getContentPane().add(withdrawButton);
        getContentPane().add(transferButton);
        getContentPane().add(exitButton);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(17, 153, 443, 183);
        getContentPane().add(scrollPane);
        transactionArea = new JTextArea(10, 40);
        scrollPane.setViewportView(transactionArea);

        dbHandler = new DatabaseHandler();

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardId = cardIdField.getText();
                password = new String(passwordField.getPassword());

                ResultSet rs = null;
                try {
                    rs = dbHandler.executeQuery("SELECT hashed_password, salt, balance FROM users WHERE card_id='" + cardId + "'");
                    if (!rs.next()) {
                        transactionArea.append("Invalid card ID\n");
                    } else {
                        String storedHashedPassword = rs.getString("hashed_password");
                        String salt = rs.getString("salt");
                        balance = rs.getDouble("balance");

                        String userEnteredHashedPassword = HashUtil.hashPassword(password, salt);

                        if (!storedHashedPassword.equals(userEnteredHashedPassword)) {
                            transactionArea.append("Invalid password\n");
                        } else {
                            transactionArea.append("User authentication successful\n");
                            checkBalanceButton.setEnabled(true);
                            depositButton.setEnabled(true);
                            withdrawButton.setEnabled(true);
                            transferButton.setEnabled(true);
                            exitButton.setEnabled(true);
                        }
                    }
                } catch (SQLException sqlEx) {
                    System.err.println("Failed to execute query");
                    sqlEx.printStackTrace();
                }
            }
        });

        checkBalanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transactionArea.append("Your balance is: " + balance + "\n");
            }
        });

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String depositAmountStr = JOptionPane.showInputDialog(null, "Enter deposit amount:");
                if (depositAmountStr != null) {
                    try {
                        depositAmount = Double.parseDouble(depositAmountStr);
                        balance += depositAmount;
                        dbHandler.executeUpdate("UPDATE users SET balance=" + balance + " WHERE card_id='" + cardId + "'");
                        insertTransaction(dbHandler, cardId, null, depositAmount, balance, false);
                        transactionArea.append("Deposit successful. Your new balance is " + balance + "\n");
                    } catch (NumberFormatException ex) {
                        transactionArea.append("Invalid amount\n");
                    } catch (SQLException sqlEx) {
                        System.err.println("Failed to execute update");
                        sqlEx.printStackTrace();
                    }
                }
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String withdrawalAmountStr = JOptionPane.showInputDialog(null, "Enter withdrawal amount:");
                if (withdrawalAmountStr != null) {
                    try {
                        withdrawalAmount = Double.parseDouble(withdrawalAmountStr);
                        if (withdrawalAmount > balance) {
                            transactionArea.append("Insufficient funds\n");
                        } else {
                            balance -= withdrawalAmount;
                            dbHandler.executeUpdate("UPDATE users SET balance=" + balance + " WHERE card_id='" + cardId + "'");
                            insertTransaction(dbHandler, cardId, null, -withdrawalAmount, balance, false);
                            transactionArea.append("Withdrawal successful. Your new balance is " + balance + "\n");
                        }
                    } catch (NumberFormatException ex) {
                        transactionArea.append("Invalid amount\n");
                    } catch (SQLException sqlEx) {
                        System.err.println("Failed to execute update");
                        sqlEx.printStackTrace();
                    }
                }
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recipientCardId = JOptionPane.showInputDialog(null, "Enter recipient card ID:");
                if (recipientCardId != null) {
                    String transferAmountStr = JOptionPane.showInputDialog(null, "Enter transfer amount:");
                    if (transferAmountStr != null) {
                        try {
                            transferAmount = Double.parseDouble(transferAmountStr);
                            if (transferAmount > balance) {
                                transactionArea.append("Insufficient funds\n");
                            } else {
                                balance -= transferAmount;
                                dbHandler.executeUpdate("UPDATE users SET balance=" + balance + " WHERE card_id='" + cardId + "'");
                                insertTransaction(dbHandler, cardId, recipientCardId, -transferAmount, balance, true);

                                ResultSet recipientBalanceRS = dbHandler.executeQuery("SELECT balance FROM users WHERE card_id='" + recipientCardId + "'");
                                double recipientBalance = 0;
                                if (recipientBalanceRS.next()) {
                                    recipientBalance = recipientBalanceRS.getDouble("balance");
                                }
                                recipientBalance += transferAmount;
                                dbHandler.executeUpdate("UPDATE users SET balance=" + recipientBalance + " WHERE card_id='" + recipientCardId + "'");

                                transactionArea.append("Transfer successful. Your new balance is " + balance + "\n");
                            }
                        } catch (NumberFormatException ex) {
                            transactionArea.append("Invalid amount\n");
                        } catch (SQLException sqlEx) {
                            System.err.println("Failed to execute update");
                            sqlEx.printStackTrace();
                        }
                    }
                }
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void insertTransaction(DatabaseHandler db, String cardId, String recipientCardId, double amount, double balance, boolean isTransfer) {
        try {
            String userIdQuery = "SELECT user_id FROM users WHERE card_id='" + cardId + "'";
            ResultSet rs = db.executeQuery(userIdQuery);
            String userId = "";
            if (rs.next()) {
                userId = rs.getString("user_id");
            }
            rs.close();

            String recipientUserId = "";
            if (isTransfer) {
                String recipientUserIdQuery = "SELECT user_id FROM users WHERE card_id='" + recipientCardId + "'";
                ResultSet recipientRs = db.executeQuery(recipientUserIdQuery);
                if (recipientRs.next()) {
                    recipientUserId = recipientRs.getString("user_id");
                }
                recipientRs.close();
            }

            if (!userId.isEmpty() && (!isTransfer || !recipientUserId.isEmpty())) {
                String insertQuery;
                if (isTransfer) {
                    insertQuery = "INSERT INTO transactions (user_id, recipient_card_id, amount, transfer_amount, sender_balance) VALUES ('" + userId + "', '" + recipientCardId + "', " + amount + ", " + Math.abs(amount) + ", " + balance + ")";
                    db.executeUpdate(insertQuery);

                    insertQuery = "INSERT INTO transactions (user_id, recipient_card_id, amount, transfer_amount, sender_balance) VALUES ('" + recipientUserId + "', NULL, " + Math.abs(amount) + ", 0, NULL)";
                    db.executeUpdate(insertQuery);
                } else {
                    insertQuery = "INSERT INTO transactions (user_id, recipient_card_id, amount, transfer_amount, sender_balance) VALUES ('" + userId + "', NULL, " + amount + ", 0, " + balance + ")";
                    db.executeUpdate(insertQuery);
                }
            } else {
                transactionArea.append("User or recipient does not exist\n");
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert transaction");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new JFrame_1();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
