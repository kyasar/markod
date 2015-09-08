package com.dopamin.markod.objects;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 05.07.2015.
 */
public class Market implements Parcelable {

    private String name;
    private String id;
    private String provider;
    private String vicinity;
    private String reference;
    private String userID;  // Needed to detect which user declare the products for this market !!
    private String totalPrice;
    private List<Product> products;
    private MarkerOptions markerOptions;

    public Market(String place_name, String id, String vicinity, String reference) {
        this.name = place_name;
        this.id = id;
        this.vicinity = vicinity;
        this.reference = reference;
        this.provider = "GOOGLE_MAPS";
    }

    public Market(Parcel parcel) {
        this.name = parcel.readString();
        this.id = parcel.readString();
        this.provider = parcel.readString();
        this.vicinity = parcel.readString();
        this.reference = parcel.readString();
        this.userID = parcel.readString();
        this.products = parcel.readArrayList(null); // be careful, Product type is parcelable?
        this.markerOptions = parcel.readParcelable(MarkerOptions.class.getClassLoader());
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String place_name) {
        this.name = place_name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(id);
        parcel.writeString(provider);
        parcel.writeString(vicinity);
        parcel.writeString(reference);
        parcel.writeString(userID);
        parcel.writeList(products);
        parcel.writeParcelable(markerOptions, i);
    }

    public static final Parcelable.Creator<Market> CREATOR = new Parcelable.Creator<Market>() {

        @Override
        public Market createFromParcel(Parcel parcel) {
            return new Market(parcel);
        }

        @Override
        public Market[] newArray(int i) {
            return new Market[i];
        }
    };

    private Market() {

    }

    public List<Product> removeNames(List<Product> products) {
        List<Product> namelessList = new ArrayList<Product>();
        for(Product p : products) {
            Product stripped_product = new Product();
            stripped_product.setBarcode(p.getBarcode());
            stripped_product.setPrice(p.getPrice());
            namelessList.add(stripped_product);
        }
        return namelessList;
    }

    public Market createJSON_AssocProducts() {
        Market market = new Market();
        market.setId(this.id);
        market.setName(this.name);
        market.setProvider(this.provider);
        market.setVicinity(this.vicinity);
        market.setProducts(removeNames(this.products));
        market.setUserID(this.userID);
        return market;
    }

    public Market createJSON_ScanMarket() {
        Market market = new Market();
        market.setId(this.id);
        return market;
    }
}
