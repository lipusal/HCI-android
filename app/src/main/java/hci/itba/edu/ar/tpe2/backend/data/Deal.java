package hci.itba.edu.ar.tpe2.backend.data;

/**
 * POJO for storing deals. <b>NOTE:</b> The inner City object is incomplete, only has:
 * <ul>
 *     <li>ID</li>
 *     <li>Name</li>
 *     <li>latitude and longitude</li>
 *     <li>
 *         Country
 *          <ul>
 *              <li>ID</li>
 *              <li>Name</li>
 *          </ul>
 *     </li>
 * </ul>
 */
public class Deal {
    private City city;
    private double price;

    public Deal(City city, double price) {
        this.city = city;
        this.price = price;
    }

    public City getCity() {
        return city;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Deal to " + city.getName() + " for $" + price;
    }
}
