package com.npdevs.blowthegarbage;

public class Users {
	private String name,mobNumber,password,address;

	public Users() {
	}

	public Users(String name, String mobNumber, String password, String address) {
		this.name = name;
		this.mobNumber = mobNumber;
		this.password = password;
		this.address = address;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMobNumber(String mobNumber) {
		this.mobNumber = mobNumber;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public String getMobNumber() {
		return mobNumber;
	}

	public String getPassword() {
		return password;
	}

	public String getAddress() {
		return address;
	}
}