package com.dopamin.markod.request;

import com.dopamin.markod.objects.Market;

import java.util.List;

/**
 * Created by kadir on 02.09.2015.
 */
public interface PlacesResult {
    void processPlaces(List<Market> nearbyMarkets);
}
