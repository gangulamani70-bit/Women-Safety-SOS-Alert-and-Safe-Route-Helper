package womensafety.ui;

import womensafety.exception.ValidationException;
import womensafety.model.User;
import womensafety.service.AppServices;

import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {
    private final AppServices app;
    private final JTextField name = new JTextField(22);
    private final JTextField phone = new JTextField(22);
    private final JTextField email = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);

    public RegistrationFrame(AppServices app) {
        this.app = app;
        Ui.frame(this, "Create account");
        setSize(500, 470);
        JPanel page = Ui.page("Create your account");
        JPanel fields = new JPanel(new GridLayout(0, 2, 12, 14));
        fields.setBackground(Ui.BG);
        fields.add(new JLabel("Name")); fields.add(name);
        fields.add(new JLabel("Phone number")); fields.add(phone);
        fields.add(new JLabel("Email")); fields.add(email);
        fields.add(new JLabel("Password")); fields.add(password);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Ui.BG);
        JButton register = Ui.button("Register");
        JButton back = Ui.button("Back to Login");
        buttons.add(register); buttons.add(back);
        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setBackground(Ui.BG);
        center.add(fields, BorderLayout.CENTER); center.add(buttons, BorderLayout.SOUTH);
        page.add(center, BorderLayout.CENTER);
        add(page);
        register.addActionListener(e -> register());
        back.addActionListener(e -> {
            new LoginFrame(app).setVisible(true);
            dispose();
        });
    }

    private void register() {
        try {
            User created = app.users.registerUser(name.getText(), phone.getText(), email.getText(), new String(password.getPassword()));
            JOptionPane.showMessageDialog(this, "Registration successful!\nYour user ID is " + created.getUserId());
            LoginFrame login = new LoginFrame(app);
            login.prefillUserId(created.getUserId());
            login.setVisible(true);
            dispose();
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Check your details", JOptionPane.WARNING_MESSAGE);
        }
    }
}
