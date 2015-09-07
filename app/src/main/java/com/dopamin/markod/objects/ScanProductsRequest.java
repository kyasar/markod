package com.dopamin.markod.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 07.09.2015.
 */
public class ScanProductsRequest {
    private List<Market> markets;
    private List<Product> products;

    public List<Market> getMarkets() {
        return markets;
    }

    public void setMarkets(List<Market> markets) {
        this.markets = new ArrayList<Market>();
        for (Market m : markets) {
            Market scan = m.createJSON_ScanMarket();
            this.markets.add(scan);
        }
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = new ArrayList<Product>();
        for (Product p : products) {
            Product scan = p.createJSON_ScanProduct();
            this.products.add(scan);
        }
    }
}
