import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardFrame extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────
    private static final Color BG    = new Color(10, 14, 26);
    private static final Color SIDE  = new Color(15, 20, 40);
    private static final Color CARD  = new Color(22, 30, 58);
    private static final Color BORD  = new Color(40, 55, 90);
    private static final Color BLUE  = new Color(99, 179, 237);
    private static final Color MINT  = new Color(72, 235, 174);
    private static final Color FG    = new Color(225, 230, 245);
    private static final Color MUTED = new Color(110, 130, 170);
    private static final Color AMBER = new Color(246, 173, 85);
    private static final Color RED   = new Color(252, 129, 129);

    // ── Live KPI labels (updated by data listener) ────────────────────────
    private JLabel lblTotalSales, lblItemsSold, lblCustomers, lblRevenue;

    // ── Chart & transactions panels (rebuilt on refresh) ──────────────────
    private ChartPanel  chartPanel;
    private JPanel      txListPanel;

    private JPanel  content;
    private JButton activeBtn;

    // ── Data listener handle so we can unregister on dispose ─────────────
    private final Runnable dataListener = this::onDataChanged;

    public DashboardFrame(String role) {
        setTitle("Departmental Store POS");
        setSize(1150, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildTopBar(role), BorderLayout.NORTH);
        add(buildSideMenu(),   BorderLayout.WEST);

        content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(content, BorderLayout.CENTER);

        StoreDataModel.get().addListener(dataListener);
        showHome();
        setVisible(true);
    }

    @Override
    public void dispose() {
        StoreDataModel.get().removeListener(dataListener);
        super.dispose();
    }

    // ── Called every time StoreDataModel changes ──────────────────────────
    private void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            StoreDataModel dm = StoreDataModel.get();
            if (lblTotalSales != null) {
                lblTotalSales.setText(StoreDataModel.pkr(dm.getTodaySales()));
                lblItemsSold .setText(String.valueOf(dm.getTodayItemCount()));
                lblCustomers .setText(String.valueOf(dm.getTodayCustomers()));
                lblRevenue   .setText(StoreDataModel.pkr(dm.getTodayRevenue()));
            }
            if (chartPanel  != null) chartPanel.repaint();
            if (txListPanel != null) refreshTxList();
        });
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────
    private JPanel buildTopBar(String role) {
        JPanel bar = filledPanel(new BorderLayout(), SIDE);
        bar.setPreferredSize(new Dimension(0, 58));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));
        bar.add(lbl("⬡  DEPARTMENTAL STORE POS", 17, Font.BOLD, BLUE), BorderLayout.WEST);
        JLabel badge = lbl("  " + role.toUpperCase() + "  ", 11, Font.BOLD, BLUE);
        badge.setOpaque(true);
        badge.setBackground(new Color(99, 179, 237, 40));
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        bar.add(badge, BorderLayout.EAST);
        return bar;
    }

    // ── SIDE MENU ─────────────────────────────────────────────────────────
    private JPanel buildSideMenu() {
        JPanel side = filledPanel(new BorderLayout(), SIDE);
        side.setPreferredSize(new Dimension(210, 0));
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(18, 10, 18, 10));
        nav.add(lbl("NAVIGATION", 9, Font.BOLD, MUTED));
        nav.add(Box.createVerticalStrut(12));

        JButton db  = navBtn("⊞   Dashboard");
        JButton bi  = navBtn("⊟   Billing");
        JButton inv = navBtn("▤   Inventory");
        JButton rep = navBtn("◈   Reports");
        JButton lo  = navBtn("⏻   Logout");
        lo.setForeground(RED);

        for (JButton b : new JButton[]{db, bi, inv, rep}) {
            nav.add(b); nav.add(Box.createVerticalStrut(4));
        }
        nav.add(Box.createVerticalGlue());
        nav.add(lo);

        db .addActionListener(e -> { setActive(db);  showHome();      });
        bi .addActionListener(e -> { setActive(bi);  showBilling();   });
        inv.addActionListener(e -> { setActive(inv); showInventory(); });
        rep.addActionListener(e -> { setActive(rep); showReports();   });
        lo .addActionListener(e -> { StoreDataModel.get().removeListener(dataListener); dispose(); new LoginFrame(); });

        setActive(db);
        side.add(nav, BorderLayout.CENTER);
        return side;
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (activeBtn == this) {
                    g2.setColor(new Color(99, 179, 237, 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(BLUE);
                    g2.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);
                } else if (hovered) {
                    g2.setColor(new Color(30, 42, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(FG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 10));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    private void setActive(JButton b) { activeBtn = b; repaint(); }

    // ── DASHBOARD HOME ─────────────────────────────────────────────────────
    private void showHome() {
        content.removeAll();
        lblTotalSales = null; lblItemsSold = null;
        lblCustomers  = null; lblRevenue   = null;
        chartPanel    = null; txListPanel  = null;

        StoreDataModel dm = StoreDataModel.get();

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        root.add(lbl("Dashboard Overview", 22, Font.BOLD, FG));
        root.add(Box.createVerticalStrut(4));
        root.add(lbl("Here's what's happening today.", 12, Font.PLAIN, MUTED));
        root.add(Box.createVerticalStrut(22));

        // ── KPI cards ──────────────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cards.setAlignmentX(LEFT_ALIGNMENT);

        lblTotalSales = lbl(StoreDataModel.pkr(dm.getTodaySales()),    24, Font.BOLD, FG);
        lblItemsSold  = lbl(String.valueOf(dm.getTodayItemCount()),     24, Font.BOLD, FG);
        lblCustomers  = lbl(String.valueOf(dm.getTodayCustomers()),     24, Font.BOLD, FG);
        lblRevenue    = lbl(StoreDataModel.pkr(dm.getTodayRevenue()),   24, Font.BOLD, FG);

        cards.add(kpiCard("Total Sales",  lblTotalSales, BLUE));
        cards.add(kpiCard("Items Sold",   lblItemsSold,  MINT));
        cards.add(kpiCard("Customers",    lblCustomers,  AMBER));
        cards.add(kpiCard("Revenue",      lblRevenue,    RED));
        root.add(cards);
        root.add(Box.createVerticalStrut(22));

        // ── Chart + transactions row ────────────────────────────────────────
        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setOpaque(false);
        bottom.setAlignmentX(LEFT_ALIGNMENT);

        chartPanel  = new ChartPanel();
        txListPanel = buildTxListPanel();

        bottom.add(chartPanel);
        bottom.add(txListPanel);
        root.add(bottom);

        content.add(root, BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    // ── KPI card — accepts an external JLabel so it can be updated live ───
    private JPanel kpiCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORD); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.setColor(accent); g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(18, 1, getWidth()-18, 1);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.add(lbl(title, 11, Font.PLAIN, MUTED), BorderLayout.NORTH);
        card.add(valueLabel,                         BorderLayout.CENTER);
        card.add(lbl("Live data", 10, Font.PLAIN, MINT), BorderLayout.SOUTH);
        return card;
    }

    // ── Weekly Sales Chart (reads live data) ─────────────────────────────
    private class ChartPanel extends JPanel {
        private static final String[] DAYS = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

        ChartPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 8, 16));
            setLayout(new BorderLayout());
            add(lbl("Weekly Sales", 13, Font.BOLD, FG), BorderLayout.NORTH);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background card
            g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(BORD); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

            double[] raw = StoreDataModel.get().getWeeklySales();

            // find max for scaling
            double max = 0;
            for (double v : raw) if (v > max) max = v;
            if (max == 0) max = 1; // avoid /0

            int pL=36, pB=28, pT=44, pR=16;
            int cW = getWidth() - pL - pR;
            int cH = getHeight() - pB - pT;
            int bW = cW / raw.length - 8;

            // baseline
            g2.setColor(BORD);
            g2.drawLine(pL, pT + cH, getWidth() - pR, pT + cH);

            for (int i = 0; i < raw.length; i++) {
                int bH = (int)(cH * raw[i] / max);
                if (bH < 2) bH = 2;           // always draw at least a stub
                int x = pL + i * (cW / raw.length) + 4;
                int y = pT + cH - bH;

                g2.setPaint(new GradientPaint(x, y, BLUE, x, y + bH, new Color(99, 179, 237, 40)));
                g2.fillRoundRect(x, y, bW, bH, 5, 5);

                // day label
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(MUTED);
                g2.drawString(DAYS[i], x + bW / 2 - 10, getHeight() - 10);

                // value label on top of bar (if non-zero)
                if (raw[i] > 0) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                    g2.setColor(FG);
                    String val = raw[i] >= 1000
                            ? String.format("%.0fk", raw[i] / 1000)
                            : String.format("%.0f", raw[i]);
                    g2.drawString(val, x + bW / 2 - 8, y - 3);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Transactions panel (wrapper with list inside) ─────────────────────
    private JPanel buildTxListPanel() {
        JPanel panel = filledPanel(new BorderLayout(), CARD);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        panel.add(lbl("Recent Transactions", 13, Font.BOLD, FG), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(10, 0, 0, 0));
        list.setName("TX_LIST");

        fillTxRows(list);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /** Populate (or repopulate) the transaction rows inside the list panel. */
    private void fillTxRows(JPanel list) {
        list.removeAll();
        List<StoreDataModel.Transaction> recent = StoreDataModel.get().getRecentTransactions(8);

        if (recent.isEmpty()) {
            JLabel empty = lbl("No transactions yet today.", 12, Font.ITALIC, MUTED);
            empty.setAlignmentX(LEFT_ALIGNMENT);
            list.add(empty);
        } else {
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            for (StoreDataModel.Transaction t : recent) {
                Color sc = t.status.equals("Paid") ? MINT
                        : t.status.equals("Pending") ? AMBER : RED;

                JPanel r = filledPanel(new BorderLayout(), new Color(30, 42, 70));
                r.setBorder(new EmptyBorder(7, 10, 7, 10));
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

                JPanel L = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));  L.setOpaque(false);
                JPanel R = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); R.setOpaque(false);

                L.add(lbl("#" + t.id, 11, Font.PLAIN, BLUE));
                L.add(lbl(t.category, 12, Font.PLAIN, FG));
                L.add(lbl(t.time.format(timeFmt), 10, Font.PLAIN, MUTED));

                R.add(lbl(StoreDataModel.pkr(t.amount), 12, Font.BOLD, FG));
                R.add(lbl(t.status, 10, Font.BOLD, sc));

                r.add(L, BorderLayout.WEST);
                r.add(R, BorderLayout.EAST);
                list.add(r);
                list.add(Box.createVerticalStrut(5));
            }
        }
        list.revalidate(); list.repaint();
    }

    /** Refresh only the transaction list inside the existing panel (no full rebuild). */
    private void refreshTxList() {
        if (txListPanel == null) return;
        // find the scroll pane → viewport → list panel
        for (Component c : txListPanel.getComponents()) {
            if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                Component view = sp.getViewport().getView();
                if (view instanceof JPanel) {
                    fillTxRows((JPanel) view);
                }
                break;
            }
        }
    }

    // ── BILLING ────────────────────────────────────────────────────────────
    private void showBilling() {
        content.removeAll();
        clearDashboardRefs();
        JPanel wrap = new JPanel(new BorderLayout(0, 14)); wrap.setOpaque(false);
        wrap.add(lbl("Billing", 22, Font.BOLD, FG), BorderLayout.NORTH);
        wrap.add(new BillingPanel(), BorderLayout.CENTER);
        content.add(wrap, BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    // ── INVENTORY ──────────────────────────────────────────────────────────
    private void showInventory() {
        content.removeAll();
        clearDashboardRefs();

        JPanel wrap = new JPanel(new BorderLayout(0, 14)); wrap.setOpaque(false);
        wrap.add(lbl("Inventory", 22, Font.BOLD, FG), BorderLayout.NORTH);

        // Live data from model
        List<StoreDataModel.Product> prods = StoreDataModel.get().getProducts();
        String[][] rows = new String[prods.size()][6];
        for (int i = 0; i < prods.size(); i++) {
            StoreDataModel.Product p = prods.get(i);
            rows[i] = new String[]{
                    String.valueOf(i + 1),
                    p.name, p.category,
                    StoreDataModel.pkr(p.price),
                    String.valueOf(p.stock),
                    p.status()
            };
        }
        String[] cols = {"#", "Product Name", "Category", "Price", "Stock", "Status"};

        DefaultTableModel inv = new DefaultTableModel(rows, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(inv);
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                String val = v.toString();
                setForeground(val.equals("In Stock") ? MINT : val.equals("Low Stock") ? AMBER : RED);
                setBackground(s ? new Color(99,179,237,50) : (r%2==0 ? CARD : new Color(28,38,70)));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORD));

        JPanel tableCard = filledPanel(new BorderLayout(0, 10), CARD);
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        tableCard.add(lbl("Stock Overview — " + prods.size() + " Products", 13, Font.BOLD, FG), BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);
        wrap.add(tableCard, BorderLayout.CENTER);
        content.add(wrap, BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    // ── REPORTS ────────────────────────────────────────────────────────────
    private void showReports() {
        content.removeAll();
        clearDashboardRefs();

        StoreDataModel dm = StoreDataModel.get();
        JPanel wrap = new JPanel(new BorderLayout(0, 14)); wrap.setOpaque(false);
        wrap.add(lbl("Reports", 22, Font.BOLD, FG), BorderLayout.NORTH);

        // Summary cards — all live
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cards.add(reportCard("Today's Sales",  StoreDataModel.pkr(dm.getTodaySales()),   "Live data",           BLUE));
        cards.add(reportCard("This Month",     StoreDataModel.pkr(dm.getMonthSales()),   "Cumulative (Paid)",   MINT));
        cards.add(reportCard("Top Category",   dm.getTopProduct(),                       "By units sold",       AMBER));

        // Transactions table — all live
        List<StoreDataModel.Transaction> txList = dm.getTransactions();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        String[][] rdata = new String[txList.size()][5];
        for (int i = 0; i < txList.size(); i++) {
            StoreDataModel.Transaction t = txList.get(txList.size() - 1 - i); // newest first
            rdata[i] = new String[]{
                    t.time.format(fmt),
                    t.category,
                    String.valueOf(t.itemCount),
                    "—",
                    StoreDataModel.pkr(t.amount)
            };
        }
        String[] rcols = {"Date & Time", "Category", "Items", "Unit Price", "Total"};
        JTable rtable = styledTable(new DefaultTableModel(rdata, rcols) {
            public boolean isCellEditable(int r, int c) { return false; }
        });

        JScrollPane scroll = new JScrollPane(rtable);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORD));

        JPanel tableCard = filledPanel(new BorderLayout(0, 10), CARD);
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        String header = txList.isEmpty()
                ? "No transactions recorded yet"
                : "Sales Transactions Log — " + txList.size() + " total";
        tableCard.add(lbl(header, 13, Font.BOLD, FG), BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout(0, 16)); main.setOpaque(false);
        main.add(cards, BorderLayout.NORTH);
        main.add(tableCard, BorderLayout.CENTER);
        wrap.add(main, BorderLayout.CENTER);
        content.add(wrap, BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    private JPanel reportCard(String title, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(BORD); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.setColor(accent); g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(18,1,getWidth()-18,1);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.add(lbl(title, 11, Font.PLAIN, MUTED),  BorderLayout.NORTH);
        card.add(lbl(value, 18, Font.BOLD,  FG),     BorderLayout.CENTER);
        card.add(lbl(sub,   10, Font.PLAIN, accent),  BorderLayout.SOUTH);
        return card;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(CARD); t.setForeground(FG);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(30); t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.getTableHeader().setBackground(SIDE);
        t.getTableHeader().setForeground(MUTED);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setSelectionBackground(new Color(99, 179, 237, 50));
        t.setSelectionForeground(FG);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, s, f, r, c);
                setBackground(s ? new Color(99,179,237,50) : (r%2==0 ? CARD : new Color(28,38,70)));
                setForeground(FG); setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setBorder(new EmptyBorder(0, 10, 0, 10)); return this;
            }
        });
        return t;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void clearDashboardRefs() {
        lblTotalSales = null; lblItemsSold = null;
        lblCustomers  = null; lblRevenue   = null;
        chartPanel    = null; txListPanel  = null;
    }

    private JLabel lbl(String t, int size, int style, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(c);
        return l;
    }

    private JPanel filledPanel(LayoutManager layout, Color bg) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.dispose(); super.paintComponent(g);
            }
        };
        p.setOpaque(false); return p;
    }

    // ── Stub LoginFrame — remove if yours already exists ──────────────────
    static class LoginFrame extends JFrame {
        LoginFrame() {
            setTitle("Login");
            setSize(400, 300);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardFrame("Admin"));
    }
}