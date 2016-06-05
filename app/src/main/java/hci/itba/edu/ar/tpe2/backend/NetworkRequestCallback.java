package hci.itba.edu.ar.tpe2.backend;

import android.content.Context;

/**
 * Class used for passing callback functions to asynchronous network requests.
 *
 * @param <T> Type of parameter that the callback function receives, Void if none.
 */
public interface NetworkRequestCallback<T> {

    public void execute(Context c, T param);
}
