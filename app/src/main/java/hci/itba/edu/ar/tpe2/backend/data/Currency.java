package hci.itba.edu.ar.tpe2.backend.data;

public class Currency {
    private String id, description, symbol;
    private double ratio;

    public Currency(String id, String description, String symbol, double ratio) {
        this.id = id;
        this.description = description;
        this.symbol = symbol;
        this.ratio = ratio;
    }

    public String getID() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getRatio() {
        return ratio;
    }
}
