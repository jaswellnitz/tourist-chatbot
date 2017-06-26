package domain;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = -7457780936211054518L;
	private final double latitude;
	private final double longitude;
	public static final Location UNSET = new Location(0,0);

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString(){
		return latitude+","+longitude;
	}
}
