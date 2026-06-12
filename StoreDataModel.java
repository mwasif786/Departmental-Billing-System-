import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Singleton data model — loads live data from MySQL and notifies
 * DashboardFrame every time something changes.
 *
 * bills table columns: bill_id, customer_name, customer_phone, grand_total, bill_date
 */
public class StoreDataModel {

    // ── Singleton ─────────────────────────────────────────────────────────
    private static final StoreDataModel INSTANCE = new StoreDataModel();
    public static StoreDataModel get() { return INSTANCE; }

    // ── Change listeners ──────────────────────────────────────────────────
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    public void addListener(Runnable r)    { listeners.add(r); }
    public void removeListener(Runnable r) { listeners.remove(r); }
    public void notifyListeners()          { listeners.forEach(Runnable::run); }

    // ── Auto-refresh every 10 seconds ─────────────────────────────────────
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "StoreDataModel-refresh");
                t.setDaemon(true);
                return t;
            });

    private StoreDataModel() {
        scheduler.scheduleAtFixedRate(this::notifyListeners, 10, 10, TimeUnit.SECONDS);
    }

    public static String pkr(double amount) {
        return String.format("PKR %,.0f", amount);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  KPI methods
    // ══════════════════════════════════════════════════════════════════════

    public double getTodaySales() {
        return queryDouble(
                "SELECT IFNULL(SUM(grand_total), 0) FROM bills " +
                        "WHERE DATE(bill_date) = CURDATE()"
        );
    }

    public int getTodayCustomers() {
        return queryInt(
                "SELECT COUNT(*) FROM bills " +
                        "WHERE DATE(bill_date) = CURDATE()"
        );
    }

    public int getTodayItemCount() {
        if (tableExists("bill_items")) {
            return queryInt(
                    "SELECT IFNULL(SUM(bi.quantity), 0) " +
                            "FROM bill_items bi " +
                            "JOIN bills b ON b.bill_id = bi.bill_id " +
                            "WHERE DATE(b.bill_date) = CURDATE()"
            );
        }
        if (tableExists("items")) {
            return queryInt(
                    "SELECT IFNULL(SUM(quantity), 0) FROM items"
            );
        }
        // Last resort: just count today's bills
        return queryInt(
                "SELECT COUNT(*) FROM bills WHERE DATE(bill_date) = CURDATE()"
        );
    }

    public double getTodayRevenue() {
        return getTodaySales();
    }

    public double getMonthSales() {
        return queryDouble(
                "SELECT IFNULL(SUM(grand_total), 0) FROM bills " +
                        "WHERE YEAR(bill_date) = YEAR(CURDATE()) " +
                        "  AND MONTH(bill_date) = MONTH(CURDATE())"
        );
    }

    public String getTopProduct() {
        String result = null;
        if (tableExists("bill_items")) {
            result = queryString(
                    "SELECT item_name FROM bill_items " +
                            "GROUP BY name ORDER BY SUM(quantity) DESC LIMIT 1"
            );
        }
        if (result == null && tableExists("items")) {
            result = queryString(
                    "SELECT item_name FROM items " +
                            "GROUP BY name ORDER BY SUM(quantity) DESC LIMIT 1"
            );
        }
        return result != null ? result : "N/A";
    }

    /** Sales totals for last 7 days. Returns double[7] Mon=0 … Sun=6. */
    public double[] getWeeklySales() {
        double[] totals = new double[7];
        String sql =
                "SELECT DAYOFWEEK(bill_date) AS dow, " +
                        "       IFNULL(SUM(grand_total), 0) AS total " +
                        "FROM bills " +
                        "WHERE bill_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                        "GROUP BY DAYOFWEEK(bill_date)";

        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                int dow  = rs.getInt("dow");        // 1=Sun, 2=Mon … 7=Sat
                double v = rs.getDouble("total");
                int idx  = (dow == 1) ? 6 : dow - 2; // Mon=0 … Sun=6
                if (idx >= 0 && idx < 7) totals[idx] = v;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return totals;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Transaction list
    // ══════════════════════════════════════════════════════════════════════

    public static class Transaction {
        public final int           id;
        public final String        category;   // customer name used here
        public final double        amount;
        public final int           itemCount;
        public final String        status;
        public final LocalDateTime time;

        Transaction(int id, String category, double amount,
                    int itemCount, String status, LocalDateTime time) {
            this.id        = id;
            this.category  = category;
            this.amount    = amount;
            this.itemCount = itemCount;
            this.status    = status;
            this.time      = time;
        }
    }

    public List<Transaction> getTransactions() {
        return loadTransactions(Integer.MAX_VALUE);
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return loadTransactions(limit);
    }

    private List<Transaction> loadTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql =
                "SELECT bill_id, customer_name, grand_total, bill_date " +
                        "FROM bills " +
                        "ORDER BY bill_date DESC" +
                        (limit < Integer.MAX_VALUE ? " LIMIT " + limit : "");

        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                int    id    = rs.getInt("bill_id");
                String name  = rs.getString("customer_name");
                double total = rs.getDouble("grand_total");
                Timestamp ts = rs.getTimestamp("bill_date");
                LocalDateTime time = (ts != null)
                        ? ts.toLocalDateTime()
                        : LocalDateTime.now();

                int itemCount = getItemCountForBill(id);
                list.add(new Transaction(id, name, total, itemCount, "Paid", time));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private int getItemCountForBill(int billId) {
        if (tableExists("bill_items")) {
            return queryInt(
                    "SELECT IFNULL(SUM(quantity), 0) FROM bill_items WHERE bill_id = " + billId
            );
        }
        return 1;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Product / Inventory list
    // ══════════════════════════════════════════════════════════════════════

    public static class Product {
        public final String name, category;
        public final double price;
        public final int    stock;

        Product(String name, String category, double price, int stock) {
            this.name     = name;
            this.category = category;
            this.price    = price;
            this.stock    = stock;
        }

        public String status() {
            if (stock <= 0) return "Out of Stock";
            if (stock <= 5) return "Low Stock";
            return "In Stock";
        }
    }

    public List<Product> getProducts() {
        List<Product> list = new ArrayList<>();

        if (tableExists("products")) {
            String sql = "SELECT name, category, price, stock FROM products ORDER BY name";
            try (Connection con = DBConnection.getConnection();
                 Statement  st  = con.createStatement();
                 ResultSet  rs  = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Product(
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getInt("stock")
                    ));
                }
            } catch (Exception e) { e.printStackTrace(); }

        } else if (tableExists("items")) {
            // items table: id, item_name, price, quantity, subtotal
            String sql =
                    "SELECT item_name, AVG(price) AS price, SUM(quantity) AS stock " +
                            "FROM items GROUP BY item_name ORDER BY item_name";
            try (Connection con = DBConnection.getConnection();
                 Statement  st  = con.createStatement();
                 ResultSet  rs  = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Product(
                            rs.getString("item_name"),
                            "General",
                            rs.getDouble("price"),
                            rs.getInt("stock")
                    ));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Private DB helpers
    // ══════════════════════════════════════════════════════════════════════

    private double queryDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private int queryInt(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private String queryString(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            if (rs.next()) return rs.getString(1);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private boolean tableExists(String tableName) {
        try (Connection con = DBConnection.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (Exception e) { return false; }
    }
}