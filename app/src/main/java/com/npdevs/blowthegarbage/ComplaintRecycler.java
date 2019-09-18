package com.npdevs.blowthegarbage;

import com.google.android.gms.maps.model.LatLng;

public class ComplaintRecycler {
	private String complaint,longitude,latitude;

	public ComplaintRecycler(String complaint, String latitude, String longitude) {
		this.complaint = complaint;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public ComplaintRecycler() {
	}

	public String getComplaint() {
		return complaint;
	}

	public void setComplaint(String complaint) {
		this.complaint = complaint;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
}
