package womensafety.service;

import womensafety.exception.ValidationException;
import womensafety.model.User;
import womensafety.util.FileManager;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserService {
    private static final Pattern PHONE = Pattern.compile("\\+?[0-9]{10,15}");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String PASSWORD_PREFIX = "pbkdf2-sha256$";
    private static final int HASH_ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final FileManager files;

    public UserService(FileManager files) { this.files = files; }

    public User login(String id, String password) {
        if (id == null || password == null) return null;
        String login = id.trim();
        List<User> users = new ArrayList<>(files.readUsers());
        for (User user : users) {
            if (!user.getUserId().equalsIgnoreCase(login) && !user.getPhone().equals(login)) continue;
            if (user.getPassword().startsWith(PASSWORD_PREFIX)) {
                return verifyPassword(password, user.getPassword()) ? user : null;
            }
            if (MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    user.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                user.setPassword(hashPassword(password));
                files.writeUsers(users);
                return user;
            }
            return null;
        }
        return null;
    }

    public User findUserById(String id) {
        return files.readUsers().stream().filter(u -> u.getUserId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public User registerUser(String name, String phone, String email, String password) throws ValidationException {
        if (name == null || name.isBlank()) throw new ValidationException("Name cannot be empty.");
        if (phone == null || !PHONE.matcher(phone.trim()).matches()) throw new ValidationException("Enter a valid phone number (10–15 digits).");
        if (email == null || !EMAIL.matcher(email.trim()).matches()) throw new ValidationException("Enter a valid email address.");
        if (password == null || password.isBlank()) throw new ValidationException("Password cannot be empty.");
        List<User> users = new ArrayList<>(files.readUsers());
        if (users.stream().anyMatch(u -> u.getPhone().equals(phone.trim()))) throw new ValidationException("That phone number is already registered.");
        String id = "U" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        User user = new User(id, name.trim(), phone.trim(), email.trim(), hashPassword(password));
        users.add(user);
        files.writeUsers(users);
        return user;
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password.toCharArray(), salt, HASH_ITERATIONS);
        return PASSWORD_PREFIX + HASH_ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) +
                "$" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String stored) {
        try {
            String[] fields = stored.split("\\$", -1);
            if (fields.length != 4 || !fields[0].equals("pbkdf2-sha256")) return false;
            int iterations = Integer.parseInt(fields[1]);
            if (iterations < 100_000 || iterations > 1_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] expected = Base64.getDecoder().decode(fields[3]);
            return MessageDigest.isEqual(expected, deriveKey(password.toCharArray(), salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] deriveKey(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }
}
