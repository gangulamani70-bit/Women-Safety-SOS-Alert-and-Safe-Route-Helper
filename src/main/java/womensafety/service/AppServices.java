package womensafety.service;

import womensafety.util.FileManager;

public class AppServices {
    public final FileManager files;
    public final UserService users;
    public final LocationService locations;
    public final SafePlaceService safePlaces;
    public final AlertService alerts;

    public AppServices(FileManager files) {
        this.files = files;
        this.users = new UserService(files);
        this.locations = new LocationService(files);
        this.safePlaces = new SafePlaceService(files);
        this.alerts = new AlertService(files);
    }
}
