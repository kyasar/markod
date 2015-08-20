package com.dopamin.markod.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.activity.MarketSelectActivity;
import com.dopamin.markod.activity.SpyMarketActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class PointsDialogFragment extends DialogFragment {

	private TextView tv_total, tv_newMarket, tv_new_products, tv_update_products;
	private View view;

	public PointsDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static PointsDialogFragment newInstance(String title, int total_points, boolean new_market,
												   int new_products, int update_products) {
		PointsDialogFragment frag = new PointsDialogFragment();
		Bundle args = new Bundle();

		args.putString("title", title);
		args.putInt("total", total_points);
		args.putBoolean("new_market", new_market);
		args.putInt("new_products", new_products);
		args.putInt("update_products", update_products);
		Log.v(MainActivity.TAG, "Total " + total_points + " new: " + new_products + " update: " + update_products);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String title = getArguments().getString("title");
		Builder builder = new Builder(getActivity());
		builder.setTitle(title);

		view = getActivity().getLayoutInflater().inflate(R.layout.dialog_points_earned,
				null);
		tv_total = (TextView) view.findViewById(R.id.tv_id_earned_total_points);
		tv_newMarket = (TextView) view.findViewById(R.id.tv_id_new_market_points);
		tv_new_products = (TextView) view.findViewById(R.id.tv_id_new_products_points);
		tv_update_products = (TextView) view.findViewById(R.id.tv_id_update_products_points);

		Log.v(MainActivity.TAG, "Total " + getArguments().getInt("total")
				+ " new: " + getArguments().getInt("new_products")
				+ " update: " + getArguments().getInt("update_products"));

		tv_total.setText(getArguments().getInt("total") + " " + getResources().getString(R.string.total_points));
		if (getArguments().getBoolean("new_market") == true) {
			tv_newMarket.setText(R.string.new_market_points);
		} else {
			view.findViewById(R.id.tableRow2).setVisibility(View.GONE);
		}
		tv_new_products.setText(getArguments().getInt("new_products") + " "
				+ getResources().getString(R.string.new_products_points));

		tv_update_products.setText(getArguments().getInt("update_products") + " "
				+ getResources().getString(R.string.update_product_points));

		builder.setView(view);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SpyMarketActivity activity = (SpyMarketActivity) getActivity();
				activity.onUserSelectValue(SpyMarketActivity.POINTS_DIALOG_FRAGMENT_SUCC_CODE, null);
				dialog.dismiss();
			}
		});

		return builder.create(); // returns an AlertDialog from a Builder.
	}
}