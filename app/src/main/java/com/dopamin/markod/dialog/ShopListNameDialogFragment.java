package com.dopamin.markod.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.dopamin.markod.R;
import com.dopamin.markod.activity.MainActivity;
import com.dopamin.markod.activity.ShopListsActivity;
import com.dopamin.markod.activity.SpyMarketActivity;


public class ShopListNameDialogFragment extends DialogFragment implements TextWatcher {

	private EditText tv_ShopListName;
	private View view;

	private AlertDialog dialog;

	public ShopListNameDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static ShopListNameDialogFragment newInstance(String title) {
		ShopListNameDialogFragment frag = new ShopListNameDialogFragment();
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

		view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_shoplist_name, null);
		tv_ShopListName = (EditText) view.findViewById(R.id.id_tv_new_shoplist_name);
		tv_ShopListName.requestFocus();
		tv_ShopListName.addTextChangedListener(this);
		builder.setView(view);

		builder.setMessage(getResources().getString(R.string.str_dialog_msg_new_shoplist));

		Log.v(MainActivity.TAG, "onCreateDialog Fragment Dialog");

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
			// check valid data entered?
			if (tv_ShopListName.getText().toString().matches("")) {
				Toast.makeText(getActivity(), "No valid name entered !!", Toast.LENGTH_SHORT).show();
				ShopListsActivity activity = (ShopListsActivity) getActivity();
				activity.onUserSelectValue(ShopListsActivity.SHOPLIST_NAME_DIALOG_FRAGMENT_FAIL_CODE, null);
			} else {
				Log.v(MainActivity.TAG, "Name: " + tv_ShopListName.getText().toString() + ", which: " + which);

				ShopListsActivity activity = (ShopListsActivity) getActivity();
				activity.onUserSelectValue(ShopListsActivity.SHOPLIST_NAME_DIALOG_FRAGMENT_SUCC_CODE,
						tv_ShopListName.getText().toString());
			}

			dialog.dismiss();
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					// Do nothing
					Log.v(MainActivity.TAG, "Name not entered. which: " + which);
					ShopListsActivity activity = (ShopListsActivity) getActivity();
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

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		if (charSequence.toString().trim().length() >= 6) {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
		} else {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}
	}

	@Override
	public void afterTextChanged(Editable editable) {

	}

	@Override
	public void onStart() {
		super.onStart();
		this.dialog = (AlertDialog) getDialog();
	}
}