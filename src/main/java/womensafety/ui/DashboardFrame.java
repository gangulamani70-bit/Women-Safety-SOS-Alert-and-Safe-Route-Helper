package womensafety.ui;

import womensafety.exception.NoContactException;
import womensafety.model.SOSAlert;
import womensafety.model.SafePlace;
import womensafety.model.User;
import womensafety.service.AppServices;
import womensafety.model.TrustedContact;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final AppServices app;
    private final User user;
    private final JLabel locationLabel = new JLabel();

    public DashboardFrame(AppServices app, User user) {
        this.app = app;
        this.user = user;
        Ui.frame(this, "Women Safety | Dashboard");
        setSize(900, 650);
        JPanel page = Ui.page("Welcome, " + user.getName());
        JPanel main = new JPanel(new BorderLayout(18, 18));
        main.setBackground(Ui.BG);
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        refreshLocation();
        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loc.setBackground(Ui.BG);
        loc.add(new JLabel("Current location:")); loc.add(locationLabel);
        JButton sos = Ui.button("SOS EMERGENCY");
        sos.setFont(new Font("Segoe UI", Font.BOLD, 23));
        sos.setBackground(Ui.RED);
        sos.setPreferredSize(new Dimension(330, 82));
        JPanel sosPanel = new JPanel();
        sosPanel.setBackground(Ui.BG);
        sosPanel.add(sos);
        JPanel actions = new JPanel(new GridLayout(0, 2, 14, 14));
        actions.setBackground(Ui.BG);
        actions.setBorder(BorderFactory.createTitledBorder("Safety tools"));
        JButton contacts = Ui.button("Trusted Contacts");
        JButton updateLocation = Ui.button("Update Location");
        JButton places = Ui.button("Find Safe Places");
        JButton route = Ui.button("Safe Route Helper");
        JButton history = Ui.button("Alert History");
        JButton logout = Ui.button("Logout");
        for (JButton b : List.of(contacts, updateLocation, places, route, history, logout)) actions.add(b);
        main.add(loc, BorderLayout.NORTH);
        main.add(sosPanel, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        page.add(main, BorderLayout.CENTER);
        add(page);

        contacts.addActionListener(e -> open(new ContactFrame(app, user)));
        updateLocation.addActionListener(e -> open(new LocationFrame(app, user, this::refreshLocation)));
        places.addActionListener(e -> open(new SafePlaceFrame(app, user)));
        route.addActionListener(e -> open(new RouteFrame(app, user)));
        history.addActionListener(e -> open(new AlertHistoryFrame(app)));
        logout.addActionListener(e -> { new LoginFrame(app).setVisible(true); dispose(); });
        sos.addActionListener(e -> activateSos());
    }

    private void open(JFrame frame) { frame.setVisible(true); }
    private void refreshLocation() {
        if (user.getCurrentLocation() == null) app.locations.getCurrentLocation(user);
        locationLabel.setText(user.getCurrentLocation() == null ? "Not set" : user.getCurrentLocation().getAddress());
    }

    private void activateSos() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to activate SOS?",
                "Confirm SOS", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            SOSAlert alert = app.alerts.sendSOS(user);
            StringBuilder result = new StringBuilder("SOS ACTIVATED (SIMULATION)\n")
                    .append("Alert ID: ").append(alert.getAlertId()).append("\n\n")
                    .append("User: ").append(user.getName()).append("\n")
                    .append("Current location: ").append(alert.getLocation().getAddress()).append("\n")
                    .append("Status: ").append(alert.getStatus()).append("\n\n")
                    .append("Simulated notifications to:\n");
            int index = 1;
            for (TrustedContact contact : user.getTrustedContacts()) {
                result.append(index++).append(". ").append(contact.getName()).append(" - ").append(contact.getRelation()).append('\n');
            }
            result.append("\nNearby safe places:\n");
            for (SafePlace place : app.safePlaces.getNearbySafePlaces()) {
                result.append("- ").append(place.getName()).append(" - ").append(place.getDistance()).append(" km\n");
            }
            result.append("\nAlert saved to local history.\n\nThis simulation does not contact emergency services.");
            JTextArea text = new JTextArea(result.toString(), 20, 42);
            text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(text), "SOS simulation", JOptionPane.INFORMATION_MESSAGE);
        } catch (NoContactException e) {
            JOptionPane.showMessageDialog(this, "Emergency alert cannot be sent.\n" + e.getMessage(),
                    "No trusted contacts", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Location required", JOptionPane.WARNING_MESSAGE);
        }
    }
}
