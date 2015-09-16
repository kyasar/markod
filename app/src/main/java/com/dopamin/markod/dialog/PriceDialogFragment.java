package com.dopamin.markod.dialog;

import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.activity.MarketSelectActivity;
import com.dopamin.markod.activity.SpyMarketActivity;


public class PriceDialogFragment extends DialogFragment {

	private EditText npMain, npCent;
	private View view;

	public PriceDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static PriceDialogFragment newInstance(String title) {
		PriceDialogFragment frag = new PriceDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String title = getArguments().getString("title");
		Builder builder = new Builder(getActivity());
		builder.setTitle(title);

		view = getActivity().getLayoutInflater().inflate(R.layout.price_piker,
				null);
		npMain = (EditText) view.findViewById(R.id.numberMain);
		npMain.requestFocus();
		npCent = (EditText) view.findViewById(R.id.numberCent);

		builder.setView(view);
		builder.setMessage(R.string.setProductPrice);

		Log.v(MainActivity.TAG, "onCreateDialog Fragment Dialog");

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				// check valid data entered?
				if (npMain.getText().toString().matches("") || npCent.getText().toString().matches("")) {
					Toast.makeText(getActivity(), "No valid price entered !!", Toast.LENGTH_SHORT).show();
					SpyMarketActivity activity = (SpyMarketActivity) getActivity();
					activity.onUserSelectValue(SpyMarketActivity.PRICE_DIALOG_FRAGMENT_FAIL_CODE, null);
				} else {
					Log.v(MainActivity.TAG, "Price: "
							+ "." + npMain.getText().toString() + "."
							+ "." + npCent.getText().toString() + ".  which: " + which);

					SpyMarketActivity activity = (SpyMarketActivity) getActivity();
					activity.onUserSelectValue(SpyMarketActivity.PRICE_DIALOG_FRAGMENT_SUCC_CODE, npMain.getText().toString() + "."
							+ npCent.getText().toString());
				}

				dialog.dismiss();
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing
						Log.v(MainActivity.TAG,
								"Price NOT Selected. which: " + which);
						SpyMarketActivity activity = (SpyMarketActivity) getActivity();
						activity.onUserSelectValue(0, null);    // product discarded
						dialog.dismiss();
					}
				});

		return builder.create(); // returns an AlertDialog from a Builder.
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
}