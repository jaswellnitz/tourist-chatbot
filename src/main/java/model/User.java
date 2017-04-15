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
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
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
		User other = (User) obj;
		if (id != other.id)
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		return true;
	}
}
