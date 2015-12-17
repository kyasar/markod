package com.dopamin.markod.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 05.07.2015.
 */
public class Market implements Parcelable {

    private String name;
    private String maps_id;      // MAPS provider's ID
    private String _id;     // ID provided by Our DB
    private String provider;
    private String vicinity;
    private String reference;
    private String userID;  // Needed to detect which user declare the products for this market !!
    private String totalPrice;
    private Integer pMain;
    private Integer pCent;
    private Integer missing;
    private List<Product> products;
    private MarkerOptions markerOptions;
    private String loc;

    public Market(String place_name, String maps_id, String vicinity, String reference) {
        this.name = place_name;
        this.maps_id = maps_id;
        this.vicinity = vicinity;
        this.reference = reference;
        this.provider = "GOOGLE_MAPS";
        this.missing = new Integer(0);
    }

    public Market(Parcel parcel) {
        this.name = parcel.readString();
        this.maps_id = parcel.readString();
        this.provider = parcel.readString();
        this.vicinity = parcel.readString();
        this.reference = parcel.readString();
        this.userID = parcel.readString();
        this.products = parcel.readArrayList(Product.class.getClassLoader()); // be careful, Product type is parcelable?
        this.markerOptions = parcel.readParcelable(MarkerOptions.class.getClassLoader());
        this.loc = parcel.readString();
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public int getMissing() {
        return this.missing;
    }

    public void setMissing(int missing) {
        this.missing = missing;
    }

    public void incMissing() {
        if (this.missing == null)
            this.missing = new Integer(0);
        this.missing++;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
        this.loc = markerOptions.getPosition().toString();
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

    public String getMaps_id() {
        return maps_id;
    }

    public void setMaps_id(String maps_id) {
        this.maps_id = maps_id;
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

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

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

    public void calculateProductList() {
        int main, cent;
        main = cent = 0;
        for (Product p : this.products) {
            main += p.getpMain();
            cent += p.getpCent();
        }
        main += cent / 100;
        cent %= 100;

        this.pCent = new Integer(cent);
        this.pMain = new Integer(main);
        this.totalPrice = Integer.toString(main) + "." + Integer.toString(cent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(maps_id);
        parcel.writeString(provider);
        parcel.writeString(vicinity);
        parcel.writeString(reference);
        parcel.writeString(userID);
        parcel.writeList(products);
        parcel.writeParcelable(markerOptions, i);
        parcel.writeString(loc);
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
        market.setMaps_id(this.maps_id);
        market.setName(this.name);
        market.setProvider(this.provider);
        market.setVicinity(this.vicinity);
        market.setProducts(removeNames(this.products));
        market.setUserID(this.userID);
        market.setLoc(this.loc);
        return market;
    }

    public Market createJSON_ScanMarket() {
        Market market = new Market();
        market.setMaps_id(this.maps_id);
        return market;
    }
}
