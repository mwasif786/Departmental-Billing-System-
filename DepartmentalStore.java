import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


class Item {
    String name;
    double price;
    int quantity;
    double subtotal;

    public Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }

    public void updateQuantity(int newQty) {
        this.quantity = newQty;
        this.subtotal = this.price * newQty;
    }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
        this.subtotal = newPrice * this.quantity;
    }
}


public class DepartmentalStore {
    static Scanner sc = new Scanner(System.in);
    static List<Item> cart = new ArrayList<>();
    static String customerName, customerPhone, paymentMethod = "Not Selected";
    static double discountPercent = 0, taxRate = 0;

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println(" WELCOME TO DEPARTMENTAL STORE");
            System.out.println("=".repeat(50));
            System.out.println("1. Admin\n2. Cashier\n3. Exit");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                if (login("admin", "wasif123")) runMenu(true);
            } else if (choice.equals("2")) {
                if (login("cashier", "cashier123")) runMenu(false);
            } else if (choice.equals("3")) {
                System.out.println("Exiting System...");
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
    }

    private static boolean login(String u, String p) {
        System.out.print("Enter Username: ");
        String user = sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        if (user.equals(u) && pass.equals(p)) {
            System.out.println("\n*Logged in Successfully*");
            getCustomerDetails();
            return true;
        }
        System.out.println("*Wrong Credentials*");
        return false;
    }

    private static void getCustomerDetails() {
        System.out.println("\n--- CUSTOMER DETAILS ---");
        while (true) {
            System.out.print("Enter Name: ");
            customerName = sc.nextLine();
            if (customerName.matches("[a-zA-Z ]+")) break;
            System.out.println("Invalid name (Letters only).");
        }
        while (true) {
            System.out.print("Enter Phone (11 digits): ");
            customerPhone = sc.nextLine();
            if (customerPhone.length() == 11 && customerPhone.matches("\\d+")) break;
            System.out.println("Invalid phone (Need 11 digits).");
        }
    }

    private static void runMenu(boolean isAdmin) {
        while (true) {
            System.out.println("\n" + "-".repeat(20));
            System.out.println(" BILLING MENU");
            System.out.println("-".repeat(20));
            System.out.println("1. Add Item\n2. View Items\n3. Apply Discount\n4. Add Tax\n5. Payment Method\n6. Finalize Bill\n7. Clear Cart\n8. Logout");

            if (isAdmin) {
                System.out.println("9. Update Product Price\n10. Update Product Quantity");
            }

            System.out.print("Select an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> addItem();
                case "2" -> viewItems();
                case "3" -> applyDiscount();
                case "4" -> applyTax();
                case "5" -> setPayment();
                case "6" -> finalizeBill();
                case "7" -> { cart.clear(); System.out.println("Cart cleared."); }
                case "8" -> { return; }
                case "9" -> { if(isAdmin) updateItem(true); }
                case "10" -> { if(isAdmin) updateItem(false); }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addItem() {
        System.out.print("Item Name: ");
        String name = sc.nextLine();
        System.out.print("Price (PKR): ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(sc.nextLine());

        cart.add(new Item(name, price, qty));
        ItemDAO.addItem(
                name,
                price,
                qty,
                price * qty
        );
        System.out.println("Item added to cart.");
    }

    private static void viewItems() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        System.out.printf("\n%-5s %-15s %-10s %-5s %-10s\n", "No", "Item", "Price", "Qty", "Subtotal");
        for (int i = 0; i < cart.size(); i++) {
            Item item = cart.get(i);
            System.out.printf("%-5d %-15s %-10.2f %-5d %-10.2f\n", i + 1, item.name, item.price, item.quantity, item.subtotal);
        }
    }

    private static void applyDiscount() {
        System.out.print("Enter discount percentage (0-100): ");
        discountPercent = Double.parseDouble(sc.nextLine()) / 100;
        System.out.println("Discount applied.");
    }

    private static void applyTax() {
        System.out.print("Enter tax percentage: ");
        taxRate = Double.parseDouble(sc.nextLine());
        System.out.println("Tax rate updated.");
    }

    private static void setPayment() {
        System.out.println("1. Jazzcash\n2. Easypaisa\n3. Bank Transfer\n4. Cash");
        String p = sc.nextLine();
        paymentMethod = switch(p) {
            case "1" -> "Jazzcash";
            case "2" -> "Easypaisa";
            case "3" -> "Bank Transfer";
            default -> "Cash";
        };
        System.out.println("Payment set to: " + paymentMethod);
    }

    private static void updateItem(boolean isPrice) {
        viewItems();
        if (cart.isEmpty()) return;
        System.out.print("Enter item number to update: ");
        int idx = Integer.parseInt(sc.nextLine()) - 1;

        if (isPrice) {
            System.out.print("New Price: ");
            cart.get(idx).updatePrice(Double.parseDouble(sc.nextLine()));
        } else {
            System.out.print("New Quantity: ");
            cart.get(idx).updateQuantity(Integer.parseInt(sc.nextLine()));
        }
        System.out.println("Update successful.");
    }

    private static void finalizeBill() {
        if (cart.isEmpty()) {
            System.out.println("No items in cart.");
            return;
        }

        double total = 0;
        for (Item item : cart) total += item.subtotal;
        double discAmt = total * discountPercent;
        double taxableAmt = total - discAmt;
        double taxAmt = taxableAmt * (taxRate / 100);
        double grandTotal = taxableAmt + taxAmt;

        System.out.println("\n" + "=".repeat(40));
        System.out.println("       FINAL RECEIPT");
        System.out.println("=".repeat(40));
        System.out.println("Customer: " + customerName);
        System.out.println("Phone:    " + customerPhone);
        System.out.println("-".repeat(40));
        viewItems();
        System.out.println("-".repeat(40));
        System.out.printf("Subtotal:  PKR %.2f\n", total);
        System.out.printf("Discount:  PKR %.2f\n", discAmt);
        System.out.printf("Tax:       PKR %.2f\n", taxAmt);
        System.out.printf("TOTAL:     PKR %.2f\n", grandTotal);
        System.out.println("Payment:   " + paymentMethod);
        System.out.println("Time:      " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println("=".repeat(40));

        boolean saved = BillDAO.addBill(
                customerName,
                customerPhone,
                grandTotal
        );

        if(saved)
        {
            System.out.println("Bill Saved Successfully!");
        }
        else
        {
            System.out.println("Bill Save Failed!");
        }

        cart.clear();
        discountPercent = 0;
        taxRate = 0;

        cart.clear();
        discountPercent = 0;
        taxRate = 0;
    }
}