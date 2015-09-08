package com.dopamin.markod.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Product implements Parcelable {
	
	private String name;
	private String barcode;
	private String price;
	private int pMain;
	private int pCent;

	public int getpMain() {
		return pMain;
	}

	public void setpMain(int pMain) {
		this.pMain = pMain;
	}

	public int getpCent() {
		return pCent;
	}

	public void setpCent(int pCent) {
		this.pCent = pCent;
	}

	private boolean priceParser() {
		if (this.price != null) {
			String[] pieces = this.price.split("\\.");
			if (pieces.length == 2) {
				this.pMain = Integer.parseInt(pieces[0]);
				this.pCent = Integer.parseInt(pieces[1]);
				return true;
			}
		}
		return false;
	}

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

	public boolean setPrice(String price) {
		this.price = price;
		return priceParser();
	}

	public Product(String name, String barcode, String price) {
		super();
		this.name = name;
		this.barcode = barcode;
		this.price = price;
		priceParser();
	}

	public Product() {
		super();
	}

	public Product(String name, String barcode) {
		super();
		this.name = name;
		this.barcode = barcode;
	}

	public Product(Parcel parcel) {
		this.name = parcel.readString();
		this.barcode = parcel.readString();
		this.price = parcel.readString();
		priceParser();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(name);
		parcel.writeString(barcode);
		parcel.writeString(price);
	}

	public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {

		@Override
		public Product createFromParcel(Parcel parcel) {
			return new Product(parcel);
		}

		@Override
		public Product[] newArray(int i) {
			return new Product[i];
		}
	};

	public Product createJSON_ScanProduct() {
		Product product = new Product();
		product.setBarcode(this.barcode);
		return product;
	}
}
