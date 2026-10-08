package womensafety.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AlertHistoryFrame extends JFrame {
    public AlertHistoryFrame(womensafety.service.AppServices app) {
        Ui.frame(this, "Alert history");
        JPanel page = Ui.page("SOS alert history");
        DefaultTableModel model = new DefaultTableModel(new String[]{"Alert ID", "User", "Date", "Time", "Location", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (String[] r : app.files.readAlerts()) model.addRow(new Object[]{r[0], r[1], r[3], r[4], r[2], r[5]});
        JTable table = new JTable(model); Ui.configureTable(table);
        JLabel note = new JLabel("History is stored locally in data/alert_history.txt. SOS records are simulations.");
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Ui.BG); content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(note, BorderLayout.SOUTH);
        page.add(content, BorderLayout.CENTER);
        JButton back = Ui.button("Back to Dashboard");
        page.add(back, BorderLayout.SOUTH);
        back.addActionListener(e -> dispose());
        add(page);
    }
}
