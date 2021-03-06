package com.dopamin.markod.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.dopamin.markod.objects.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 17.09.2015.
 */
public class ShopList implements Parcelable {
    private List<Product> products;
    private String name;

    public ShopList(String name) {
        this.name = name;
        this.products = new ArrayList<Product>();
    }

    public ShopList(Parcel parcel) {
        this.name = parcel.readString();
        this.products = parcel.readArrayList(Product.class.getClassLoader()); // be careful, Product type is parcelable?
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
        parcel.writeString(name);
        parcel.writeList(products);
    }

    public static final Parcelable.Creator<ShopList> CREATOR = new Parcelable.Creator<ShopList>() {

        @Override
        public ShopList createFromParcel(Parcel parcel) {
            return new ShopList(parcel);
        }

        @Override
        public ShopList[] newArray(int i) {
            return new ShopList[i];
        }
    };
}
