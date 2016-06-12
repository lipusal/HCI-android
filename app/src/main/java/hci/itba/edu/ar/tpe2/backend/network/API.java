package hci.itba.edu.ar.tpe2.backend.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hci.itba.edu.ar.tpe2.backend.FileManager;
import hci.itba.edu.ar.tpe2.backend.data.Airline;
import hci.itba.edu.ar.tpe2.backend.data.Airport;
import hci.itba.edu.ar.tpe2.backend.data.City;
import hci.itba.edu.ar.tpe2.backend.data.Currency;
import hci.itba.edu.ar.tpe2.backend.data.Country;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.Deal;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.Language;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.data.Place;
import hci.itba.edu.ar.tpe2.backend.data.Review;

/**
 * Singleton class used for making requests to the API.
 */
public class API {
    public static final int DEFAULT_RADIUS = 2;
    private static API instance = new API();
    private static DateFormat APIdateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static Gson gson = new Gson();
    private static final int DEFAULT_PAGE_SIZE = 30;

    private API() {}

    /**
     * Supported API methods.
     */
    public enum Method {
        getairlines, getcities, getairports, getcountries, getlanguages, getcurrencies, getflightstatus, getlastminuteflightdeals, getairportsbyposition, getcitiesbyposition, getairlinereviews, reviewairline2
    }

    /**
     * Services that the supported methods belong to.
     */
    public enum Service {
        misc, geo, booking, review, status
    }

    public static final String API_BASE_URL = "http://eiffel.itba.edu.ar/hci/service4/",
                                LOG_TAG = "VOLANDO";

    /**
     * @return The singleton instance.
     */
    public static API getInstance() {
        return instance;
    }

    /**
     * Queries the API for flights with the specified criteria.
     *
     * @param departureID   Valid origin ID (city or airport)
     * @param arrivalID     Valid destinarion ID (city or airport)
     * @param departureDate Departure date in YYYY-MM-DD format.
     * @param airlineID     Search only flights with this airline ID (optional, specify null to ignore)
     * @param context       The context under which to run the specified callback.
     * @param callback      The callback to run, once flights have been fetched. The callback is passed
     *                      the returned flights.
     */
    public void getAllFlights(String departureID, String arrivalID, /*Date */String departureDate, String airlineID, final Context context, final NetworkRequestCallback<List<Flight>> callback) {
        final Service service = Service.booking;
        final Bundle params = new Bundle();
        params.putString("method", "getonewayflights");
        params.putString("from", departureID);
        params.putString("to", arrivalID);
        params.putString("dep_date", departureDate/*APIdateFormat.format(departureDate)*/);
        if (airlineID != null) {
            params.putString("airline_id", airlineID);
        }
        //TODO OK to hardcode these?
        params.putString("adults", "1");
        params.putString("children", "0");
        params.putString("infants", "0");
        //Count flights to make sure we get all of them at once
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, final Integer total) {
                if (total == 0) {    //No flights found, don't make a 2nd request
                    if (callback != null) {
                        callback.execute(context, Collections.EMPTY_LIST);
                    }
                    return;
                }
                params.putString("page_size", Integer.toString(total));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String result) {
                        if (callback == null) {
                            Log.d("VOLANDO", "Requested flights with no callback, useless network request =(");
                            return;
                        }
                        //Got all flights now, parse them
                        JsonArray data = gson.fromJson(result, JsonObject.class).getAsJsonArray("flights");
                        List<Flight> flights = new ArrayList<Flight>(total);
                        for (JsonElement flight : data) {
                            flights.add(Flight.fromJson(flight.getAsJsonObject()));
                        }
                        callback.execute(context, flights);
                    }

