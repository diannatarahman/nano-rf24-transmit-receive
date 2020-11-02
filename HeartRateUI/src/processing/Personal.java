package processing;

import java.io.Serializable;

public final class Personal implements Comparable<Personal>, Serializable {
	private static final long serialVersionUID = 2482398308744652468L;
	private final String name;
	private final String rank;
	private final String unit;
	private final String category;
	private final int age;
	
	public Personal() {
		name = "";
		rank = "";
		unit = "";
		category = "";
		age = 0;
	}

	public Personal(String name, String rank, String unit, String category, int age) {
		if (name == null)
			name = "";
		if (rank == null)
			rank = "";
		if (unit == null)
			unit = "";
		if (category == null)
			category = "";
		if (age < 0)
			age = 0;
		this.name = name;
		this.rank = rank;
		this.unit = unit;
		this.category = category;
		this.age = age;
	}
	
	public Personal(String name, String rank, String unit) {
		this(name, rank, unit, "", 0);
	}

	public String getName() {
		return name;
	}

	public String getRank() {
		return rank;
	}

	public String getUnit() {
		return unit;
	}
	
	public String getCategory() {
		return category;
	}
	
	public int getAge() {
		return age;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Personal))
			return false;
		Personal p = (Personal) o;
		return getName().equals(p.getName()) && getRank().equals(p.getRank())
				&& getUnit().equals(p.getUnit()) && getCategory().equals(p.getCategory())
				&& getAge() == p.getAge();
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31*result + getName().hashCode();
		result = 31*result + getRank().hashCode();
		result = 31*result + getUnit().hashCode();
		result = 31*result + getCategory().hashCode();
		result = 31*result + getAge();
		return result;
	}
	
	@Override
	public String toString() {
		return "{name: " + getName() + ", rank: " + getRank() + ", unit: " + getUnit() +
				", category: " + getCategory() + ", age: " + getAge() + "}";
	}

	@Override
	public int compareTo(Personal p) {
		int result = getUnit().compareTo(p.getUnit());
		if (result != 0)
			return result;
		result = getRank().compareTo(p.getRank());
		if (result != 0)
			return result;
		result = getCategory().compareTo(p.getCategory());
		if (result != 0)
			return result;
		result = getName().compareTo(p.getName());
		if (result != 0)
			return result;
		return Integer.compare(getAge(), p.getAge());
	}

}
