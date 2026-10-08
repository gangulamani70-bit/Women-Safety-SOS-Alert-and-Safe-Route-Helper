package womensafety.ui;

import womensafety.model.SafePlace;
import womensafety.model.User;
import womensafety.service.AppServices;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SafePlaceFrame extends JFrame {
    private final AppServices app;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Location", "Distance"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public SafePlaceFrame(AppServices app, User user) {
        this.app = app;
        Ui.frame(this, "Nearby safe places");
        JPanel page = Ui.page("Nearby safe places");
        JLabel location = new JLabel("Current location: " + (user.getCurrentLocation() == null ? "Not set" : user.getCurrentLocation().getAddress()));
        JTable table = new JTable(model); Ui.configureTable(table);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setBackground(Ui.BG);
        for (String type : List.of("ALL", "POLICE", "HOSPITAL", "SHOP")) {
            JButton b = Ui.button(type);
            filters.add(b);
            b.addActionListener(e -> fill(type));
        }
        JButton nearest = Ui.button("NEAREST");
        JButton back = Ui.button("Back to Dashboard");
        filters.add(nearest); filters.add(back);
        nearest.addActionListener(e -> {
            app.safePlaces.findNearestPlace().ifPresentOrElse(
                    p -> JOptionPane.showMessageDialog(this, "Nearest listed safe place:\n" + p.displayDetails(),
                            "Nearest place", JOptionPane.INFORMATION_MESSAGE),
                    () -> JOptionPane.showMessageDialog(this, "No safe-place records are available.",
                            "Nearest place", JOptionPane.INFORMATION_MESSAGE));
        });
        back.addActionListener(e -> dispose());
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Ui.BG); center.add(location, BorderLayout.NORTH); center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(filters, BorderLayout.SOUTH);
        page.add(center, BorderLayout.CENTER); add(page); fill("ALL");
    }

    private void fill(String type) {
        model.setRowCount(0);
        List<SafePlace> places = type.equals("ALL") ? app.safePlaces.getAllSafePlaces() : app.safePlaces.findByType(type);
        for (SafePlace p : places) model.addRow(new Object[]{p.getPlaceId(), p.getName(), p.getType(), p.getLocation(), p.getDistance() + " km"});
    }
}
