package womensafety.ui;

import womensafety.model.Location;
import womensafety.model.User;
import womensafety.service.AppServices;

import javax.swing.*;
import java.awt.*;

public class LocationFrame extends JFrame {
    public LocationFrame(AppServices app, User user, Runnable onUpdate) {
        Ui.frame(this, "Update location");
        JPanel page = Ui.page("Update simulated location");
        JComboBox<Location> locations = new JComboBox<>(app.locations.getAvailableLocations().toArray(Location[]::new));
        locations.setRenderer((list, value, index, selected, focus) ->
                new JLabel(value == null ? "" : value.getAddress()));
        JTextArea details = new JTextArea(6, 35);
        details.setEditable(false);
        details.setFont(Ui.FONT);
        Runnable showSelected = () -> {
            Location l = (Location) locations.getSelectedItem();
            if (l != null) details.setText("Sample location: " + l.getAddress()
                    + "\nCoordinates are not displayed.\nTimestamp: Updated when selected");
        };
        locations.addActionListener(e -> showSelected.run());
        showSelected.run();
        JPanel form = new JPanel(new BorderLayout(12, 12));
        form.setBackground(Ui.BG);
        form.add(new JLabel("Choose a sample location:"), BorderLayout.NORTH);
        form.add(locations, BorderLayout.CENTER);
        form.add(new JScrollPane(details), BorderLayout.SOUTH);
        JButton update = Ui.button("UPDATE LOCATION");
        JButton back = Ui.button("Back to Dashboard");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(Ui.BG); buttons.add(update); buttons.add(back);
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setBackground(Ui.BG); center.add(form, BorderLayout.CENTER); center.add(buttons, BorderLayout.SOUTH);
        page.add(center, BorderLayout.CENTER); add(page);
        update.addActionListener(e -> {
            Location selected = (Location) locations.getSelectedItem();
            if (selected == null) return;
            app.locations.updateLocation(user, selected);
            details.setText("Location updated: " + user.getCurrentLocation().getAddress()
                    + "\nCoordinates are not displayed.\nTimestamp: " + user.getCurrentLocation().getTimestamp());
            onUpdate.run();
            JOptionPane.showMessageDialog(this, "Location updated successfully.");
        });
        back.addActionListener(e -> dispose());
    }
}
