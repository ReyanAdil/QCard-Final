package com.qcard.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.qcard.R;
import com.qcard.ui.home.HomeActivity;

public class AlertUtil {
    public static void alert(Activity activity, String title, String message, boolean finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (finish) {
                    activity.finish();
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                }
            }
        });
        builder.show();

    }
}
