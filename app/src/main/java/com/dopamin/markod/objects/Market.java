package com.dopamin.markod.objects;

/**
 * Created by kadir on 05.07.2015.
 */
public class Market {

    private String place_name;
    private String gmap_id;
    private String vicinity;

    public Market(String place_name, String gmap_id, String vicinity) {
        this.place_name = place_name;
        this.gmap_id = gmap_id;
        this.vicinity = vicinity;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getGmap_id() {
        return gmap_id;
    }

    public void setGmap_id(String gmap_id) {
        this.gmap_id = gmap_id;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
