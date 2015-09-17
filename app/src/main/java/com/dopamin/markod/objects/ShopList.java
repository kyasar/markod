package com.dopamin.markod.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by kadir on 17.09.2015.
 */
public class ShopList implements Parcelable {
    private List<Product> products;
    private String name;

    public ShopList(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
