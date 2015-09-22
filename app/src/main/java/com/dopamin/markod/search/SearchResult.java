package com.dopamin.markod.search;

import android.graphics.drawable.Drawable;

import com.dopamin.markod.objects.Product;

public class SearchResult {
    public Product product;
    public Drawable icon;

    /**
     * Create a search result with text and an icon
     * @param title
     * @param icon
     */
    public SearchResult(Product product, Drawable icon) {
       this.product = product;
       this.icon = icon;
    }
    
    /**
     * Return the title of the result
     */
    public Product getProduct() {
        return product;
    }
    
}