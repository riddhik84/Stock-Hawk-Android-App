package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.DisplayToast;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
    Handler mHandler;

    final String LOG_TAG = StockIntentService.class.getSimpleName();

    public StockIntentService() {
        super(StockIntentService.class.getName());
        mHandler = new Handler();
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")) {
            Log.d(LOG_TAG, "rkakadia tag is add");
            args.putString("symbol", intent.getStringExtra("symbol"));
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
        Log.d(LOG_TAG, "rkakadia result " +result);

        if(result == 2){
            mHandler.post(new DisplayToast(this, getString(R.string.incorrect_stock_toast)));
        }
    }
}
