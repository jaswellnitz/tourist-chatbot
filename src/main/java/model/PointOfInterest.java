package model;

public class PointOfInterest implements ProfileItem {

	private final long id;
	private final String name;
	private final String address;
	private final int distance;
	private final String openingHours;
	private final POIProfile profile;

	public PointOfInterest(long id, String name, String streetName, String houseNumber, int distance,
			String openingHours, POIProfile profile) {
		this.id = id;
		this.name = name != null? name: "";
		this.address = parseAddress(streetName, houseNumber);
		this.distance = distance;
		this.openingHours = openingHours != null? openingHours: "";
		this.profile = profile;
	}

	public final String parseAddress(String streetName, String houseNumber) {
		String s = "";
		String h = "";
		if (streetName != null && !streetName.equals("")) {
			s = streetName;
			if (houseNumber != null && !houseNumber.equals("")) {
				h = " " + houseNumber;
			}
		}
		return s + h;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public POIProfile getProfile() {
		return profile;
	}

	public int getDistance() {
		return distance;
	}

	public String getOpeningHours() {
		return openingHours;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + distance;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((openingHours == null) ? 0 : openingHours.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointOfInterest other = (PointOfInterest) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (distance != other.distance)
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		if (openingHours == null) {
			if (other.openingHours != null)
				return false;
		} else if (!openingHours.equals(other.openingHours))
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id + "(: " + name + ", " + address + ", " + distance + ", " + openingHours +")";
	}

}
