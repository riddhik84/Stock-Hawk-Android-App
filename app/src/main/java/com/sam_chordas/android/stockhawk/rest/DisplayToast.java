package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by RKs on 6/25/2016.
 */
public class DisplayToast implements Runnable {
    private final Context mContext;
    String toastText;

    public DisplayToast(Context context, String text){
        mContext = context;
        toastText = text;
    }

    @Override
    public void run() {
        Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
    }
}
