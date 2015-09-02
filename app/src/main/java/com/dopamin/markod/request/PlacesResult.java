package com.dopamin.markod.request;

import com.dopamin.markod.objects.Market;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kadir on 02.09.2015.
 * Callback for the Result of Nearby Markets in search area
 */
public interface PlacesResult {
    void processPlaces(List<Market> nearbyMarkets);
    void processMarkers(List<MarkerOptions> markerOptions);
}
