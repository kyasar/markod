package com.dopamin.markod.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;

/**
 * Created by kadir on 01.10.2015.
 */
public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private String [] ads;
    private int [] icons;

    LayoutInflater inflater;

    public ViewPagerAdapter(Context context, String [] ads, int [] icons) {
        this.context = context;
        this.ads = ads;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return ads.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // Declare Variables
        TextView tv_info;
        ImageView iv_icon;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container, false);

        // Locate the TextViews in viewpager_item.xml
        tv_info = (TextView) itemView.findViewById(R.id.id_viewpager_info);
        tv_info.setText(ads[position]);

        iv_icon = (ImageView) itemView.findViewById(R.id.id_viewpager_img);
        iv_icon.setImageResource(icons[position]);

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}
