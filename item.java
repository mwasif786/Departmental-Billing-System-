public class item {

    private String name;
    private double price;
    private int quantity;
    private double subtotal;

    public item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setPrice(double price) {
        this.price = price;
        this.subtotal = this.price * this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.price * this.quantity;
    }
}