package womensafety.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private String phone;
    private String email;
    private String password;
    private Location currentLocation;
    private final ArrayList<TrustedContact> trustedContacts = new ArrayList<>();

    public User(String userId, String name, String phone, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public void addTrustedContact(TrustedContact contact) { trustedContacts.add(contact); }
    public boolean removeTrustedContact(String id) { return trustedContacts.removeIf(c -> c.getContactId().equals(id)); }
    public List<TrustedContact> getTrustedContacts() { return new ArrayList<>(trustedContacts); }
    public void setTrustedContacts(List<TrustedContact> contacts) {
        trustedContacts.clear();
        trustedContacts.addAll(contacts);
    }
    public void updateLocation(Location location) { currentLocation = location; }
    public String displayUserDetails() { return userId + " - " + name + " - " + phone; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Location getCurrentLocation() { return currentLocation; }
}
