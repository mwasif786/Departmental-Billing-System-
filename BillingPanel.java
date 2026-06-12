import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillingPanel extends JPanel {

    private static final Color BG   = new Color(10, 14, 26);
    private static final Color CARD = new Color(22, 30, 58);
    private static final Color SIDE = new Color(15, 20, 40);
    private static final Color BORD = new Color(40, 55, 90);
    private static final Color BLUE = new Color(99, 179, 237);
    private static final Color MINT = new Color(72, 235, 174);
    private static final Color FG   = new Color(225, 230, 245);
    private static final Color MUTED= new Color(110, 130, 170);
    private static final Color RED  = new Color(252, 129, 129);

    private JTextField customerField;
    private JTextField phoneField;
    private JTextField itemField, priceField, qtyField, discField, taxField;
    private DefaultTableModel model;
    private JTextArea billArea;

    public BillingPanel() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildTablePanel(),  BorderLayout.CENTER);
        add(buildBillArea(),    BorderLayout.EAST);
    }

    // ── INPUT FORM ────────────────────────────────────────────────────────
    private JPanel buildInputPanel() {
        JPanel outer = darkCard(new BorderLayout(0, 10));
        outer.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = lbl("Add Item to Bill", 14, Font.BOLD, FG);
        outer.add(title, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(2, 7, 10, 8));
        fields.setOpaque(false);
        fields.add(lbl("Customer Name",11,Font.BOLD,MUTED));
        fields.add(lbl("Phone",11,Font.BOLD,MUTED));
        fields.add(lbl("Item Name",11,Font.BOLD,MUTED));
        fields.add(lbl("Price (PKR)",11,Font.BOLD,MUTED));
        fields.add(lbl("Quantity",11,Font.BOLD,MUTED));
        fields.add(lbl("Discount %",11,Font.BOLD,MUTED));
        fields.add(lbl("Tax %",11,Font.BOLD,MUTED));

        customerField = styledField("Customer Name");
        phoneField = styledField("03XXXXXXXXX");

        itemField  = styledField("e.g. Rice 1kg");
        priceField = styledField("e.g. 200");
        qtyField   = styledField("e.g. 2");
        discField  = styledField("0");
        taxField   = styledField("0");

        fields.add(customerField);
        fields.add(phoneField);

        fields.add(itemField);
        fields.add(priceField);
        fields.add(qtyField);
        fields.add(discField);
        fields.add(taxField);
        outer.add(fields, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        JButton addBtn   = btn("＋  Add Item",       BLUE);
        JButton billBtn  = btn("🖨  Generate Bill",   MINT);
        JButton clearBtn = btn("✕  Clear All",        RED);

        addBtn.addActionListener(e   -> addItem());
        billBtn.addActionListener(e  -> generateBill());
        clearBtn.addActionListener(e -> clearAll());

        btns.add(addBtn); btns.add(billBtn); btns.add(clearBtn);
        outer.add(btns, BorderLayout.SOUTH);
        return outer;
    }

    // ── TABLE ─────────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        model = new DefaultTableModel(new String[]{"#","Item","Price","Qty","Discount","Tax","Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(CARD);
        table.setForeground(FG);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(SIDE);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        table.setSelectionBackground(new Color(99, 179, 237, 50));
        table.setSelectionForeground(FG);

        // Alternate row color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? new Color(99,179,237,50) : (r%2==0 ? CARD : new Color(28,38,70)));
                setForeground(FG);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORD));

        JPanel panel = darkCard(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        panel.add(lbl("Bill Items", 13, Font.BOLD, FG), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── BILL RECEIPT AREA ─────────────────────────────────────────────────
    private JPanel buildBillArea() {
        billArea = new JTextArea();
        billArea.setBackground(new Color(8, 12, 22));
        billArea.setForeground(MINT);
        billArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        billArea.setEditable(false);
        billArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        billArea.setText("  Receipt will appear here\n  after generating bill...");

        JScrollPane scroll = new JScrollPane(billArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORD));
        scroll.setPreferredSize(new Dimension(260, 0));

        JPanel panel = darkCard(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(275, 0));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        panel.add(lbl("Receipt Preview", 13, Font.BOLD, FG), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── LOGIC ─────────────────────────────────────────────────────────────
    private void addItem() {
        String item = itemField.getText().trim();
        String priceText = priceField.getText().trim();
        String qtyText = qtyField.getText().trim();
        String discText = discField.getText().trim();
        String taxText = taxField.getText().trim();

        if (item.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
            showError("Please fill Item, Price and Quantity fields.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int qty = Integer.parseInt(qtyText);
            double disc = discText.isEmpty() ? 0 : Double.parseDouble(discText);
            double tax  = taxText.isEmpty()  ? 0 : Double.parseDouble(taxText);

            if (price <= 0 || qty <= 0) { showError("Price and Qty must be greater than 0."); return; }

            double sub = price * qty;
            double discAmt = sub * disc / 100;
            double taxAmt  = (sub - discAmt) * tax / 100;
            double total   = sub - discAmt + taxAmt;

            int rowNum = model.getRowCount() + 1;
            model.addRow(new Object[]{
                    rowNum,
                    item,
                    String.format("PKR %.2f", price),
                    qty,
                    String.format("%.1f%%", disc),
                    String.format("%.1f%%", tax),
                    String.format("PKR %.2f", total)
            });

            // Save to database
            boolean saved = ItemDAO.addItem(item, price, qty, total);
            if (!saved) {
                JOptionPane.showMessageDialog(this,
                        "Item added to bill but NOT saved to database.\nCheck console for error details.",
                        "Database Warning", JOptionPane.WARNING_MESSAGE);
            }

            itemField.setText(""); priceField.setText(""); qtyField.setText("");

        } catch (NumberFormatException ex) {
            showError("Price and Qty must be valid numbers.");
        }
    }

    private void generateBill() {

        String customerName = customerField.getText().trim();
        String customerPhone = phoneField.getText().trim();

        if(customerName.isEmpty()) {
            showError("Enter Customer Name");
            return;
        }

        if (model.getRowCount() == 0) {
            showError("No items added yet. Add items first.");
            return;
        }

        double grandTotal = 0;

        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════╗\n");
        sb.append("║   DEPARTMENTAL STORE     ║\n");
        sb.append("╚══════════════════════════╝\n");

        sb.append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                .append("\n");

        sb.append("Customer : ")
                .append(customerName)
                .append("\n");

        sb.append("Phone    : ")
                .append(customerPhone)
                .append("\n");

        sb.append("─────────────────────────────\n");

        for (int i = 0; i < model.getRowCount(); i++) {

            String name = model.getValueAt(i, 1).toString();
            String qty = model.getValueAt(i, 3).toString();
            String subtotal = model.getValueAt(i, 6).toString();

            double sub =
                    Double.parseDouble(
                            subtotal.replace("PKR ", "")
                    );

            grandTotal += sub;

            String display =
                    name.length() > 14
                            ? name.substring(0, 13) + "."
                            : name;

            sb.append(String.format(
                    "%-15s x%s\n  → %s\n",
                    display,
                    qty,
                    subtotal
            ));
        }

        sb.append("─────────────────────────────\n");
        sb.append(String.format("GRAND TOTAL : PKR %.2f\n", grandTotal));
        sb.append("─────────────────────────────\n");
        sb.append("      Thank You! Visit Again\n");

        billArea.setText(sb.toString());

        // Save bill in database
        boolean saved = BillDAO.addBill(
                customerName,
                customerPhone,
                grandTotal
        );

        if(saved) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bill Saved Successfully!"
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Bill Save Failed!"
            );
        }
    }

    private void clearAll() {
        model.setRowCount(0);
        billArea.setText("  Receipt will appear here\n  after generating bill...");
        itemField.setText(""); priceField.setText("");
        qtyField.setText(""); discField.setText("0"); taxField.setText("0");
        customerField.setText("");
        phoneField.setText("");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private JLabel lbl(String t, int size, int style, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(c);
        return l;
    }

    private JTextField styledField(String hint) {
        JTextField f = new JTextField();
        f.setBackground(new Color(30, 42, 70));
        f.setForeground(FG);
        f.setCaretColor(BLUE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORD),
                new EmptyBorder(6, 10, 6, 10)
        ));
        f.setToolTipText(hint);
        return f;
    }

    private JButton btn(String text, Color accent) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(accent);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel darkCard(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }
}