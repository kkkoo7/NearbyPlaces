package com.learning.kulendra.briskytask;

import android.location.Location;

public class GooglePlace {
	private String name;
	private String category;
	private String rating;
	private String open;
	private Location l;

	public GooglePlace() {
		this.name = "";
		this.rating = "";
		this.open = "";
		this.setCategory("");
		this.l= new Location("dummy");
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getRating() {
		return rating;
	}

	public void setOpenNow(String open) {
		this.open = open;
	}

	public String getOpenNow() {
		return open;
	}

	public Location getL() {
		return l;
	}
	public void setL(double lat, double lon){
		this.l.setLatitude(lat);
		this.l.setLongitude(lon);
	}
}