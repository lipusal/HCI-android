package hci.itba.edu.ar.tpe2.backend.data;

import java.io.Serializable;

public class Airline implements Serializable {
    private String id, name, logo;
    private double taxes, charges, rating;

    public Airline(String id, String name, String logo, double taxes, double charges, double rating) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.taxes = taxes;
        this.charges = charges;
        this.rating = rating;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogoURL() {
        return logo;
    }

    public double getTaxes() {
        return taxes;
    }

    public double getCharges() {
        return charges;
    }

    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
