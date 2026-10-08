package womensafety.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import womensafety.exception.NoContactException;
import womensafety.model.Location;
import womensafety.model.SafePlace;
import womensafety.model.TrustedContact;
import womensafety.model.User;
import womensafety.service.AppServices;
import womensafety.util.FileManager;
import womensafety.util.SampleData;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LocalhostServer {
    private static final int DEFAULT_PORT = 8080;
    private static final String SESSION_COOKIE = "WOMEN_SAFETY_SESSION";
    private final AppServices app;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final ThreadLocal<User> requestUser = new ThreadLocal<>();
    private final ThreadLocal<String> requestNotice = ThreadLocal.withInitial(() -> "");

    private LocalhostServer(AppServices app) {
        this.app = app;
    }

    public static void main(String[] args) throws IOException {
        boolean production = "production".equalsIgnoreCase(System.getenv("APP_ENV"));
        int port = readPort();
        String host = System.getenv().getOrDefault("HOST", production ? "0.0.0.0" : "127.0.0.1");
        String dataDirectory = System.getenv().getOrDefault("DATA_DIR", "data");
        FileManager files = new FileManager(java.nio.file.Path.of(dataDirectory));
        SampleData.initialize(files, !production);
        AppServices app = new AppServices(files);
        LocalhostServer application = new LocalhostServer(app);
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", application::handle);
        server.setExecutor(null);
        server.start();
        if (production) {
            System.out.println("Women Safety server is listening on " + host + ":" + port);
        } else {
            String base = "http://localhost:" + port;
            System.out.println("Women Safety is available at " + base + " (local machine only).");
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(java.net.URI.create(base));
            } catch (Exception e) {
                System.err.println("Could not open a browser automatically. Open " + base + " manually: " + e.getMessage());
            }
        }
    }

    private static int readPort() {
        String value = System.getenv("PORT");
        if (value == null || value.isBlank()) return DEFAULT_PORT;
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PORT must be a valid TCP port from 1 to 65535.", e);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            if ("/healthz".equals(exchange.getRequestURI().getPath())) {
                sendHtml(exchange, "ok");
                return;
            }
            User user = authenticatedUser(exchange);
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String action = form.getOrDefault("action", "");
                if ("login".equals(action)) {
                    handleLogin(exchange, form);
                    return;
                }
                if ("register".equals(action)) {
                    handleRegistration(exchange, form);
                    return;
                }
                if ("logout".equals(action)) {
                    String token = sessionToken(exchange);
                    if (token != null) sessions.remove(token);
                    exchange.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
                    redirect(exchange, "/?page=login&message=You%20have%20signed%20out.");
                    return;
                }
                if ("liveLocation".equals(action)) {
                    if (user == null) {
                        redirect(exchange, "/?page=login&message=Please%20sign%20in%20to%20continue.");
                        return;
                    }
                    requestUser.set(user);
                    updateLiveLocation(form);
                    String message = java.net.URLEncoder.encode(requestNotice.get(), StandardCharsets.UTF_8);
                    redirect(exchange, "/?page=location&message=" + message);
                    return;
                }
                if (user == null) {
                    redirect(exchange, "/?page=login&message=Please%20sign%20in%20to%20continue.");
                    return;
                }
                requestUser.set(user);
                String page = handlePostAction(form);
                String message = requestNotice.get();
                if (!message.isBlank()) page += "&message=" + java.net.URLEncoder.encode(message, StandardCharsets.UTF_8);
                redirect(exchange, "/?page=" + page);
                return;
            }
            Map<String, String> query = parseForm(exchange.getRequestURI().getRawQuery());
            String page = query.getOrDefault("page", user == null ? "login" : "home");
            if ("register".equals(page)) {
                sendHtml(exchange, registrationPage());
                return;
            }
            if ("login".equals(page)) {
                if (user != null && !query.containsKey("message")) {
                    redirect(exchange, "/?page=home");
                    return;
                }
                sendHtml(exchange, loginPage(query.getOrDefault("message", "")));
                return;
            }
            if (user == null) {
                redirect(exchange, "/?page=login&message=Please%20sign%20in%20to%20continue.");
                return;
            }
            requestUser.set(user);
            user.setTrustedContacts(app.files.readContacts(user.getUserId()));
            app.locations.getCurrentLocation(user);
            requestNotice.set(query.getOrDefault("message", ""));
            String html = render(page, query.getOrDefault("type", "ALL"));
            sendHtml(exchange, html);
        } catch (RuntimeException e) {
            System.err.println("Localhost request failed: " + e.getMessage());
            sendHtml(exchange, pageShell("Request error", "<section class='card'><h2>Unable to complete that action</h2><p>" +
                    escape(e.getMessage()) + "</p><a class='button' href='/?page=home'>Return home</a></section>"), 500);
        } finally {
            requestUser.remove();
            requestNotice.remove();
            exchange.close();
        }
    }

    private User authenticatedUser(HttpExchange exchange) {
        String token = sessionToken(exchange);
        if (token == null) return null;
        String userId = sessions.get(token);
        return userId == null ? null : app.users.findUserById(userId);
    }

    private String sessionToken(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && SESSION_COOKIE.equals(parts[0])) return parts[1];
        }
        return null;
    }

    private void handleLogin(HttpExchange exchange, Map<String, String> form) throws IOException {
        User user = app.users.login(form.getOrDefault("userId", ""), form.getOrDefault("password", ""));
        if (user == null) {
            sendHtml(exchange, loginPage("Invalid user ID or password."));
            return;
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getUserId());
        String secure = "production".equalsIgnoreCase(System.getenv("APP_ENV")) ? "; Secure" : "";
        exchange.getResponseHeaders().add("Set-Cookie",
                SESSION_COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Lax" + secure);
        redirect(exchange, "/?page=home");
    }

    private void handleRegistration(HttpExchange exchange, Map<String, String> form) throws IOException {
        try {
            User user = app.users.registerUser(form.getOrDefault("name", ""), form.getOrDefault("phone", ""),
                    form.getOrDefault("email", ""), form.getOrDefault("password", ""));
            redirect(exchange, "/?page=login&message=" + java.net.URLEncoder.encode(
                    "Registration successful. Sign in with your new user ID: " + user.getUserId(), StandardCharsets.UTF_8));
        } catch (womensafety.exception.ValidationException e) {
            sendHtml(exchange, registrationPage(e.getMessage()));
        }
    }

    private String loginPage(String message) {
        return pageShell("Sign in | Women Safety",
                "<section class='auth card'><p class='eyebrow'>SOS & SAFE ROUTE HELPER</p><h1>Welcome back</h1>" +
                "<p class='muted'>Sign in to access your safety dashboard.</p>" +
                (message.isBlank() ? "" : "<div class='notice'>" + escape(message) + "</div>") +
                "<form method='post' class='stack'><input type='hidden' name='action' value='login'>" +
                "<label>User ID or phone<input name='userId' required autocomplete='username'></label>" +
                "<label>Password<input type='password' name='password' required autocomplete='current-password'></label>" +
                "<button class='button' type='submit'>Sign in</button></form>" +
                "<p class='muted'>New here? <a href='/?page=register'>Create an account</a></p></section>");
    }

    private String registrationPage() { return registrationPage(""); }

    private String registrationPage(String message) {
        return pageShell("Create account | Women Safety",
                "<section class='auth card'><p class='eyebrow'>SOS & SAFE ROUTE HELPER</p><h1>Create account</h1>" +
                (message.isBlank() ? "" : "<div class='notice'>" + escape(message) + "</div>") +
                "<form method='post' class='stack'><input type='hidden' name='action' value='register'>" +
                "<label>Name<input name='name' required autocomplete='name'></label>" +
                "<label>Phone number<input name='phone' required pattern='[+]?[0-9]{10,15}' autocomplete='tel'></label>" +
                "<label>Email<input type='email' name='email' required autocomplete='email'></label>" +
                "<label>Password<input type='password' name='password' required autocomplete='new-password'></label>" +
                "<button class='button' type='submit'>Register</button></form>" +
                "<p class='muted'>Already registered? <a href='/?page=login'>Sign in</a></p></section>");
    }

    private void sendHtml(HttpExchange exchange, String html) throws IOException { sendHtml(exchange, html, 200); }

    private void sendHtml(HttpExchange exchange, String html, int status) throws IOException {
            byte[] body = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
    }

    private String handlePostAction(Map<String, String> form) {
        String action = form.getOrDefault("action", "");
        switch (action) {
            case "sos" -> { sendSos(); return "home"; }
            case "addContact" -> { addContact(form); return "contacts"; }
            case "removeContact" -> { removeContact(form); return "contacts"; }
            case "location" -> { updateLocation(form); return "location"; }
            case "route" -> { showRoute(form); return "route"; }
            case "saveEmergencyNumbers" -> { saveEmergencyNumbers(form); return "places"; }
            default -> { requestNotice.set("That action was not recognized."); return "home"; }
        }
    }

    private String render(String page, String type) {
        if (!List.of("home", "contacts", "location", "places", "route", "history").contains(page)) page = "home";
        String content = switch (page) {
            case "contacts" -> contactsPage();
            case "location" -> locationPage();
            case "places" -> placesPage(type);
            case "route" -> routePage();
            case "history" -> historyPage();
            default -> homePage();
        };
        String result = requestNotice.get();
        requestNotice.set("");
        return pageShell("Women Safety | " + page, (result.isBlank() ? "" : "<div class='notice'>" + escape(result) + "</div>") + content);
    }

    private String homePage() {
        User user = requestUser.get();
        String location = user.getCurrentLocation() == null ? "Not set" : user.getCurrentLocation().getAddress();
        List<TrustedContact> contacts = user.getTrustedContacts();
        StringBuilder callLinks = new StringBuilder();
        for (TrustedContact contact : contacts) {
            callLinks.append("<a class='button' href='tel:").append(escape(contact.getPhone()))
                    .append("' onclick='return confirmCall(this)'>Call ").append(escape(contact.getName())).append("</a>");
        }
        String[] numbers = app.files.readEmergencyNumbers(user.getUserId());
        return """
                <section class="hero">
                  <p class="eyebrow">SOS & SAFE ROUTE HELPER</p>
                  <h1>Welcome, %s</h1>
                  <p class="muted">Current sample location: <strong>%s</strong></p>
                  <form method="post"><input type="hidden" name="action" value="sos">
                    <button class="sos" type="submit">SOS EMERGENCY</button>
                  </form>
                  <p class="warning">Simulation only: this does not contact emergency services or send messages.</p>
                </section>
                <section class="card"><h2>Call for help</h2>
                  <p class="muted">Choose a call yourself. Your device may ask you to confirm; the app cannot place calls automatically.</p>
                  <div class="call-actions">%s%s</div>
                  <p>%s</p>
                </section>
                <section class="grid">
                  <a class="card linkcard" href="/?page=contacts"><h2>Trusted contacts</h2><p>View, add, or remove contacts.</p></a>
                  <a class="card linkcard" href="/?page=location"><h2>Update location</h2><p>Choose a predefined sample location.</p></a>
                  <a class="card linkcard" href="/?page=places"><h2>Safe places</h2><p>Browse the sample nearby directory.</p></a>
                  <a class="card linkcard" href="/?page=route"><h2>Safe route helper</h2><p>Preview an illustrative route.</p></a>
                  <a class="card linkcard" href="/?page=history"><h2>Alert history</h2><p>Review alerts saved locally.</p></a>
                </section>
                """.formatted(escape(user.getName()), escape(location),
                callLink("Call Police", numbers[1]), callLink("Call Hospital", numbers[2]),
                callLinks.isEmpty() ? "<span class='muted'>Add trusted contacts to enable call links.</span>" : callLinks.toString());
    }

    private String contactsPage() {
        StringBuilder rows = new StringBuilder();
        for (TrustedContact c : requestUser.get().getTrustedContacts()) {
            rows.append("<tr><td>").append(escape(c.getName())).append("</td><td>")
                    .append(escape(c.getRelation())).append("</td><td>").append(escape(c.getPhone()))
                    .append("</td><td><form method='post'><input type='hidden' name='action' value='removeContact'>")
                    .append("<input type='hidden' name='id' value='").append(escape(c.getContactId()))
                    .append("'><button class='small danger' type='submit'>Remove</button></form></td><td>")
                    .append(callLink("Call " + c.getName(), c.getPhone())).append("</td></tr>");
        }
        if (rows.isEmpty()) rows.append("<tr><td colspan='5'>No contacts saved. Add at least one before simulating SOS.</td></tr>");
        return "<section class='card'><h1>Trusted contacts</h1><div class='tablewrap'><table><thead><tr><th>Name</th><th>Relation</th><th>Phone</th><th></th><th>Call</th></tr></thead><tbody>" +
                rows + "</tbody></table></div><h2>Add contact</h2><form method='post' class='formrow'>" +
                "<input type='hidden' name='action' value='addContact'><label>Name<input name='name' required></label>" +
                "<label>Relation<input name='relation' required></label><label>Phone<input name='phone' required pattern='[+]?[0-9]{10,15}'></label>" +
                "<button class='button' type='submit'>Add contact</button></form></section>";
    }

    private String locationPage() {
        User user = requestUser.get();
        StringBuilder options = new StringBuilder();
        for (Location l : app.locations.getAvailableLocations()) {
            options.append("<option value='").append(escape(l.getLocationId())).append("'")
                    .append(user.getCurrentLocation() != null && user.getCurrentLocation().getLocationId().equals(l.getLocationId()) ? " selected" : "")
                    .append(">").append(escape(l.getAddress())).append("</option>");
        }
        Location current = user.getCurrentLocation();
        String detail = current == null ? "No location selected." :
                (current.getLocationId().equals("LIVE") ? "Live location updated" : current.getAddress())
                        + " · updated " + current.getTimestamp();
        return "<section class='card'><h1>Update location</h1><p class='muted'>" + escape(detail) +
                "</p><p class='muted'>Your coordinates stay on this computer and are not shown on screen or sent to other people.</p>" +
                "<button class='button' id='live-location' type='button'>Use my live location</button>" +
                "<p id='location-status' class='muted' aria-live='polite'>Your browser will ask permission before sharing location with this local app.</p>" +
                "<form id='live-location-form' method='post' hidden><input type='hidden' name='action' value='liveLocation'>" +
                "<input type='hidden' name='latitude'><input type='hidden' name='longitude'></form>" +
                "<form method='post' class='stack'><input type='hidden' name='action' value='location'>" +
                "<label>Choose a simulated location<select name='locationId'>" + options + "</select></label>" +
                "<button class='button' type='submit'>Update sample location</button></form>" +
                "<p class='warning'>Live GPS requires browser permission. Sample locations are not live GPS.</p>" +
                "<script>(function(){const button=document.getElementById('live-location');const status=document.getElementById('location-status');" +
                "button.addEventListener('click',function(){if(!navigator.geolocation){status.textContent='Live location is not supported by this browser.';return;}" +
                "button.disabled=true;status.textContent='Waiting for location permission…';navigator.geolocation.getCurrentPosition(function(position){" +
                "const form=document.getElementById('live-location-form');form.elements.latitude.value=position.coords.latitude;" +
                "form.elements.longitude.value=position.coords.longitude;status.textContent='Location received. Saving privately on this computer…';form.submit();" +
                "},function(error){button.disabled=false;status.textContent=error.code===1?'Location permission was denied. You can choose a sample location instead.':'Could not get your location. Check browser/device location settings and try again.';},{enableHighAccuracy:true,timeout:15000,maximumAge:0});});})();</script>" +
                "</section>";
    }

    private String placesPage(String type) {
        User user = requestUser.get();
        List<SafePlace> places = "ALL".equalsIgnoreCase(type) ? app.safePlaces.getAllSafePlaces() : app.safePlaces.findByType(type);
        String[] numbers = app.files.readEmergencyNumbers(user.getUserId());
        StringBuilder rows = new StringBuilder();
        for (SafePlace p : places) {
            rows.append("<tr><td>").append(escape(p.getPlaceId())).append("</td><td>").append(escape(p.getName()))
                    .append("</td><td>").append(escape(p.getType())).append("</td><td>").append(escape(p.getLocation()))
                        .append("</td><td>").append(p.getDistance()).append(" km</td><td>")
                    .append("Police".equals(p.getType()) ? callLink("Call configured police line", numbers[1])
                            : "Hospital".equals(p.getType()) ? callLink("Call configured hospital line", numbers[2]) : "—")
                    .append("</td></tr>");
        }
        String nearestText = app.safePlaces.findNearestPlace()
                .map(place -> escape(place.getName()) + " (" + place.getDistance() + " km)")
                .orElse("No safe-place records are configured.");
        return "<section class='card'><h1>Sample safe places</h1><nav class='filters'>" +
                filterLink("ALL", type) + filterLink("Police", type) + filterLink("Hospital", type) + filterLink("Shop", type) +
                "</nav><div class='tablewrap'><table><thead><tr><th>ID</th><th>Name</th><th>Type</th><th>Area</th><th>Distance</th><th>Call</th></tr></thead><tbody>" +
                rows + "</tbody></table></div><p><strong>Nearest listed place:</strong> " + nearestText +
                "</p>" + emergencyNumbersForm(numbers) +
                "<p class='warning'>Place names/distances are sample data. Call links use the local number you entered and do not guarantee reaching that specific place.</p></section>";
    }

    private String emergencyNumbersForm(String[] numbers) {
        return "<details class='numbers-settings'><summary>Set verified local call numbers</summary>" +
                "<p class='muted'>Enter numbers you have verified for your current country or region. These are saved on this computer.</p>" +
                "<form method='post' class='formrow'><input type='hidden' name='action' value='saveEmergencyNumbers'>" +
                "<label>Police number<input name='police' inputmode='tel' pattern='[+]?[0-9 ()-]{3,24}' value='" + escape(numbers[1]) + "'></label>" +
                "<label>Hospital number<input name='hospital' inputmode='tel' pattern='[+]?[0-9 ()-]{3,24}' value='" + escape(numbers[2]) + "'></label>" +
                "<button class='button' type='submit'>Save numbers</button></form></details>";
    }

    private String callLink(String label, String phone) {
        if (phone == null || phone.isBlank()) return "<span class='muted'>Set a verified number to enable " + escape(label.toLowerCase()) + ".</span>";
        String dialable = phone.replaceAll("[^+0-9]", "");
        if (dialable.isBlank()) return "<span class='muted'>Set a valid number to enable " + escape(label.toLowerCase()) + ".</span>";
        return "<a class='button' href='tel:" + escape(dialable) + "' onclick='return confirmCall(this)'>" + escape(label) + "</a>";
    }

    private String filterLink(String value, String current) {
        String type = value.equalsIgnoreCase("ALL") ? "ALL" : value;
        return "<a class='chip " + (type.equalsIgnoreCase(current) ? "active" : "") + "' href='/?page=places&type=" +
                escape(type) + "'>" + escape(type) + "</a>";
    }

    private String routePage() {
        StringBuilder options = new StringBuilder();
        for (Location l : app.locations.getAvailableLocations()) {
            options.append("<option value='").append(escape(l.getLocationId())).append("'>").append(escape(l.getAddress())).append("</option>");
        }
        String notice = requestNotice.get();
        return "<section class='card'><h1>Safe route helper</h1><p class='muted'>Create an illustrative route from the local sample list.</p>" +
                "<form method='post' class='formrow'><input type='hidden' name='action' value='route'>" +
                "<label>Start<select name='start' required>" + options + "</select></label>" +
                "<label>Destination<select name='destination' required>" + options + "</select></label>" +
                "<button class='button' type='submit'>Suggest route</button></form>" +
                (notice.startsWith("Suggested route:") ? "<p class='route-result'>" + escape(notice) + "</p>" : "") +
                "<p class='warning'>Illustrative only—not map navigation, verified route advice, or a safety guarantee.</p></section>";
    }

    private String historyPage() {
        StringBuilder rows = new StringBuilder();
        for (String[] r : app.files.readAlerts()) {
            rows.append("<tr><td>").append(escape(r[0])).append("</td><td>").append(escape(r[1]))
                    .append("</td><td>").append(escape(r[3])).append("</td><td>").append(escape(r[4]))
                    .append("</td><td>").append(escape(r[2])).append("</td><td>").append(escape(r[5])).append("</td></tr>");
        }
        if (rows.isEmpty()) rows.append("<tr><td colspan='6'>No alert history yet.</td></tr>");
        return "<section class='card'><h1>Alert history</h1><div class='tablewrap'><table><thead><tr><th>Alert ID</th><th>User</th><th>Date</th><th>Time</th><th>Location</th><th>Status</th></tr></thead><tbody>" +
                rows + "</tbody></table></div><p class='muted'>Saved locally to data/alert_history.txt.</p></section>";
    }

    private void sendSos() {
        if (requestUser.get().getCurrentLocation() == null) {
            requestNotice.set("SOS was not recorded. Update your sample location first.");
            return;
        }
        try {
            var alert = app.alerts.sendSOS(requestUser.get());
            requestNotice.set("SOS simulation recorded: " + alert.getAlertId() + " · " + alert.getStatus() +
                    ". Simulated only; no contacts or emergency services were contacted.");
        } catch (NoContactException e) {
            requestNotice.set("SOS was not recorded: " + e.getMessage());
        }
    }

    private void addContact(Map<String, String> form) {
        String name = form.getOrDefault("name", "").trim();
        String relation = form.getOrDefault("relation", "").trim();
        String phone = form.getOrDefault("phone", "").trim();
        if (name.isBlank() || relation.isBlank() || !phone.matches("\\+?[0-9]{10,15}")) {
            requestNotice.set("Enter a name, relation, and valid 10–15 digit phone number.");
            return;
        }
        User user = requestUser.get();
        if (user.getTrustedContacts().stream().anyMatch(c -> c.getPhone().equals(phone))) {
            requestNotice.set("A contact with that phone number already exists.");
            return;
        }
        user.addTrustedContact(new TrustedContact("C" + UUID.randomUUID().toString().substring(0, 7), name, relation, phone));
        app.files.writeContacts(user.getUserId(), user.getTrustedContacts());
        requestNotice.set("Trusted contact added.");
    }

    private void removeContact(Map<String, String> form) {
        User user = requestUser.get();
        if (user.removeTrustedContact(form.getOrDefault("id", ""))) {
            app.files.writeContacts(user.getUserId(), user.getTrustedContacts());
            requestNotice.set("Trusted contact removed.");
        } else requestNotice.set("That contact was not found.");
    }

    private void updateLocation(Map<String, String> form) {
        String id = form.getOrDefault("locationId", "");
        Location selected = app.locations.getAvailableLocations().stream()
                .filter(location -> location.getLocationId().equals(id)).findFirst().orElse(null);
        if (selected == null) {
            requestNotice.set("Choose a listed location.");
            return;
        }
        User user = requestUser.get();
        app.locations.updateLocation(user, selected);
        requestNotice.set("Location updated to " + user.getCurrentLocation().getAddress() + ".");
    }

    private void updateLiveLocation(Map<String, String> form) {
        try {
            double latitude = Double.parseDouble(form.getOrDefault("latitude", ""));
            double longitude = Double.parseDouble(form.getOrDefault("longitude", ""));
            if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                    || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
                requestNotice.set("Browser returned invalid location. No location was saved.");
                return;
            }

            Location location = new Location("LIVE", latitude, longitude, "Live location (private)");
            app.locations.updateLocation(requestUser.get(), location);
            requestNotice.set("Live location updated and saved on this computer. Coordinates are hidden.");
        } catch (NumberFormatException e) {
            requestNotice.set("Could not read your location. Please allow location access and try again.");
        }
    }

    private void saveEmergencyNumbers(Map<String, String> form) {
        String police = normalizePhone(form.getOrDefault("police", ""));
        String hospital = normalizePhone(form.getOrDefault("hospital", ""));
        if (police == null || hospital == null) {
            requestNotice.set("Enter valid phone numbers using digits and an optional leading +. Leave a field empty if unknown.");
            return;
        }
        app.files.writeEmergencyNumbers(requestUser.get().getUserId(), police, hospital);
        requestNotice.set("Local call numbers saved. Verify them for your current region before calling.");
    }

    private String normalizePhone(String value) {
        String phone = value.trim().replaceAll("[ ()-]", "");
        if (phone.isEmpty()) return "";
        return phone.matches("\\+?[0-9]{3,20}") ? phone : null;
    }

    private void showRoute(Map<String, String> form) {
        String startId = form.getOrDefault("start", "");
        String destinationId = form.getOrDefault("destination", "");
        Location start = app.locations.getAvailableLocations().stream().filter(l -> l.getLocationId().equals(startId)).findFirst().orElse(null);
        Location destination = app.locations.getAvailableLocations().stream().filter(l -> l.getLocationId().equals(destinationId)).findFirst().orElse(null);
        if (start == null || destination == null || startId.equals(destinationId)) {
            requestNotice.set("Choose different valid start and destination locations.");
            return;
        }
        StringBuilder route = new StringBuilder("Suggested route: ").append(start.getAddress());
        for (SafePlace place : app.safePlaces.getNearbySafePlaces()) route.append(" → ").append(place.getName());
        route.append(" → ").append(destination.getAddress()).append(". Nearby sample safe places are included.");
        requestNotice.set(route.toString());
    }

    private String pageShell(String title, String content) {
        String navigation;
        if (requestUser.get() == null) {
            navigation = "<nav><a href='/?page=login'>Sign in</a><a href='/?page=register'>Create account</a></nav>";
        } else {
            navigation = "<nav><a href='/?page=home'>Home</a><a href='/?page=contacts'>Contacts</a>" +
                    "<a href='/?page=location'>Location</a><a href='/?page=places'>Safe places</a>" +
                    "<a href='/?page=route'>Route</a><a href='/?page=history'>History</a>" +
                    "<form method='post' class='logout'><input type='hidden' name='action' value='logout'>" +
                    "<button class='small' type='submit'>Sign out</button></form></nav>";
        }
        return """
                <!doctype html><html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>%s</title><style>
                *{box-sizing:border-box}body{margin:0;background:#f4f7f9;color:#1c3149;font:15px "Segoe UI",Arial,sans-serif}
                header{background:#fff;padding:18px max(24px,calc((100%% - 1080px)/2));display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #e3e9ed}
                .brand{font-size:18px;font-weight:700;letter-spacing:.4px}.brand small{display:block;color:#007d7d;font-size:10px;letter-spacing:1.2px;margin-top:4px}
                nav{display:flex;align-items:center;gap:14px}nav a{color:#1c3149;text-decoration:none;font-size:13px}.logout{display:inline}
                main{max-width:1080px;margin:30px auto;padding:0 24px}
                h1{font-size:27px;margin:0 0 14px}h2{font-size:17px;margin:0 0 8px}.muted{color:#61717d}.eyebrow{color:#007d7d;font-weight:700;letter-spacing:1px;font-size:11px}
                .hero,.card{background:#fff;border:1px solid #e0e7eb;border-radius:12px;padding:24px;margin-bottom:18px}
                .hero{padding:28px 34px}.hero h1{font-size:34px}.sos{background:#be2337;color:#fff;border:0;border-radius:8px;padding:15px 25px;font-size:18px;font-weight:700;cursor:pointer;margin-top:8px}
                .call-actions{display:flex;gap:10px;flex-wrap:wrap}.numbers-settings{margin-top:16px;padding:14px;background:#f4f7f9;border-radius:8px}.numbers-settings summary{cursor:pointer;font-weight:700}
                .warning{color:#795c24;background:#fff7e1;padding:11px 13px;border-radius:6px;font-size:12px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:14px}
                .linkcard{color:inherit;text-decoration:none;transition:transform .12s}.linkcard:hover{transform:translateY(-2px);border-color:#8bbfbd}
                .button,.small{display:inline-block;background:#007d7d;color:#fff;border:0;border-radius:6px;padding:10px 15px;text-decoration:none;font-weight:600;cursor:pointer}
                .danger{background:#a7283a;padding:7px 10px}.notice{background:#e3f4ee;border:1px solid #c6e9d9;color:#1d6046;border-radius:8px;padding:12px 16px;margin-bottom:16px}
                .auth{max-width:480px;margin:30px auto}.auth a{color:#007d7d}.small{padding:7px 10px;font-size:12px}
                .tablewrap{overflow-x:auto}table{width:100%%;border-collapse:collapse;margin:14px 0}th,td{text-align:left;padding:10px;border-bottom:1px solid #e6ecef}th{background:#f4f7f9}
                .formrow{display:flex;gap:12px;align-items:end;flex-wrap:wrap}.stack{display:grid;gap:14px;max-width:420px}label{display:grid;gap:6px;color:#435666;font-weight:600}
                input,select{font:inherit;padding:10px;border:1px solid #cbd5dc;border-radius:6px;min-width:150px}.chip{display:inline-block;padding:7px 12px;border-radius:18px;background:#edf3f5;color:#28485e;text-decoration:none;margin:0 6px 8px 0}
                .chip.active{background:#007d7d;color:#fff}.route-result{padding:15px;background:#edf7f5;border-radius:8px;line-height:1.8}
                footer{max-width:1080px;margin:20px auto;padding:0 24px 24px;color:#71808a;font-size:11px}
                @media(max-width:650px){header{align-items:flex-start;gap:12px;flex-direction:column}nav a{margin:0 10px 0 0}main{margin:16px auto}.hero{padding:20px}}
                </style><script>function confirmCall(link){return window.confirm('Open your device dialer to call '+link.textContent+'? This app does not place calls automatically.');}</script></head><body><header><a href="/" style="color:inherit;text-decoration:none"><span class="brand">WOMEN SAFETY<small>SOS & SAFE ROUTE HELPER</small></span></a>
                %s</header>
                <main>%s</main><footer>Local educational simulation only. It does not contact emergency services, send messages, use live GPS, or guarantee safe routes.</footer>
                </body></html>
                """.formatted(escape(title), navigation, content);
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }

    private static Map<String, String> parseForm(String data) {
        Map<String, String> result = new HashMap<>();
        if (data == null || data.isBlank()) return result;
        for (String entry : data.split("&")) {
            String[] pair = entry.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
