import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/departmental_store";
    private static final String USER     = "root";
    private static final String PASSWORD = "mwasif@786";

    public static Connection getConnection() {
        try {
            // Explicitly load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            return con;
        } catch (ClassNotFoundException e) {
            System.err.println(" MySQL JDBC Driver not found in classpath!");
            System.err.println("   → Download mysql-connector-j-8.x.jar and add it to your project libraries.");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("   → Check: Is MySQL running? Is database name correct? Is password correct?");
            e.printStackTrace();
            return null;
        }
    }
}