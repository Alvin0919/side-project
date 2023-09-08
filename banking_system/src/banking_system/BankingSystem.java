package banking_system4;
import java.util.Random;
import java.sql.SQLException;
import java.util.Scanner;

public class BankingSystem {

    public static void main(String[] args) {

        DatabaseHandler db = null;
        Scanner scanner = null;

        try {
            db = new DatabaseHandler();
            scanner = new Scanner(System.in);

            System.out.println("請輸入使用者 ID：");
            String id = scanner.nextLine();

            System.out.println("請輸入密碼：");
            String password = scanner.nextLine();

            
            String cardId = generateCardId();

            User user = new User(id, password, cardId);

            db.executeUpdate("INSERT INTO users (user_id, card_id, hashed_password, salt) VALUES ('" + user.getId() + "', '" + user.getCardId() + "', '" + user.getHashedPassword() + "', '" + user.getSalt() + "')");

            System.out.println("成功創建新使用者");

        } catch (ClassNotFoundException e) {
            System.err.println("未找到 MySQL JDBC 驅動程式。請確保在庫中包含它");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL 錯誤。請檢查 SQL 語法或連線設定");
            e.printStackTrace();
        } finally {
            
            try {
                if (db != null) db.close();
                if (scanner != null) scanner.close();
            } catch (SQLException e) {
                System.err.println("關閉資源失敗");
                e.printStackTrace();
            }
        }
    }

    

    private static String generateCardId() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 13; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }

        return sb.toString();
    	}
}
    