                    @Override
                    protected void errorCallback(String result) {
                        Log.w("VOLANDO", "Error searching flights:");
                        Log.w("VOLANDO", result);
                        callback.execute(context, Collections.EMPTY_LIST);
                    }
                }.execute();
            }
        });
    }

    /**
     * Queries the status of the flight with the specified information.
     *
     * @param airlineId The flight's airline ID.
     * @param flightNum The flight number.
     * @param context Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     */
    public void getFlightStatus(String airlineId, int flightNum, final Context context, final NetworkRequestCallback<FlightStatus> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getflightstatus.name());
        params.putString("airline_id", airlineId);
        params.putString("flight_number", Integer.toString(flightNum));
        new APIRequest(Service.status, params) {
            @Override
            protected void successCallback(String result) {
                JsonObject responseJson = API.gson.fromJson(result, JsonObject.class);
                FlightStatus status = FlightStatus.fromJson(responseJson.getAsJsonObject("status"));
                if (callback != null) {
                    callback.execute(context, status);
                }
            }
        }.execute();
    }

    /**
     * Queries the status of the flight with the specified information.
     *
     * @see #getFlightStatus(String, int, Context, NetworkRequestCallback)
     */
    public void getFlightStatus(Flight flight, final Context context, final NetworkRequestCallback<FlightStatus> callback) {
        getFlightStatus(flight.getAirline().getID(), flight.getNumber(), context, callback);
    }

    /**
     * Fetches all airlines provided by the API. Airlines are passed to the specified callback.
     *
     * @param context Context under which to run the specified callback.
     * @param callback Function to execute when network request completes.
     */
    public void getAllAirlines(final Context context, final NetworkRequestCallback<Airline[]> callback) {
        final Service service = Service.misc;
        final Bundle params = new Bundle();
        params.putString("method", Method.getairlines.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer airlineCount) {
                params.putString("page_size", Integer.toString(airlineCount));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String result) {
                        JsonObject data = gson.fromJson(result, JsonObject.class);
                        Airline[] airlines = gson.fromJson(data.get("airlines"), Airline[].class);
                        if(callback != null) {
                            callback.execute(context, airlines);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Fetches all cities provided by the API. Cities are passed to the specified callback.
     *
     * @param context Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     */
    public void getAllCities(final Context context, final NetworkRequestCallback<City[]> callback) {
        final Service service = Service.geo;
        final Bundle params = new Bundle();
        params.putString("method", Method.getcities.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer cityCount) {
                //TODO handle null
                //Now that we have the total, fetch again with a page of size <cityCount>
                params.putString("page_size", Integer.toString(cityCount));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String result) {
                        int startIndex = result.indexOf("cities") + 8,   //Start at cities' [
                                endIndex = result.lastIndexOf(']') + 1;         //End at cities' ]
                        String data = result.substring(startIndex, endIndex);
                        City[] cities = gson.fromJson(data, City[].class);
                        //TODO stop saving them here, save them in the callback if anything
                        if(callback != null) {
                            callback.execute(context, cities);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Gets all currencies as returned by the API.
     *
     * @param context Context under which to run the specified callback.
     * @param callback Function to run when the network request completes. It will be passed the
     *                 returned currencies.
     */
    public void getAllCurrencies(final Context context, final NetworkRequestCallback<Currency[]> callback) {
        final Service service = Service.misc;
        final Bundle params = new Bundle();
        params.putString("method", Method.getcurrencies.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer currencyCount) {
                params.putString("page_size", Integer.toString(currencyCount));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String result) {
                        if(callback != null) {
                            //TODO make all success callbacks work like this (don't process data if there's no callback)
                            JsonObject json = gson.fromJson(result, JsonObject.class);
                            Currency[] currencies = gson.fromJson(json.get("currencies"), Currency[].class);
                            callback.execute(context, currencies);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Fetches all countries provided by the API. Countries are passed to the specified callback.
     *
     * @param context Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     */
    public void getAllCountries(final Context context, final NetworkRequestCallback<Country[]> callback) {
        final Service service = Service.geo;
        final Bundle params = new Bundle();
        params.putString("method", Method.getcountries.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer count) {
                params.putString("page_size", Integer.toString(count));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String data) {
                        JsonObject json = gson.fromJson(data, JsonObject.class);
                        Country[] result = gson.fromJson(json.get("countries"), Country[].class);
                        if (callback != null) {
                            callback.execute(context, result);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Fetches all airports provided by the API. Airports are passed to the specified callback.
     *
     * @param context  Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     */
    public void getAllAirports(final Context context, final NetworkRequestCallback<Airport[]> callback) {
        final Service service = Service.geo;
        final Bundle params = new Bundle();
        params.putString("method", Method.getairports.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer count) {
                params.putString("page_size", Integer.toString(count));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String data) {
                        JsonObject json = gson.fromJson(data, JsonObject.class);
                        Airport[] result = gson.fromJson(json.get("airports"), Airport[].class);
                        if(callback != null) {
                            callback.execute(context, result);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Fetches all languages provided by the API. Languages are passed to the specified callback.
     *
     * @param context Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     */
    public void getLanguages(final Context context, final NetworkRequestCallback<Language[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getlanguages.name());
        new APIRequest(Service.misc, params) {
            @Override
            protected void successCallback(String result) {
                int startIndex = result.indexOf("languages") + 11,
                        endIndex = result.lastIndexOf(']') + 1;
                String data = result.substring(startIndex, endIndex);
                Language[] langs = gson.fromJson(data, Language[].class);
                //TODO stop saving them here, save them in the callback if anything
                if(new FileManager(context).saveLanguages(langs)) {
                    if(callback != null) {
                        callback.execute(context, langs);
                    }
                }
                else {
                    Log.w(LOG_TAG, "Couldn't save languages.");
                }
            }
        }.execute();
    }

    /**
     * Gets all airports near the specified location within the specified radius. <b>NOTE:</b> API
     * returns incomplete airports, this function maps the returned airports to the complete airport
     * objects stored in local storage.
     *
     * @param latitude
     * @param longitude
     * @param radius In km
     * @param context Context under which to run the specified callback.
     * @param callback Function to run when the network request completes. Will get passed the
     *                 returned (complete) Airport objects.
     */
    public void getAirportsByLocation(double latitude, double longitude, int radius, final Context context, final NetworkRequestCallback<Airport[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getairportsbyposition.name());
        params.putString("latitude", Double.toString(latitude));
        params.putString("longitude", Double.toString(longitude));
        params.putString("radius", Integer.toString(radius));
        new APIRequest(Service.geo, params) {
            @Override
            protected void successCallback(String data) {
                if(callback != null) {
                    JsonObject json = gson.fromJson(data, JsonObject.class);
                    JsonArray airports = json.getAsJsonArray("airports");
                    List<Airport> result = new ArrayList<>();
                    Map<String, Airport> completeAirports = PersistentData.getInstance().getAirports();
                    if (completeAirports == null) {
                        throw new IllegalStateException("Airports not stored in local storage, can't search airports by location.");
                    }
                    for (JsonElement airport : airports) {
                        result.add(completeAirports.get(airport.getAsJsonObject().get("id").getAsString()));
                    }
                    if(callback != null) {
                        callback.execute(context, result.toArray(new Airport[0]));
                    }
                }
            }
        }.execute();
    }

    /**
     * Gets all airports within a {@code DEFAULT_RADIUS}-km radius.
     *
     * @see #getAirportsByLocation(double, double, int, Context, NetworkRequestCallback)
     */
    public void getAirportsByLocation(double latitude, double longitude, final Context context, final NetworkRequestCallback<Airport[]> callback) {
        getAirportsByLocation(latitude, longitude, DEFAULT_RADIUS, context, callback);
    }

    /**
     * Gets all cities near the specified location within the specified radius. <b>NOTE:</b> API
     * returns incomplete cities, this function maps the returned cities to the complete city
     * objects stored in local storage.
     *
     * @param latitude
     * @param longitude
     * @param radius In km
     * @param context Context under which to run the specified callback.
     * @param callback Function to run when the network request completes. Will get passed the
     *                 returned (complete) City objects.
     */
    public void getCitiesByLocation(double latitude, double longitude, int radius, final Context context, final NetworkRequestCallback<City[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getcitiesbyposition.name());
        params.putString("latitude", Double.toString(latitude));
        params.putString("longitude", Double.toString(longitude));
        params.putString("radius", Integer.toString(radius));
        new APIRequest(Service.geo, params) {
            @Override
            protected void successCallback(String data) {
                if(callback != null) {
                    JsonObject json = gson.fromJson(data, JsonObject.class),
                            cities = json.getAsJsonObject("cities");
                    List<City> result = new ArrayList<>();
                    Map<String, City> completeCities = PersistentData.getInstance().getCities();
                    if(completeCities == null) {
                        throw new IllegalStateException("Cities not stored in local storage, can't search cities by location.");
                    }
                    for(JsonElement city : json.getAsJsonArray("cities")) {
                        result.add(completeCities.get(city.getAsJsonObject().get("id").getAsString()));
                    }
                    if(callback != null) {
                        callback.execute(context, result.toArray(new City[0]));
                    }
                }
            }
        }.execute();
    }

    /**
     * Gets all cities within a {@code DEFAULT_RADIUS}-km radius.
     *
     * @see #getCitiesByLocation(double, double, int, Context, NetworkRequestCallback)
     */
    public void getCitiesByLocation(double latitude, double longitude, final Context context, final NetworkRequestCallback<City[]> callback) {
        getCitiesByLocation(latitude, longitude, DEFAULT_RADIUS, context, callback);
    }

    /**
     * Gets last-minute flight deals from the specified place.
     * @param from The place from which to search for deals, airport or city.
     * @param context The context under which to run the specified callback.
     * @param callback Function to run once network request is complete.
     */
    public void getDeals(Place from, final Context context, final NetworkRequestCallback<Deal[]> callback) {
        getDeals(from.getID(), context, callback);
    }

    /**
     * Gets last-minute flight deals from the specified origin.
     *
     * @param fromID Valid ID of origin (city or airport)
     * @param context The context under which to run the specified callback.
     * @param callback Function to run once network request is complete.
     */
    public void getDeals(String fromID, final Context context, final NetworkRequestCallback<Deal[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getlastminuteflightdeals.name());
        params.putString("from", fromID);
        new APIRequest(Service.booking, params) {
            @Override
            protected void successCallback(String result) {
                Gson g = new Gson();
                JsonObject json = g.fromJson(result, JsonObject.class);
                Deal[] deals = g.fromJson(json.get("deals"), Deal[].class);
                if(callback != null) {
                    callback.execute(context, deals);
                }
            }
        }.execute();
    }

    /**
     * Gets a subset of all reviews for the specified flight.
     *
     * @param flight The flight for which to search reviews.
     * @param pageNumber The page number of reviews to fetch.
     * @param pageSize The number of reviews per page.
     * @param context The context under which to run the specified callback.
     * @param callback Function to run once the network request completes. Will get passed the found
     *                 reviews.
     */
    public void getPageOfReviews(Flight flight, int pageNumber, int pageSize, final Context context, final NetworkRequestCallback<Review[]> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getairlinereviews.name());
        params.putString("airline_id", flight.getAirline().getID());
        params.putString("flight_number", Integer.toString(flight.getNumber()));
        params.putString("page_size", Integer.toString(pageSize));
        params.putString("page", Integer.toString(pageNumber));
        new APIRequest(Service.review, params) {
            @Override
            protected void successCallback(String result) {
                JsonObject json = gson.fromJson(result, JsonObject.class);
                JsonArray reviewsJson = json.getAsJsonArray("reviews");
                Review[] reviews = new Review[reviewsJson.size()];
                for (int i = 0; i < reviewsJson.size(); i++) {
                    reviews[i] = Review.fromJson(reviewsJson.get(i).getAsJsonObject());
                }
                if(callback != null) {
                    callback.execute(context, reviews);
                }
            }
        }.execute();
    }

    /**
     * Gets <b>all</b> the reviews for the specified flight.
     *
     * @param flight The flight for which to search for reviews.
     * @param context Context under which to run the specified callback.
     * @param callback Function to run once the network request completes. Will get passed the
     *                 found reviews.
     */
    public void getAllReviews(Flight flight, final Context context, final NetworkRequestCallback<Review[]> callback) {
        final Bundle params = new Bundle();
        params.putString("method", Method.getairlinereviews.name());
        params.putString("airline_id", flight.getAirline().getID());
        params.putString("flight_number", Integer.toString(flight.getNumber()));
        final Service service = Service.review;
        count(service, params, context, new NetworkRequestCallback<Integer>() {
            @Override
            public void execute(Context c, Integer count) {
                params.putString("page_size", Integer.toString(count));
                new APIRequest(service, params) {
                    @Override
                    protected void successCallback(String result) {
                        JsonObject json = gson.fromJson(result, JsonObject.class);
                        JsonArray reviewsJson = json.getAsJsonArray("reviews");
                        Review[] reviews = new Review[reviewsJson.size()];
                        for (int i = 0; i < reviewsJson.size(); i++) {
                            reviews[i] = Review.fromJson(reviewsJson.get(i).getAsJsonObject());
                        }
                        if(callback != null) {
                            callback.execute(context, reviews);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Submits the specified review to the server.
     *
     * @param review The review to publish.
     * @param context Context under which to run the specified callback, if any.
     * @param callback Function to run once the network request completes.
     */
    public void submitReview(Review review, final Context context, final NetworkRequestCallback<Void> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.reviewairline2.name());
        params.putString("review", review.toJson());
        new APIRequest(Service.review, params) {
            @Override
            protected void successCallback(String result) {
                if(callback != null) {
                    callback.execute(context, null);
                }
            }
        }.execute();
    }

    /**
     * Gets the count of results returned by the specified query, or {@code null} if the query
     * doesn't specify a total, and passes it to the specified callback function.
     *
     * @param service The service the request is for.
     * @param requestParams Request parameters.
     * @param context Context to run the callback with.
     * @param callback Callback which will receive the total, or {@code null} if not specified in
     *                 the request's response.
     */
    public void count(Service service, Bundle requestParams, final Context context, final NetworkRequestCallback<Integer> callback) {
        new APIRequest(service, requestParams) {
            @Override
            protected void successCallback(String result) {
                if(callback != null) {
                    JsonObject json = gson.fromJson(result, JsonObject.class);
                    JsonElement totalObj = json.get("total");
                    Integer total = null;
                    if (totalObj != null) {
                        total = totalObj.getAsInt();
                    }
                    callback.execute(context, total);
                }
            }
        }.execute();
    }
}