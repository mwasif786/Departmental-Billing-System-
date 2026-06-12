import java.sql.*;

public class ItemDAO {

    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS items (" +
                "  id          INT AUTO_INCREMENT PRIMARY KEY," +
                "  item_name   VARCHAR(100) NOT NULL," +
                "  price       DOUBLE       NOT NULL," +
                "  quantity    INT          NOT NULL," +
                "  subtotal    DOUBLE       NOT NULL," +
                "  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) { System.err.println("❌ No DB connection."); return; }
            con.prepareStatement(sql).executeUpdate();
            System.out.println("✅ Table 'items' is ready.");
            con.close();
        } catch (SQLException e) {
            System.err.println("❌ Failed to create items table: " + e.getMessage());
        }
    }

    public static boolean addItem(String name, double price, int quantity, double subtotal) {
        if (name == null || name.trim().isEmpty()) { System.err.println("❌ Item name empty."); return false; }
        if (price <= 0 || quantity <= 0)           { System.err.println("❌ Invalid price/qty."); return false; }

        Connection con = DBConnection.getConnection();
        if (con == null) { System.err.println("❌ No DB connection."); return false; }

        String sql = "INSERT INTO items (item_name, price, quantity, subtotal) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name.trim());
            ps.setDouble(2, price);
            ps.setInt(3, quantity);
            ps.setDouble(4, subtotal);
            int rows = ps.executeUpdate();
            if (rows > 0) { System.out.println("✅ Item saved → " + name); return true; }
            else { System.err.println("❌ Insert ran but 0 rows affected."); return false; }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error saving item: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState() + " | Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } finally {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }
}