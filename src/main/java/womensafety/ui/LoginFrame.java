package womensafety.ui;

import womensafety.model.User;
import womensafety.service.AppServices;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AppServices app;
    private final JTextField userId = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);

    public LoginFrame(AppServices app) {
        this.app = app;
        Ui.frame(this, "Women Safety | Sign in");
        setSize(520, 430);
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Ui.BG);
        JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 228, 234)),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));
        JLabel title = new JLabel("WOMEN SAFETY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Ui.NAVY);
        JLabel subtitle = new JLabel("SOS & Safe Route Helper", SwingConstants.CENTER);
        subtitle.setForeground(Ui.TEAL);
        form.add(title); form.add(subtitle);
        form.add(new JLabel("User ID / Phone")); form.add(userId);
        form.add(new JLabel("Password")); form.add(password);
        JButton login = Ui.button("Login");
        JButton register = Ui.button("Create account");
        form.add(login); form.add(register);
        root.add(form);
        add(root);
        login.addActionListener(e -> signIn());
        password.addActionListener(e -> signIn());
        register.addActionListener(e -> {
            new RegistrationFrame(app).setVisible(true);
            dispose();
        });
    }

    void prefillUserId(String value) { userId.setText(value); }

    private void signIn() {
        User user = app.users.login(userId.getText(), new String(password.getPassword()));
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid user ID or password.", "Sign in failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        user.setTrustedContacts(app.files.readContacts(user.getUserId()));
        app.locations.getCurrentLocation(user);
        new DashboardFrame(app, user).setVisible(true);
        dispose();
    }
}
