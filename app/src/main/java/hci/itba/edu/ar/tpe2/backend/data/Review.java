package hci.itba.edu.ar.tpe2.backend.data;

import android.text.Html;

import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Review {
    private int overall, friendliness, food, punctuality, mileage_program, comfort, quality_price, flightNumber;
    private String airlineID, comment;
    private boolean isRecommended;

    private Review() {
    }

    public Review(Flight flight, int overallScore, String comment) {
        //TODO is recommended?
        if (overallScore > 5) {
            isRecommended = true;
        } else {
            isRecommended = false;
        }
        flightNumber = flight.getNumber();
        airlineID = flight.getAirline().getID();
        this.comment = comment;
        //Map all fields to overall
        friendliness = food = punctuality = mileage_program = comfort = quality_price = overall = overallScore; //TODO verify it works properly
    }

    public Review(Flight flight, int overallScore) {
        this(flight, overallScore, null);
    }

    public static Review fromJson(JsonObject json) {
        Review result = new Review();
        JsonObject numbers = json.getAsJsonObject("rating");
        result.airlineID = json.getAsJsonObject("flight").getAsJsonObject("airline").get("id").getAsString();
        result.flightNumber = json.getAsJsonObject("flight").get("number").getAsInt();
        result.overall = numbers.get("overall").getAsInt();
        result.friendliness = numbers.get("friendliness").getAsInt();
        result.food = numbers.get("food").getAsInt();
        result.punctuality = numbers.get("punctuality").getAsInt();
        result.mileage_program = numbers.get("mileage_program").getAsInt();
        result.comfort = numbers.get("comfort").getAsInt();
        result.quality_price = numbers.get("quality_price").getAsInt();
        result.isRecommended = json.get("yes_recommend").getAsBoolean();
        result.comment = Html.fromHtml(json.get("comments").getAsString()).toString();   //TODO trim?
        return result;
    }

    /**
     * @return This review's overall score, mapped to a scale of 5. Decimals are rounded down.
     */
    public int getOverall() {
        return (int) Math.ceil(overall / 2);
    }

    /**
     * @return This review's original score, as returned by the API.
     */
    public int getOverallOver10() {
        return overall;
    }

    public int getFriendliness() {
        return friendliness;
    }

    public int getFood() {
        return food;
    }

    public int getPunctuality() {
        return punctuality;
    }

    public int getMileageProgram() {
        return mileage_program;
    }

    public int getComfort() {
        return comfort;
    }

    public int getQuality_Price() {
        return quality_price;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public String getAirlineID() {
        return airlineID;
    }

    public String getComment() {
        return comment;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    public String toJson() {
        try {
            return "{\"flight\":{\"airline\":{\"id\":\"" + airlineID + "\"},\"number\":" + flightNumber + "},\"rating\":{\"friendliness\":" + friendliness + ",\"food\":" + food + ",\"punctuality\":" + punctuality + ",\"mileage_program\":" + mileage_program + ",\"comfort\":" + comfort + ",\"quality_price\":" + quality_price + "},\"yes_recommend\":" + isRecommended + ",\"comments\":\"" + URLEncoder.encode(comment, "UTF-8") + "\"}";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
