package model;

public class PointOfInterest {

	private final long id;
	private final String name;
	private final String address;
	private final int distance;
	private final POIProfile profile;
	
	public PointOfInterest(int id, String name, String streetName, String houseNumber, int distance, POIProfile profile) {
		this.id = id;
		this.name = name;
		// Handling null values?
		String s = streetName!=null&&!streetName.equals("null")?streetName + " ":"-";
		String h = houseNumber!=null&&!houseNumber.equals("null")?houseNumber:"";
		this.address =  s + h;
		this.distance  = distance;
		this.profile = profile;
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
	
	@Override
	public String toString(){
		return id+"(: "+name+", "+address + ", " + distance +")";
	}
}
