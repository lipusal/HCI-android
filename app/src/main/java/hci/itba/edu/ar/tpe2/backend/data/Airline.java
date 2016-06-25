package hci.itba.edu.ar.tpe2.backend.data;

import java.io.Serializable;

import hci.itba.edu.ar.tpe2.R;

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

    public int getDrawableLogoID() {
        switch (id) {
            case "AR":
                return R.drawable.logo_ar;
            case "LA":
                return R.drawable.logo_la;
            case "8R":
                return R.drawable.logo_8r;
            case "JJ":
                return R.drawable.logo_jj;
            case "BA":
                return R.drawable.logo_ba;
            case "AF":
                return R.drawable.logo_af;
            case "AZ":
                return R.drawable.logo_az;
            case "AA":
                return R.drawable.logo_aa;
            case "IB":
                return R.drawable.logo_ib;
            case "AM":
                return R.drawable.logo_am;
            case "TA":
                return R.drawable.logo_ta;
            case "CM":
                return R.drawable.logo_cm;
            case "AV":
                return R.drawable.logo_av;
            default:
                return R.drawable.ic_flight;
        }
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
