package com.drassapps.ourwall;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class Common {

    // Show snackbar to open configuration
    public void showConfiguration(View mLayout, final Activity act) {
        Snackbar.make(mLayout, R.string.locationPermission,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.configurar, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openConfiguration(act);
                    }
                })
                .show();
    }

    // Open device settings to granted permissions
    private void openConfiguration(Activity act) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + act.getPackageName()));
        act.startActivity(intent);
    }

    // Fuerza la creación de un SnackBar, personificado
    public void setSnackBar(View coordinatorLayout, String snackTitle) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, snackTitle, Snackbar.LENGTH_SHORT);
        snackbar.show();
        View view = snackbar.getView();
        TextView txtv = view.findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}
