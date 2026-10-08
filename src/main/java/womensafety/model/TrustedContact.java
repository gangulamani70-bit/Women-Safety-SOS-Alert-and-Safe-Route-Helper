package womensafety.model;

public class TrustedContact {
    private String contactId;
    private String name;
    private String relation;
    private String phone;

    public TrustedContact(String contactId, String name, String relation, String phone) {
        this.contactId = contactId;
        this.name = name;
        this.relation = relation;
        this.phone = phone;
    }

    public String displayDetails() { return name + " - " + relation + " - " + phone; }
    public String receiveAlert(SOSAlert alert) {
        return "Simulated notification to " + name + " (" + phone + ") for " + alert.getAlertId();
    }
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
