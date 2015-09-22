package com.dopamin.markod.adapter;

/**
 * Created by kadir on 21.09.2015.
 */

public class NavItem {
    private String title;
    private String subtitle;
    int mIcon;

    public NavItem(String title, String subtitle, int icon) {
        this.title = title;
        this.subtitle = subtitle;
        mIcon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getmIcon() {
        return mIcon;
    }

    public void setmIcon(int mIcon) {
        this.mIcon = mIcon;
    }
}
