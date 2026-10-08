package womensafety.service;

import womensafety.model.Hospital;
import womensafety.model.Police;
import womensafety.model.SafePlace;
import womensafety.model.Shop;
import womensafety.util.FileManager;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SafePlaceService {
    private final List<SafePlace> places = new ArrayList<>();

    public SafePlaceService(FileManager files) {
        for (String[] row : files.readSafePlaces()) {
            double distance = Double.parseDouble(row[4]);
            switch (row[1]) {
                case "Police" -> places.add(new Police(row[0], row[2], row[3], distance, row[5]));
                case "Hospital" -> places.add(new Hospital(row[0], row[2], row[3], distance, Boolean.parseBoolean(row[5])));
                case "Shop" -> places.add(new Shop(row[0], row[2], row[3], distance, Boolean.parseBoolean(row[5])));
                default -> throw new IllegalStateException("Unknown safe place type in local data: " + row[1]);
            }
        }
    }

    public void addSafePlace(SafePlace place) { places.add(place); }
    public List<SafePlace> getAllSafePlaces() { return places.stream().sorted(Comparator.comparingDouble(SafePlace::getDistance)).toList(); }
    public Optional<SafePlace> findNearestPlace() { return getAllSafePlaces().stream().findFirst(); }
    public List<SafePlace> findByType(String type) {
        return places.stream().filter(p -> p.getType().equalsIgnoreCase(type))
                .sorted(Comparator.comparingDouble(SafePlace::getDistance)).toList();
    }
    public List<SafePlace> getNearbySafePlaces() { return getAllSafePlaces().stream().limit(3).toList(); }
}
