package hci.itba.edu.ar.tpe2.backend;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class used for storing essential data for proper app functionality (e.g. cities, airports,
 * languages, currencies) in the device's internal storage.
 */
public class FileManager {
    public enum StorageFile {CITIES, COUNTRIES, AIRPORTS, LANGUAGES, CURRENCIES};
    private Context context;

    public FileManager(Context c) {
        this.context = c;
    }

    public boolean saveCities(City[] cities) {
        try {
            return saveObjects(cities, StorageFile.CITIES);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public City[] loadCities() {
        List<City> result = new ArrayList<>();
        try {
            loadObjects(context, StorageFile.CITIES, result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result.toArray(new City[0]);     //Empty array instead of length-sized array http://stackoverflow.com/a/4042464/2333689
    }

    public boolean saveLanguages(Language[] cities) {
        try {
            return saveObjects(cities, StorageFile.LANGUAGES);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Language[] loadLanguages() {
        List<Language> result = new ArrayList<>();
        try {
            loadObjects(context, StorageFile.LANGUAGES, result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result.toArray(new Language[0]);
    }

    private boolean saveObjects(Serializable[] objects, StorageFile destFile) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(destFile.name().toLowerCase(), Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeInt(objects.length);
            for(Serializable o : objects) {
                oos.writeObject(o);
            }
            Log.i(API.LOG_TAG, "Saved " + objects.length + " objects to " + destFile);
        } catch (FileNotFoundException e) {
            Log.wtf(API.LOG_TAG, "Wut, " + destFile + " file not found, even though we're creating it...");
            return false;
        }
        finally {
            if(fos != null) {
                fos.close();
            }
            if(oos != null) {
                oos.close();
            }
        }
        return true;
    }

    private static <T> boolean loadObjects(Context context, StorageFile srcFile, Collection<T> dest) throws IOException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(srcFile.name().toLowerCase());
            ois = new ObjectInputStream(fis);
            int numObjects = ois.readInt();
            for(int i = 0; i < numObjects; i++) {
                dest.add((T) ois.readObject());
            }
            Log.i(API.LOG_TAG, "Read " + numObjects + " objects from " + srcFile);
        } catch (FileNotFoundException e) {
            Log.wtf(API.LOG_TAG, "Wut, " + srcFile + " file not found, even though we're creating it...");
            return false;
        } catch (ClassNotFoundException e) {
            Log.e(API.LOG_TAG, "Error reading objects from " + srcFile + ": " + e.getMessage());
            return false;
        } finally {
            if(fis != null) {
                fis.close();
            }
            if(ois != null) {
                ois.close();
            }
        }
        return true;
    }
}
