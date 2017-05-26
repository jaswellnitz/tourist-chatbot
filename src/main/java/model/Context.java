package model;

import java.util.Map;

public class Context {

	private final String name;
	private final Map<String, Object> parameters;
	private final int lifespan;

	public Context(String name, Map<String, Object> parameters, int lifespan) {
		this.name = name;
		this.parameters = parameters;
		this.lifespan = lifespan;
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public int getLifespan() {
		return lifespan;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lifespan;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
		Context other = (Context) obj;
		if (lifespan != other.lifespan)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return name +", " + parameters+ ", " +  lifespan;
	}

}
