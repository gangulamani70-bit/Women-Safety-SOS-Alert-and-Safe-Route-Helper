package womensafety.ui;

import womensafety.exception.ValidationException;
import womensafety.model.TrustedContact;
import womensafety.model.User;
import womensafety.service.AppServices;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.UUID;

public class ContactFrame extends JFrame {
    private final AppServices app;
    private final User user;
    private final JTextField name = new JTextField(14);
    private final JTextField relation = new JTextField(12);
    private final JTextField phone = new JTextField(14);
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Relation", "Phone"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public ContactFrame(AppServices app, User user) {
        this.app = app; this.user = user;
        Ui.frame(this, "Trusted contacts");
        JPanel page = Ui.page("Trusted contacts");
        JTable table = new JTable(model); Ui.configureTable(table);
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        inputs.setBackground(Ui.BG);
        inputs.add(new JLabel("Name")); inputs.add(name);
        inputs.add(new JLabel("Relation")); inputs.add(relation);
        inputs.add(new JLabel("Phone")); inputs.add(phone);
        JButton add = Ui.button("Add contact");
        JButton remove = Ui.button("Remove selected");
        JButton back = Ui.button("Back to Dashboard");
        inputs.add(add); inputs.add(remove); inputs.add(back);
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Ui.BG);
        center.add(inputs, BorderLayout.NORTH); center.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER); add(page);
        refresh();
        add.addActionListener(e -> addContact());
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a contact first."); return; }
            user.removeTrustedContact(String.valueOf(model.getValueAt(row, 0)));
            saveAndRefresh();
        });
        back.addActionListener(e -> dispose());
    }

    private void addContact() {
        try {
            if (name.getText().isBlank() || relation.getText().isBlank()) throw new ValidationException("Name and relation are required.");
            if (!phone.getText().trim().matches("\\+?[0-9]{10,15}")) throw new ValidationException("Enter a valid phone number (10–15 digits).");
            if (user.getTrustedContacts().stream().anyMatch(c -> c.getPhone().equals(phone.getText().trim())))
                throw new ValidationException("That phone number is already in your contacts.");
            user.addTrustedContact(new TrustedContact("C" + UUID.randomUUID().toString().substring(0, 7),
                    name.getText().trim(), relation.getText().trim(), phone.getText().trim()));
            saveAndRefresh();
            name.setText(""); relation.setText(""); phone.setText("");
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Check contact details", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveAndRefresh() {
        app.files.writeContacts(user.getUserId(), user.getTrustedContacts());
        refresh();
    }
    private void refresh() {
        model.setRowCount(0);
        for (TrustedContact c : app.files.readContacts(user.getUserId())) model.addRow(new Object[]{c.getContactId(), c.getName(), c.getRelation(), c.getPhone()});
        user.setTrustedContacts(app.files.readContacts(user.getUserId()));
    }
}
