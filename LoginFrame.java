import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    JTextField user;
    JPasswordField pass;

    public LoginFrame() {

        setTitle("Login - Departmental Store");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(20, 30, 60));

        JLabel title = new JLabel("STORE LOGIN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(150, 20, 200, 30);

        JLabel u = new JLabel("Username");
        u.setForeground(Color.WHITE);
        u.setBounds(70, 80, 100, 25);

        user = new JTextField();
        user.setBounds(160, 80, 200, 30);

        JLabel p = new JLabel("Password");
        p.setForeground(Color.WHITE);
        p.setBounds(70, 130, 100, 25);

        pass = new JPasswordField();
        pass.setBounds(160, 130, 200, 30);

        JButton login = new JButton("LOGIN");
        login.setBounds(160, 180, 200, 35);
        login.setBackground(new Color(0, 150, 136));
        login.setForeground(Color.WHITE);

        login.addActionListener(e -> checkLogin());

        panel.add(title);
        panel.add(u);
        panel.add(user);
        panel.add(p);
        panel.add(pass);
        panel.add(login);

        add(panel);
        setVisible(true);
    }

    void checkLogin() {

        String u = user.getText();
        String p = new String(pass.getPassword());

        if ((u.equals("admin") && p.equals("admin123")) ||
                (u.equals("cashier") && p.equals("cashier123"))) {

            dispose();
            new DashboardFrame(u);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Login");
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}