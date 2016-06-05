package hci.itba.edu.ar.tpe2.backend;

import java.io.Serializable;

public class Language implements Serializable {
    private String id, name;

    public Language(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
