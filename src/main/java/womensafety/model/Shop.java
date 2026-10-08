package womensafety.model;

public class Shop extends SafePlace {
    private final boolean open24Hours;
    public Shop(String id, String name, String location, double distance, boolean open24Hours) {
        super(id, name, location, distance, "Shop");
        this.open24Hours = open24Hours;
    }
    @Override public String displayDetails() {
        return super.displayDetails() + (open24Hours ? " | Open 24 hours" : "");
    }
}
