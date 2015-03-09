package org.avr;

public class Address {
	
	private String addr1;
	private String addr2;
	private String city;
	private String state;
	private ZipCode zipCode;
	public String getAddr1() {
		return addr1;
	}
	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}
	public String getAddr2() {
		return addr2;
	}
	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public ZipCode getZipCode() {
		return zipCode;
	}
	public void setZipCode(ZipCode zipCode) {
		this.zipCode = zipCode;
	}
	
}
