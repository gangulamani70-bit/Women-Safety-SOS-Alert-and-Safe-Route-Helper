package womensafety.ui;

import womensafety.model.Location;
import womensafety.model.SafePlace;
import womensafety.model.User;
import womensafety.service.AppServices;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RouteFrame extends JFrame {
    public RouteFrame(AppServices app, User user) {
        Ui.frame(this, "Safe route helper");
        JPanel page = Ui.page("Safe route helper (offline simulation)");
        List<Location> available = app.locations.getAvailableLocations();
        JComboBox<Location> start = new JComboBox<>(available.toArray(Location[]::new));
        JComboBox<Location> destination = new JComboBox<>(available.toArray(Location[]::new));
        start.setRenderer((list, value, index, selected, focus) -> new JLabel(value == null ? "" : value.getAddress()));
        destination.setRenderer((list, value, index, selected, focus) -> new JLabel(value == null ? "" : value.getAddress()));
        Location current = user.getCurrentLocation();
        if (current != null) {
            for (int i = 0; i < available.size(); i++) {
                if (available.get(i).getLocationId().equals(current.getLocationId())) start.setSelectedIndex(i);
            }
        }
        JPanel fields = new JPanel(new GridLayout(2, 2, 12, 12));
        fields.setBackground(Ui.BG);
        fields.add(new JLabel("Start location")); fields.add(start);
        fields.add(new JLabel("Destination")); fields.add(destination);
        JTextArea result = new JTextArea(14, 55);
        result.setEditable(false); result.setFont(Ui.FONT); result.setLineWrap(true); result.setWrapStyleWord(true);
        JButton suggest = Ui.button("Suggest safe route");
        JButton back = Ui.button("Back to Dashboard");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(Ui.BG); actions.add(suggest); actions.add(back);
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(Ui.BG); content.add(fields, BorderLayout.NORTH);
        content.add(new JScrollPane(result), BorderLayout.CENTER); content.add(actions, BorderLayout.SOUTH);
        page.add(content, BorderLayout.CENTER); add(page);
        suggest.addActionListener(e -> {
            Location from = (Location) start.getSelectedItem();
            Location to = (Location) destination.getSelectedItem();
            if (from == null || to == null || from.getLocationId().equals(to.getLocationId())) {
                JOptionPane.showMessageDialog(this, "Choose different start and destination locations.", "Route required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<SafePlace> nearby = app.safePlaces.getNearbySafePlaces();
            StringBuilder route = new StringBuilder("Start: ").append(from.getAddress()).append("\n\nSuggested safe route:\n")
                    .append(from.getAddress()).append('\n');
            for (SafePlace p : nearby) route.append("↓\n").append(p.getName()).append(" (").append(p.getType()).append(")\n");
            route.append("↓\n").append(to.getAddress()).append("\n\nSafe places near the simulated route:\n");
            for (SafePlace p : nearby) route.append("• ").append(p.displayDetails()).append('\n');
            route.append("\nSuggested route includes nearby safe places.\n")
                    .append("Route is illustrative only; it is not map navigation or a safety guarantee.");
            result.setText(route.toString());
        });
        back.addActionListener(e -> dispose());
    }
}
