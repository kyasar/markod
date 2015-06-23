package com.dopamin.markod.objects;

public class Product {
	
	private String name;
	private String barcode;
	private String price;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public Product(String name, String barcode, String price) {
		super();
		this.name = name;
		this.barcode = barcode;
		this.price = price;
	}
	public Product() {
		super();
	}
	public Product(String name, String barcode) {
		super();
		this.name = name;
		this.barcode = barcode;
	}
}
