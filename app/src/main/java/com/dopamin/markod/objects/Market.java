package com.dopamin.markod.objects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kadir on 05.07.2015.
 */
public class Market implements Serializable {

    private String name;
    private String id;
    private String provider;
    private String vicinity;
    private List products;
    private String userID;  // Needed to detect which user declare the products for this market !!

    public Market(String place_name, String id, String vicinity) {
        this.name = place_name;
        this.id = id;
        this.vicinity = vicinity;
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

    public List getProducts() {
        return products;
    }

    public void setProducts(List products) {
        this.products = products;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
