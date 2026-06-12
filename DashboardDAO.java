import java.sql.*;

public class DashboardDAO {

    public static double getTotalSales() {
        return getDouble("SELECT IFNULL(SUM(grand_total),0) FROM bills");
    }

    public static int getTotalCustomers() {
        return getInt("SELECT COUNT(*) FROM customers");
    }

    public static int getItemsSold() {
        return getInt("SELECT IFNULL(SUM(quantity),0) FROM bill_items");
    }

    public static double getRevenue() {
        return getDouble("SELECT IFNULL(SUM(grand_total),0) FROM bills");
    }

    private static double getDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getInt(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}