package com.dopamin.markod.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopamin.markod.R;
import com.dopamin.markod.objects.Market;

import java.util.List;

/**
 * Created by kadir on 29.09.2015.
 */
public class RV_MarketAdapter extends RecyclerView.Adapter<RV_MarketAdapter.MarketViewHolder> {

    private List<Market> markets;

    public RV_MarketAdapter(List<Market> markets) {
        this.markets = markets;
    }

    public void setData(List<Market> markets) {
        this.markets = markets;
    }

    @Override
    public MarketViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_market_select, viewGroup, false);
        MarketViewHolder mvh = new MarketViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(MarketViewHolder marketViewHolder, int i) {
        //marketViewHolder.placePhoto.setImageBitmap();
        marketViewHolder.placeName.setText(this.markets.get(i).getName());
        marketViewHolder.placeAddress.setText(this.markets.get(i).getVicinity());
    }

    @Override
    public int getItemCount() {
        return this.markets.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class MarketViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView placeName;
        TextView placeAddress;
        ImageView placePhoto;

        MarketViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.id_cv_market_select);
            placeName = (TextView)itemView.findViewById(R.id.id_place_name);
            placeAddress = (TextView)itemView.findViewById(R.id.id_place_address);
            placePhoto = (ImageView)itemView.findViewById(R.id.id_place_photo);
        }
    }
}
