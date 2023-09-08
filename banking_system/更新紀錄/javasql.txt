package banking_system4;

import java.sql.*;

public class javasql {

    public static void main(String[] args) {

        String driver = "com.mysql.cj.jdbc.Driver";  
        String url = "jdbc:mysql://localhost:3306/banking_system";
        String user = "root";  
        String password = "1234"; 

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Include it in your library path ");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select * from users")) {

            System.out.println("Successfully connected to Bank_db database");

            while (rs.next()) {
                System.out.printf("%s\t", rs.getString("id"));
                System.out.printf("%s\t", rs.getString("card_id"));
                System.out.printf("%s\t", rs.getInt("balance"));
                System.out.printf("\n");
            }

            System.out.println("Successfully closed Bank_db database");
        } catch (SQLException e) {
            System.err.println("SQL Exception. Check SQL syntax or connection settings.");
            e.printStackTrace();
        }
    }
}

