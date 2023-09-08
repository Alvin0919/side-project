package banking_system4;

import java.sql.*;

public class DatabaseHandler {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/banking_system";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private Connection conn;
    private Statement st;

    public DatabaseHandler() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
        st = conn.createStatement();
    }

    public ResultSet executeQuery(String query) throws SQLException {
        return st.executeQuery(query);
    }

    public int executeUpdate(String query) throws SQLException {
        return st.executeUpdate(query);
    }

    public void close() throws SQLException {
        st.close();
        conn.close();
    }
}