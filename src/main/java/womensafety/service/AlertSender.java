package womensafety.service;

import womensafety.model.SOSAlert;
import womensafety.model.TrustedContact;

public interface AlertSender {
    void sendAlert(TrustedContact contact, SOSAlert alert);
}
