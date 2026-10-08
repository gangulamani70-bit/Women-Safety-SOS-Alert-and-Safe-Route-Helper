package womensafety.ui;

import womensafety.service.AppServices;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomeFrame extends JFrame {
    private final AppServices app;

    public HomeFrame(AppServices app) {
        this.app = app;
        Ui.frame(this, "Women Safety | Home");
        setSize(960, 680);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(22, 44, 18, 44));

        JLabel brand = new JLabel("WOMEN SAFETY");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 21));
        brand.setForeground(Ui.NAVY);
        JLabel subtitle = new JLabel("SOS & SAFE ROUTE HELPER");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(Ui.TEAL);
        JPanel brandBlock = new JPanel(new GridLayout(0, 1, 0, 3));
        brandBlock.setBackground(Color.WHITE);
        brandBlock.add(brand);
        brandBlock.add(subtitle);

        JButton signIn = Ui.button("Sign in");
        JButton createAccount = Ui.button("Create account");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);
        actions.add(signIn);
        actions.add(createAccount);
        header.add(brandBlock, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        signIn.addActionListener(e -> openLogin());
        createAccount.addActionListener(e -> {
            new RegistrationFrame(app).setVisible(true);
            dispose();
        });
        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(Ui.BG);
        content.setBorder(new EmptyBorder(36, 52, 36, 52));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("A SIMPLE OFFLINE SAFETY COMPANION");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eyebrow.setForeground(Ui.TEAL);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html>Feel more prepared<br>wherever you go.</html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(Ui.NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea description = new JTextArea(
                "Keep trusted contacts close, choose a sample location, and explore nearby places in one easy-to-use desktop app.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        description.setForeground(new Color(79, 96, 111));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFocusable(false);
        description.setMaximumSize(new Dimension(590, 62));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton getStarted = Ui.button("Get started  →");
        getStarted.setFont(new Font("Segoe UI", Font.BOLD, 15));
        getStarted.setAlignmentX(Component.LEFT_ALIGNMENT);
        getStarted.addActionListener(e -> openLogin());

        JPanel features = new JPanel(new GridLayout(1, 3, 14, 14));
        features.setBackground(Ui.BG);
        features.setAlignmentX(Component.LEFT_ALIGNMENT);
        features.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        features.add(featureCard("01", "Trusted circle", "Manage the people you trust."));
        features.add(featureCard("02", "SOS simulation", "Preview an alert and save its history."));
        features.add(featureCard("03", "Local guidance", "Browse sample places and routes."));

        JLabel notice = new JLabel(
                "<html><b>Educational demo only.</b> No real messages, emergency calls, GPS, or live navigation are provided.</html>");
        notice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notice.setForeground(new Color(107, 83, 38));
        notice.setOpaque(true);
        notice.setBackground(new Color(255, 247, 225));
        notice.setBorder(new EmptyBorder(12, 14, 12, 14));
        notice.setAlignmentX(Component.LEFT_ALIGNMENT);
        notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        content.add(eyebrow);
        content.add(Box.createVerticalStrut(12));
        content.add(title);
        content.add(Box.createVerticalStrut(12));
        content.add(description);
        content.add(Box.createVerticalStrut(20));
        content.add(getStarted);
        content.add(Box.createVerticalStrut(30));
        content.add(features);
        content.add(Box.createVerticalStrut(22));
        content.add(notice);
        return content;
    }

    private JPanel featureCard(String number, String title, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 231, 235)),
                new EmptyBorder(14, 16, 14, 16)));
        JLabel index = new JLabel(number);
        index.setForeground(Ui.TEAL);
        index.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel heading = new JLabel(title);
        heading.setForeground(Ui.NAVY);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel detail = new JLabel("<html>" + description + "</html>");
        detail.setForeground(new Color(91, 106, 119));
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(index);
        card.add(Box.createVerticalStrut(6));
        card.add(heading);
        card.add(Box.createVerticalStrut(5));
        card.add(detail);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 44, 12, 44));
        JLabel text = new JLabel("Women Safety • Student project");
        text.setForeground(new Color(111, 124, 135));
        text.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(text, BorderLayout.WEST);
        return footer;
    }

    private void openLogin() {
        new LoginFrame(app).setVisible(true);
        dispose();
    }
}
