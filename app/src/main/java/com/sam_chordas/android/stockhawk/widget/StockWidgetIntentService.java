package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import java.util.concurrent.ExecutionException;

/**
 * Created by RKs on 7/1/2016.
 */
public class StockWidgetIntentService extends RemoteViewsService {

    final String LOG_TAG = StockWidgetIntentService.class.getSimpleName();

//    private static final String STOCK_COLUMNS[] = new String[]{
//            QuoteColumns._ID,
//            QuoteColumns.SYMBOL,
//            QuoteColumns.CHANGE,
//            QuoteColumns.BIDPRICE
//    };
//
//    private static final int INDEX_ID = 0;
//    private static final int INDEX_SYMBOL = 1;
//    private static final int INDEX_CHANGE = 3;
//    private static final int INDEX_BIDPRICE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    //Inner class
    public class StockRemoteViewsFactory implements RemoteViewsFactory {

        private final String TAG = StockRemoteViewsFactory.class.getSimpleName();
        private Context context;
        private Cursor cursor;
        private int appWidgetID;

        public StockRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {

            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, //0
                            QuoteColumns.SYMBOL, //1
                            QuoteColumns.BIDPRICE, //2
                            QuoteColumns.CHANGE, //3
                            QuoteColumns.ISUP}, //4
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDataSetChanged() {
            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, //0
                            QuoteColumns.SYMBOL, //1
                            QuoteColumns.BIDPRICE, //2
                            QuoteColumns.CHANGE, //3
                            QuoteColumns.ISUP}, //4
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDestroy() {
            if (this.cursor != null)
                this.cursor.close();
        }

        @Override
        public int getCount() {
            return (this.cursor != null) ? this.cursor.getCount() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(), R.layout.list_item_quote);

            if (this.cursor.moveToPosition(position)) {
                String symbol = cursor.getString(1);
                remoteViews.setTextViewText(R.id.stock_symbol, symbol);
                remoteViews.setTextViewText(R.id.bid_price, cursor.getString(2));
                remoteViews.setTextViewText(R.id.change, cursor.getString(3));

                if (cursor.getInt(4) == 1) {
                    remoteViews.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    remoteViews.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }

                Bundle extras = new Bundle();
                extras.putString(StockWidgetProvider.EXTRA_SYMBOL, symbol);

                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                remoteViews.setOnClickFillInIntent(R.id.widget_stock_symbol, fillInIntent);

            }


            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            //we have only one type of view to display so returning 1.
            return 1;
        }

        @Override
        public long getItemId(int position) {
            //Return the data from the ID column of the table.
            return this.cursor.getInt(0);
        }

        @Override
        public boolean hasStableIds() {
            /**
             * As the table contains a column called ID,
             * whose value we are returning at getItemId(),
             * and also is a primary column,
             * every Id is unique and hence stable.
             */
            return true;
        }
    }

}