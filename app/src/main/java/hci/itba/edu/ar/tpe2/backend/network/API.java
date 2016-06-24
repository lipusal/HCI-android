package hci.itba.edu.ar.tpe2.backend.network;

import android.content.Context;
import android.net.Network;
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

    private API() {
    }

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
     * @param departureID     Valid origin ID (city or airport)
     * @param arrivalID       Valid destinarion ID (city or airport)
     * @param departureDate   Departure date in YYYY-MM-DD format.
     * @param airlineID       Search only flights with this airline ID (optional, specify null to ignore)
     * @param context         The context under which to run the specified callback.
     * @param successCallback The callback to run, once flights have been successfully fetched.
     *                        Callback is passed the returned flights.
     * @param errorCallback   The callback to run if there was an error (i.e. network timeout).
     *                        Callback will be passed the resulting JSON or default API request
     *                        error message.
     */
    public void getAllFlights(String departureID, String arrivalID, /*Date */String departureDate, String airlineID, final Context context, final NetworkRequestCallback<List<Flight>> successCallback, final NetworkRequestCallback<String> errorCallback) {
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
                            if (successCallback != null) {
                                successCallback.execute(context, Collections.EMPTY_LIST);
                            }
                            return;
                        }
                        params.putString("page_size", Integer.toString(total));
                        new APIRequest(service, params) {
                            @Override
                            protected void successCallback(String result) {
                                if (successCallback == null) {
                                    Log.d("VOLANDO", "Requested flights with no callback, useless network request =(");
                                    return;
                                }
                                //Got all flights now, parse them
                                JsonArray data = gson.fromJson(result, JsonObject.class).getAsJsonArray("flights");
                                List<Flight> flights = new ArrayList<Flight>(total);
                                for (JsonElement flight : data) {
                                    flights.add(Flight.fromJson(flight.getAsJsonObject()));
                                }
                                successCallback.execute(context, flights);
                            }

                            @Override
                            protected void errorCallback(String result) {
                                if (errorCallback == null) {
                                    super.errorCallback(result);
                                } else {
                                    errorCallback.execute(context, result);
                                }
                            }
                        }.execute();
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(c, param);
                        }
                    }
                });
    }

    /**
     * Queries the API for flights with the specified criteria. If there's a network error, calls
     * the success callback with an empty result set.
     *
     * @param departureID     Valid origin ID (city or airport)
     * @param arrivalID       Valid destinarion ID (city or airport)
     * @param departureDate   Departure date in YYYY-MM-DD format.
     * @param airlineID       Search only flights with this airline ID (optional, specify null to ignore)
     * @param context         The context under which to run the specified callback.
     * @param successCallback The callback to run, once flights have been successfully fetched.
     *                        Callback is passed the returned flights.
     */
    public void getAllFlights(String departureID, String arrivalID, /*Date */String departureDate, String airlineID, final Context context, final NetworkRequestCallback<List<Flight>> successCallback) {
        getAllFlights(departureID, arrivalID, departureDate, airlineID, context, successCallback, new NetworkRequestCallback<String>() {
            @Override
            public void execute(Context c, String param) {
                Log.w("VOLANDO", "Error searching flights:");
                Log.w("VOLANDO", param);
                successCallback.execute(context, Collections.EMPTY_LIST);
            }
        });
    }

    /**
     * Queries the status of the flight with the specified information.
     *
     * @param airlineId       The flight's airline ID.
     * @param flightNum       The flight number.
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     * @param errorCallback   Function to execute on network error. If {@code null}, will run default
     *                        error handler.
     */
    public void getFlightStatus(String airlineId, int flightNum, final Context context, final NetworkRequestCallback<FlightStatus> successCallback, final NetworkRequestCallback<String> errorCallback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getflightstatus.name());
        params.putString("airline_id", airlineId);
        params.putString("flight_number", Integer.toString(flightNum));
        new APIRequest(Service.status, params) {
            @Override
            protected void successCallback(String result) {
                JsonObject responseJson = API.gson.fromJson(result, JsonObject.class);
                FlightStatus status = FlightStatus.fromJson(responseJson.getAsJsonObject("status"));
                if (successCallback != null) {
                    successCallback.execute(context, status);
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback == null) {
                    super.errorCallback(result);
                } else {
                    errorCallback.execute(context, result);
                }
            }
        }.execute();
    }

    /**
     * Queries the status of the flight with the specified information.
     *
     * @param airlineId The flight's airline ID.
     * @param flightNum The flight number.
     * @param context   Context under which to run the specified callback.
     * @param callback  Function to execute when network request is complete.
     */
    public void getFlightStatus(String airlineId, int flightNum, final Context context, final NetworkRequestCallback<FlightStatus> callback) {
        getFlightStatus(airlineId, flightNum, context, callback, null);
    }

    /**
     * Queries the status of the flight with the specified information.
     *
     * @see #getFlightStatus(String, int, Context, NetworkRequestCallback)
     */
    public void getFlightStatus(Flight flight, final Context context, final NetworkRequestCallback<FlightStatus> callback) {
        getFlightStatus(flight, context, callback, null);
    }

    /**
     * Queries the status of the specified flight, with an error callback.
     *
     * @see #getFlightStatus(String, int, Context, NetworkRequestCallback, NetworkRequestCallback)
     */
    public void getFlightStatus(Flight flight, final Context context, final NetworkRequestCallback<FlightStatus> successCallback, final NetworkRequestCallback<String> errorCallback) {
        getFlightStatus(flight.getAirline().getID(), flight.getNumber(), context, successCallback, errorCallback);
    }

    /**
     * Fetches all airlines provided by the API. Airlines are passed to the specified callback.
     *
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to execute when network request completes.
     * @param errorCallback   Function to execute on network error.
     */
    public void getAllAirlines(final Context context, final NetworkRequestCallback<Airline[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
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
                                if (successCallback != null) {
                                    successCallback.execute(context, airlines);
                                }
                            }

                            @Override
                            protected void errorCallback(String result) {
                                if (errorCallback != null) {
                                    errorCallback.execute(context, result);
                                } else {
                                    super.errorCallback(result);
                                }
                            }
                        }.execute();
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(c, param);
                        }
                    }
                });
    }

    /**
     * Fetches all airlines provided by the API. Airlines are passed to the specified callback.
     *
     * @param context  Context under which to run the specified callback.
     * @param callback Function to execute when network request completes.
     */
    public void getAllAirlines(final Context context, final NetworkRequestCallback<Airline[]> callback) {
        getAllAirlines(context, callback, null);
    }

    /**
     * Fetches all cities provided by the API. Cities are passed to the specified callback.
     *
     * @param context  Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     * @param errorCallback   Function to execute on network error.
     */
    public void getAllCities(final Context context, final NetworkRequestCallback<City[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        final Service service = Service.geo;
        final Bundle params = new Bundle();
        params.putString("method", Method.getcities.name());
        count(service, params, context, new NetworkRequestCallback<Integer>() {
                    @Override
                    public void execute(Context c, Integer cityCount) {
                        //Now that we have the total, fetch again with a page of size <cityCount>
                        params.putString("page_size", Integer.toString(cityCount));
                        new APIRequest(service, params) {
                            @Override
                            protected void successCallback(String result) {
                                JsonObject data = gson.fromJson(result, JsonObject.class);
                                JsonArray citiesJson = data.getAsJsonArray("cities");
                                City[] cities = new City[citiesJson.size()];
                                for (int i = 0; i < cities.length; i++) {
                                    cities[i] = City.fromJson(citiesJson.get(i).getAsJsonObject());
                                }
                                if (successCallback != null) {
                                    successCallback.execute(context, cities);
                                }
                            }

                            @Override
                            protected void errorCallback(String result) {
                                if (errorCallback != null) {
                                    errorCallback.execute(context, result);
                                } else {
                                    super.errorCallback(result);
                                }
                            }
                        }.execute();
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(c, param);
                        }
                    }
                });
    }

    /**
     * Fetches all cities provided by the API. Cities are passed to the specified callback.
     *
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     */
    public void getAllCities(final Context context, final NetworkRequestCallback<City[]> successCallback) {
        getAllCities(context, successCallback, null);
    }

    /**
     * Gets all currencies as returned by the API.
     *
     * @param context  Context under which to run the specified callback.
     * @param successCallback Function to run when the network request completes. It will be passed the
     *                 returned currencies.
     * @param errorCallback Function to run on network error.
     */
    public void getAllCurrencies(final Context context, final NetworkRequestCallback<Currency[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
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
                                if (successCallback != null) {
                                    //TODO make all success callbacks work like this (don't process data if there's no callback)
                                    JsonObject json = gson.fromJson(result, JsonObject.class);
                                    Currency[] currencies = gson.fromJson(json.get("currencies"), Currency[].class);
                                    successCallback.execute(context, currencies);
                                }
                            }

                            @Override
                            protected void errorCallback(String result) {
                                if (errorCallback != null) {
                                    errorCallback.execute(context, result);
                                } else {
                                    super.errorCallback(result);
                                }
                            }
                        }.execute();
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(context, param);
                        }
                    }
                });
    }

    /**
     * Gets all currencies as returned by the API.
     *
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to run when the network request completes. It will be passed the
     *                        returned currencies.
     */
    public void getAllCurrencies(final Context context, final NetworkRequestCallback<Currency[]> successCallback) {
        getAllCurrencies(context, successCallback, null);
    }

    /**
     * Fetches all countries provided by the API, running specified callbacks on success and error.
     * Countries are passed to the specified callback.
     *
     * @param context  Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     * @param errorCallback Function to run if there's a network error.
     */
    public void getAllCountries(final Context context, final NetworkRequestCallback<Country[]> callback, final NetworkRequestCallback<String> errorCallback) {
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
                                JsonArray countriesJson = json.getAsJsonArray("countries");
                                Country[] countries = new Country[countriesJson.size()];
                                for (int i = 0; i < countries.length; i++) {
                                    countries[i] = Country.fromJson(countriesJson.get(i).getAsJsonObject());
                                }
                                if (callback != null) {
                                    callback.execute(context, countries);
                                }
                            }

                            @Override
                            protected void errorCallback(String result) {
                                if (errorCallback != null) {
                                    errorCallback.execute(context, result);
                                } else {
                                    super.errorCallback(result);
                                }
                            }
                        }.execute();
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(context, param);
                        }
                    }
                });
    }

    /**
     * Fetches all countries provided by the API. Countries are passed to the specified callback.
     *
     * @param context  Context under which to run the specified callback.
     * @param callback Function to execute when network request is complete.
     */
    public void getAllCountries(final Context context, final NetworkRequestCallback<Country[]> callback) {
        getAllCountries(context, callback, null);
    }

    /**
     * Fetches all airports provided by the API. Airports are passed to the specified callback.
     *
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     * @param errorCallback   Function to run on network error.
     */
    public void getAllAirports(final Context context, final NetworkRequestCallback<Airport[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
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
                        JsonArray airports = json.getAsJsonArray("airports");
                        Airport[] result = new Airport[airports.size()];
                        for (int i = 0; i < result.length; i++) {
                            result[i] = Airport.fromJson(airports.get(i).getAsJsonObject());
                        }
                        if (successCallback != null) {
                            successCallback.execute(context, result);
                        }
                    }

                    @Override
                    protected void errorCallback(String result) {
                        if (errorCallback != null) {
                            errorCallback.execute(context, result);
                        } else {
                            super.errorCallback(result);
                        }
                    }
                }.execute();
            }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(context, param);
                        }
                    }
                });
    }

    /**
     * Fetches all airports provided by the API. Airports are passed to the specified callback.
     *
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     */
    public void getAllAirports(final Context context, final NetworkRequestCallback<Airport[]> successCallback) {
        getAllAirports(context, successCallback, null);
    }

    /**
     * Fetches all languages provided by the API. Languages are passed to the specified callback.
     *
     * @param context  Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     * @param errorCallback Function to run on network error.
     */
    public void getLanguages(final Context context, final NetworkRequestCallback<Language[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getlanguages.name());
        new APIRequest(Service.misc, params) {
            @Override
            protected void successCallback(String result) {
                JsonObject data = gson.fromJson(result, JsonObject.class);
                Language[] langs = gson.fromJson(data.get("languages"), Language[].class);
                if (successCallback != null) {
                    successCallback.execute(context, langs);
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
                }
            }
        }.execute();
    }

    /**
     * Fetches all languages provided by the API. Languages are passed to the specified callback.
     *
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to execute when network request is complete.
     */
    public void getLanguages(final Context context, final NetworkRequestCallback<Language[]> successCallback) {
        getLanguages(context, successCallback, null);
    }

    /**
     * Gets all airports near the specified location within the specified radius. <b>NOTE:</b> API
     * returns incomplete airports, this function maps the returned airports to the complete airport
     * objects stored in local storage.
     *
     * @param latitude
     * @param longitude
     * @param radius          In km
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to run when the network request completes. Will get passed the
     *                        returned (complete) Airport objects.
     * @param errorCallback   Function to run on network error.
     */
    public void getAirportsByLocation(double latitude, double longitude, int radius, final Context context, final NetworkRequestCallback<Airport[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getairportsbyposition.name());
        params.putString("latitude", Double.toString(latitude));
        params.putString("longitude", Double.toString(longitude));
        params.putString("radius", Integer.toString(radius));
        new APIRequest(Service.geo, params) {
            @Override
            protected void successCallback(String data) {
                if (successCallback != null) {
                    JsonObject json = gson.fromJson(data, JsonObject.class);
                    JsonArray airports = json.getAsJsonArray("airports");
                    List<Airport> result = new ArrayList<>();
                    Map<String, Airport> completeAirports = PersistentData.getContextLessInstance().getAirports();
                    if (completeAirports == null) {
                        throw new IllegalStateException("Airports not stored in local storage, can't search airports by location.");
                    }
                    for (JsonElement airport : airports) {
                        result.add(completeAirports.get(airport.getAsJsonObject().get("id").getAsString()));
                    }
                    if (successCallback != null) {
                        successCallback.execute(context, result.toArray(new Airport[0]));
                    }
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
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
     * @param radius    In km
     * @param context   Context under which to run the specified callback.
     * @param callback  Function to run when the network request completes. Will get passed the
     *                  returned (complete) Airport objects.
     */
    public void getAirportsByLocation(double latitude, double longitude, int radius, final Context context, final NetworkRequestCallback<Airport[]> callback) {
        getAirportsByLocation(latitude, longitude, radius, context, callback, null);
    }

    /**
     * Gets all airports within a {@code DEFAULT_RADIUS}-km radius.
     *
     * @see #getAirportsByLocation(double, double, int, Context, NetworkRequestCallback)
     */
    public void getAirportsByLocation(double latitude, double longitude, final Context context, final NetworkRequestCallback<Airport[]> callback) {
        getAirportsByLocation(latitude, longitude, DEFAULT_RADIUS, context, callback, null);
    }

    /**
     * Gets all airports within a {@code DEFAULT_RADIUS}-km radius with an error callback.
     *
     * @see #getAirportsByLocation(double, double, int, Context, NetworkRequestCallback, NetworkRequestCallback)
     */
    public void getAirportsByLocation(double latitude, double longitude, final Context context, final NetworkRequestCallback<Airport[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        getAirportsByLocation(latitude, longitude, DEFAULT_RADIUS, context, successCallback, errorCallback);
    }

    /**
     * Gets all cities near the specified location within the specified radius. <b>NOTE:</b> API
     * returns incomplete cities, this function maps the returned cities to the complete city
     * objects stored in local storage.
     *
     * @param latitude
     * @param longitude
     * @param radius    In km
     * @param context   Context under which to run the specified callback.
     * @param successCallback  Function to run when the network request completes. Will get passed the
     *                  returned (complete) City objects.
     * @param errorCallback Function to run on network error.
     */
    public void getCitiesByLocation(double latitude, double longitude, int radius, final Context context, final NetworkRequestCallback<City[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getcitiesbyposition.name());
        params.putString("latitude", Double.toString(latitude));
        params.putString("longitude", Double.toString(longitude));
        params.putString("radius", Integer.toString(radius));
        new APIRequest(Service.geo, params) {
            @Override
            protected void successCallback(String data) {
                if (successCallback != null) {
                    JsonObject json = gson.fromJson(data, JsonObject.class),
                            cities = json.getAsJsonObject("cities");
                    List<City> result = new ArrayList<>();
                    Map<String, City> completeCities = PersistentData.getContextLessInstance().getCities();
                    if (completeCities == null) {
                        throw new IllegalStateException("Cities not stored in local storage, can't search cities by location.");
                    }
                    for (JsonElement city : json.getAsJsonArray("cities")) {
                        result.add(completeCities.get(city.getAsJsonObject().get("id").getAsString()));
                    }
                    if (successCallback != null) {
                        successCallback.execute(context, result.toArray(new City[0]));
                    }
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
                }
            }
        }.execute();
    }

    /**
     * Gets all cities near the specified location within the specified radius. <b>NOTE:</b> API
     * returns incomplete cities, this function maps the returned cities to the complete city
     * objects stored in local storage.
     *
     * @param latitude
     * @param longitude
     * @param radius          In km
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to run when the network request completes. Will get passed the
     *                        returned (complete) City objects.
     */
    public void getCitiesByLocation(double latitude, double longitude, int radius, final Context context, final NetworkRequestCallback<City[]> successCallback) {
        getCitiesByLocation(latitude, longitude, radius, context, successCallback, null);
    }

    /**
     * Gets all cities within a {@link #DEFAULT_RADIUS}-km radius.
     *
     * @see #getCitiesByLocation(double, double, int, Context, NetworkRequestCallback, NetworkRequestCallback)
     */
    public void getCitiesByLocation(double latitude, double longitude, final Context context, final NetworkRequestCallback<City[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        getCitiesByLocation(latitude, longitude, DEFAULT_RADIUS, context, successCallback, errorCallback);
    }

    /**
     * Gets all cities within a {@link #DEFAULT_RADIUS}-km radius.
     *
     * @see #getCitiesByLocation(double, double, int, Context, NetworkRequestCallback)
     */
    public void getCitiesByLocation(double latitude, double longitude, final Context context, final NetworkRequestCallback<City[]> successCallback) {
        getCitiesByLocation(latitude, longitude, DEFAULT_RADIUS, context, successCallback, null);
    }

    /**
     * Gets last-minute flight deals from the specified origin.
     *
     * @param fromID          Valid ID of origin (city or airport)
     * @param context         The context under which to run the specified callback.
     * @param successCallback Function to run once network request is complete.
     * @param errorCallback   Function to run on network error.
     */
    public void getDeals(String fromID, final Context context, final NetworkRequestCallback<Deal[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        Bundle params = new Bundle();
        params.putString("method", Method.getlastminuteflightdeals.name());
        params.putString("from", fromID);
        new APIRequest(Service.booking, params) {
            @Override
            protected void successCallback(String result) {
                Gson g = new Gson();
                JsonObject json = g.fromJson(result, JsonObject.class);
                Deal[] deals = g.fromJson(json.get("deals"), Deal[].class);
                if (successCallback != null) {
                    successCallback.execute(context, deals);
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
                }
            }
        }.execute();
    }

    /**
     * Gets last-minute flight deals from the specified origin.
     *
     * @param fromID   Valid ID of origin (city or airport)
     * @param context  The context under which to run the specified callback.
     * @param callback Function to run once network request is complete.
     */
    public void getDeals(String fromID, final Context context, final NetworkRequestCallback<Deal[]> callback) {
        getDeals(fromID, context, callback, null);
    }

    /**
     * Gets last-minute flight deals from the specified place.
     *
     * @param from            The place from which to search for deals, airport or city.
     * @param context         The context under which to run the specified callback.
     * @param successCallback Function to run once network request is complete.
     * @param errorCallback   Function to run on network error.
     */
    public void getDeals(Place from, final Context context, final NetworkRequestCallback<Deal[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
        getDeals(from.getID(), context, successCallback, errorCallback);
    }

    /**
     * Gets last-minute flight deals from the specified place.
     *
     * @param from     The place from which to search for deals, airport or city.
     * @param context  The context under which to run the specified callback.
     * @param callback Function to run once network request is complete.
     */
    public void getDeals(Place from, final Context context, final NetworkRequestCallback<Deal[]> callback) {
        getDeals(from, context, callback, null);
    }

    /**
     * Gets a subset of all reviews for the specified flight.
     *
     * @param flight     The flight for which to search reviews.
     * @param pageNumber The page number of reviews to fetch.
     * @param pageSize   The number of reviews per page.
     * @param context    The context under which to run the specified callback.
     * @param successCallback   Function to run once the network request completes. Will get passed the found
     *                   reviews.
     * @param errorCallback Function to run on network error.
     */
    public void getPageOfReviews(Flight flight, int pageNumber, int pageSize, final Context context, final NetworkRequestCallback<Review[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
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
                if (successCallback != null) {
                    successCallback.execute(context, reviews);
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
                }
            }
        }.execute();
    }

    /**
     * Gets a subset of all reviews for the specified flight.
     *
     * @param flight          The flight for which to search reviews.
     * @param pageNumber      The page number of reviews to fetch.
     * @param pageSize        The number of reviews per page.
     * @param context         The context under which to run the specified callback.
     * @param successCallback Function to run once the network request completes. Will get passed the found
     *                        reviews.
     */
    public void getPageOfReviews(Flight flight, int pageNumber, int pageSize, final Context context, final NetworkRequestCallback<Review[]> successCallback) {
        getPageOfReviews(flight, pageNumber, pageSize, context, successCallback, null);
    }

    /**
     * Gets <b>all</b> the reviews for the specified flight.
     *
     * @param flight          The flight for which to search for reviews.
     * @param context         Context under which to run the specified callback.
     * @param successCallback Function to run once the network request completes. Will get passed the
     *                        found reviews.
     * @param errorCallback   Function to run on network error.
     */
    public void getAllReviews(Flight flight, final Context context, final NetworkRequestCallback<Review[]> successCallback, final NetworkRequestCallback<String> errorCallback) {
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
                                if (successCallback != null) {
                                    successCallback.execute(context, reviews);
                                }
                            }

                            @Override
                            protected void errorCallback(String result) {
                                if (errorCallback != null) {
                                    errorCallback.execute(context, result);
                                } else {
                                    super.errorCallback(result);
                                }
                            }
                        }.execute();
                    }
                },
                new NetworkRequestCallback<String>() {
                    @Override
                    public void execute(Context c, String param) {
                        if (errorCallback != null) {
                            errorCallback.execute(c, param);
                        }
                    }
                });
    }

    /**
     * Gets <b>all</b> the reviews for the specified flight.
     *
     * @param flight   The flight for which to search for reviews.
     * @param context  Context under which to run the specified callback.
     * @param callback Function to run once the network request completes. Will get passed the
     *                 found reviews.
     */
    public void getAllReviews(Flight flight, final Context context, final NetworkRequestCallback<Review[]> callback) {
        getAllReviews(flight, context, callback, null);
    }

    /**
     * Submits the specified review to the server.
     *
     * @param review          The review to publish.
     * @param context         Context under which to run the specified callback, if any.
     * @param successCallback Function to run once the network request completes.
     * @param errorCallback   Function to run on network error.
     */
    public void submitReview(Review review, final Context context, final NetworkRequestCallback<Void> successCallback, final NetworkRequestCallback<String> errorCallback) {
        Bundle params = new Bundle();
        params.putString("method", Method.reviewairline2.name());
        params.putString("review", review.toJson());
        new APIRequest(Service.review, params) {
            @Override
            protected void successCallback(String result) {
                if (successCallback != null) {
                    successCallback.execute(context, null);
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
                }
            }
        }.execute();
    }

    /**
     * Submits the specified review to the server.
     *
     * @param review   The review to publish.
     * @param context  Context under which to run the specified callback, if any.
     * @param callback Function to run once the network request completes.
     */
    public void submitReview(Review review, final Context context, final NetworkRequestCallback<Void> callback) {
        Bundle params = new Bundle();
        params.putString("method", Method.reviewairline2.name());
        params.putString("review", review.toJson());
        new APIRequest(Service.review, params) {
            @Override
            protected void successCallback(String result) {
                if (callback != null) {
                    callback.execute(context, null);
                }
            }
        }.execute();
    }

    /**
     * Gets the count of results returned by the specified query, or {@code null} if the query
     * doesn't specify a total, and passes it to the specified callback function.
     *
     * @param service         The service the request is for.
     * @param requestParams   Request parameters.
     * @param context         Context to run the callback with.
     * @param successCallback Callback which will receive the total, or {@code null} if not specified in
     *                        the request's response.
     * @param errorCallback   Callback to run on network error. If null, will run default error handler.
     */
    public void count(Service service, Bundle requestParams, final Context context, final NetworkRequestCallback<Integer> successCallback, final NetworkRequestCallback<String> errorCallback) {
        new APIRequest(service, requestParams) {
            @Override
            protected void successCallback(String result) {
                if (successCallback != null) {
                    JsonObject json = gson.fromJson(result, JsonObject.class);
                    JsonElement totalObj = json.get("total");
                    Integer total = null;
                    if (totalObj != null) {
                        total = totalObj.getAsInt();
                    }
                    successCallback.execute(context, total);
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback == null) {
                    super.errorCallback(result);
                } else {
                    errorCallback.execute(context, result);
                }
            }
        }.execute();
    }

    /**
     * Gets the count of results returned by the specified query, or {@code null} if the query
     * doesn't specify a total, and passes it to the specified callback function.
     *
     * @param service       The service the request is for.
     * @param requestParams Request parameters.
     * @param context       Context to run the callback with.
     * @param callback      Callback which will receive the total, or {@code null} if not specified in
     *                      the request's response.
     */
    public void count(Service service, Bundle requestParams, final Context context, final NetworkRequestCallback<Integer> callback) {
        count(service, requestParams, context, callback, null);
    }

    /**
     * Queries Flickr for landscape images matching the specified query (usually a city or airport).
     * The specified callback gets returned the URL of the first matching image, or {@code null} if
     * none were found.
     *
     * @param query           Query for images to match.
     * @param context         Context under which to run the specified callback.
     * @param successCallback Callback to run when the network request completes. Will get passed
     *                        the URL of the first matching image, or {@code null} if no images were
     *                        found.
     * @param errorCallback   Function to run on network error.
     */
    public void getFlickrImg(String query, final Context context, final NetworkRequestCallback<String> successCallback, final NetworkRequestCallback<String> errorCallback) {
        new FlickrRequest(query) {
            @Override
            protected void successCallback(String result) {
                JsonObject flickrData = gson.fromJson(result, JsonObject.class);
                JsonArray photos = flickrData.getAsJsonObject("photos").getAsJsonArray("photo");
                if (photos.size() == 0) {
                    successCallback.execute(context, null);
                } else {
                    successCallback.execute(context, getFlickImageURL(photos.get(0).getAsJsonObject()));
                }
            }

            @Override
            protected void errorCallback(String result) {
                if (errorCallback != null) {
                    errorCallback.execute(context, result);
                } else {
                    super.errorCallback(result);
                }
            }
        }.execute();
    }

    /**
     * Queries Flickr for landscape images matching the specified query (usually a city or airport).
     * The specified callback gets returned the URL of the first matching image, or {@code null} if
     * none were found.
     *
     * @param query           Query for images to match.
     * @param context         Context under which to run the specified callback.
     * @param successCallback Callback to run when the network request completes. Will get passed
     *                        the URL of the first matching image, or {@code null} if no images were
     *                        found.
     */
    public void getFlickrImg(String query, final Context context, final NetworkRequestCallback<String> successCallback) {
        getFlickrImg(query, context, successCallback, null);
    }

    private String getFlickImageURL(JsonObject imgObj) {
        return "https://farm" + Integer.toString(imgObj.get("farm").getAsInt()) + ".staticflickr.com/" + imgObj.get("server").getAsString() + "/" + imgObj.get("id").getAsString() + "_" + imgObj.get("secret").getAsString() + "_c.jpg";
    }
}
