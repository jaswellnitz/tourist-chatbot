package domain;

import java.io.Serializable;

/**
 * Defines a location, specified by coordinates
 * @author Jasmin Wellnitz
 *
 */
public class Location implements Serializable {

	private static final long serialVersionUID = -7457780936211054518L;
	private final double latitude;
	private final double longitude;
	/**
	 * Defines a location that was not yet specified, e.g. when a user has not mentioned his current location to the system yet
	 */
	public static final Location UNSET = new Location(0,0);

	/**
	 * Creates a location, specified by coordinates.
	 * @param latitude
	 * @param longitude
	 */
	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Gets the latitude
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Gets the longitude
	 * @return
	 */
	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString(){
		return latitude+","+longitude;
	}
}
