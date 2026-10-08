package womensafety.service;

import womensafety.exception.NoContactException;
import womensafety.model.SOSAlert;
import womensafety.model.TrustedContact;
import womensafety.model.User;
import womensafety.util.FileManager;

public class AlertService implements AlertSender {
    private final FileManager files;
    public AlertService(FileManager files) { this.files = files; }

    public SOSAlert sendSOS(User user) throws NoContactException {
        if (user.getTrustedContacts().isEmpty()) {
            throw new NoContactException("No trusted contacts found. Please add at least one trusted contact.");
        }
        if (user.getCurrentLocation() == null) throw new IllegalStateException("Please update your current location first.");
        SOSAlert alert = new SOSAlert(user, user.getCurrentLocation());
        for (TrustedContact contact : user.getTrustedContacts()) sendAlert(contact, alert);
        files.saveAlert(alert);
        return alert;
    }

    @Override
    public void sendAlert(TrustedContact contact, SOSAlert alert) {
        contact.receiveAlert(alert);
    }
}
