package womensafety.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class Ui {
    static final Color NAVY = new Color(28, 49, 73);
    static final Color TEAL = new Color(0, 125, 125);
    static final Color RED = new Color(190, 35, 55);
    static final Color BG = new Color(245, 248, 250);
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private Ui() { }
    static void frame(JFrame frame, String title) {
        frame.setTitle(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(850, 600);
        frame.setMinimumSize(new Dimension(700, 480));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG);
    }
    static JPanel page(String heading) {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(22, 28, 24, 28));
        JLabel label = new JLabel(heading);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(NAVY);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }
    static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT);
        button.setFocusPainted(false);
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        return button;
    }
    static void configureTable(JTable table) {
        table.setFont(FONT);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFillsViewportHeight(true);
    }
}
