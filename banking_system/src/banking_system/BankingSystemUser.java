package banking_system4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BankingSystemUser {

    public static void main(String[] args) {

        DatabaseHandler db = null;
        Scanner scanner = null;

        try {
            db = new DatabaseHandler();

            scanner = new Scanner(System.in);

            System.out.println("Enter card ID: ");
            String cardId = scanner.nextLine();

            System.out.println("Enter password: ");
            String password = scanner.nextLine();

          
            ResultSet rs = db.executeQuery("SELECT hashed_password, salt, balance FROM users WHERE card_id='" + cardId + "'");
            if (!rs.next()) {
                System.out.println("Invalid card ID");
            } else {
                String storedHashedPassword = rs.getString("hashed_password");
                String salt = rs.getString("salt");
                double balance = rs.getDouble("balance");

                
                String userEnteredHashedPassword = HashUtil.hashPassword(password, salt);

                if (!storedHashedPassword.equals(userEnteredHashedPassword)) {
                    System.out.println("Invalid password");
                } else {
                    
                    System.out.println("User authentication successful");

                    boolean exit = false;
                    while (!exit) {
                        System.out.println("Please select an option:");
                        System.out.println("1. Check balance");
                        System.out.println("2. Deposit");
                        System.out.println("3. Withdraw");
                        System.out.println("4. Transfer");
                        System.out.println("5. Exit");

                        int option = scanner.nextInt();
                        scanner.nextLine();  

                        switch (option) {
                            case 1:
                                System.out.println("Your balance is: " + balance);
                                break;
                            case 2:
                                System.out.println("Enter deposit amount: ");
                                double depositAmount = scanner.nextDouble();
                                balance += depositAmount;
                                db.executeUpdate("UPDATE users SET balance=" + balance + " WHERE card_id='" + cardId + "'");
                                
                                insertTransaction(db, cardId, null, depositAmount, balance, false); // 存款
                                System.out.println("Deposit successful. Your new balance is " + balance);
                                break;
                            case 3:
                                System.out.println("Enter withdrawal amount: ");
                                double withdrawalAmount = scanner.nextDouble();
                                if (withdrawalAmount > balance) {
                                    System.out.println("Insufficient funds");
                                } else {
                                    balance -= withdrawalAmount;
                                    db.executeUpdate("UPDATE users SET balance=" + balance + " WHERE card_id='" + cardId + "'");
                                    
                                    insertTransaction(db, cardId, null, -withdrawalAmount, balance, false); // 提款
                                    System.out.println("Withdrawal successful. Your new balance is " + balance);
                                }
                                break;
                            case 4:
                                System.out.println("Enter recipient card ID: ");
                                String recipientCardId = scanner.nextLine();
                                System.out.println("Enter transfer amount: ");
                                double transferAmount = scanner.nextDouble();
                                if (transferAmount > balance) {
                                    System.out.println("Insufficient funds");
                                } else {
                                    balance -= transferAmount;
                                    // Update sender's balance
                                    db.executeUpdate("UPDATE users SET balance=" + balance + " WHERE card_id='" + cardId + "'");
                                    insertTransaction(db, cardId, recipientCardId, -transferAmount, balance, true);
                                    
                                    // Update recipient's balance
                                    ResultSet recipientBalanceRS = db.executeQuery("SELECT balance FROM users WHERE card_id='" + recipientCardId + "'");
                                    double recipientBalance = 0;
                                    if (recipientBalanceRS.next()) {
                                        recipientBalance = recipientBalanceRS.getDouble("balance");
                                    }
                                    recipientBalance += transferAmount;
                                    db.executeUpdate("UPDATE users SET balance=" + recipientBalance + " WHERE card_id='" + recipientCardId + "'");
                                    
                                    System.out.println("Transfer successful. Your new balance is " + balance);
                                }
                                break;
                            case 5:
                                exit = true;
                                break;
                            default:
                                System.out.println("Invalid option");
                        }

                    }
                }
            }

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Include it in your library path ");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Exception. Check SQL syntax or connection settings.");
            e.printStackTrace();
        } finally {
            
            try {
                if (db != null) db.close();
                if (scanner != null) scanner.close();
            } catch (SQLException e) {
                System.err.println("Failed to close resources");
                e.printStackTrace();
            }
        }
    }

    private static void insertTransaction(DatabaseHandler db, String cardId, String recipientCardId, double amount, double balance, boolean isTransfer) {
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
                System.out.println("User or recipient does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert transaction");
            e.printStackTrace();
        }
    }
}