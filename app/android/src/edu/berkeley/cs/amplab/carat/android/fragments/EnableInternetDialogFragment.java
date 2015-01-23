package edu.berkeley.cs.amplab.carat.android.fragments;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class EnableInternetDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_text)
               .setPositiveButton(R.string.dialog_enable, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       CaratApplication.getMainActivity().GoToWifiScreen();
                   }
               })
               .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   EnableInternetDialogFragment.this.getDialog().cancel();
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}