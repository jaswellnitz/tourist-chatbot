package model;

public class User implements ProfileItem {

	private final long id;

	private final POIProfile profile;

	public User(long id, POIProfile profile) {
		this.id = id;
		this.profile = profile;
	}

	public long getId() {
		return id;
	}

	public POIProfile getProfile() {
		return profile;
	}
	
	public String toString(){
		return "User:("+id+", " + profile+")";
	}
}
