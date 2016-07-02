package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by RKs on 7/1/2016.
 */
public class StockWidgetIntentService extends RemoteViewsService {

    final String LOG_TAG = StockWidgetIntentService.class.getSimpleName();

    private static final String STOCK_COLUMNS[] = new String[]{
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };

    private final int INDEX_COLUMN_SYMBOL = 1;
    private final int INDEX_COLUMN_PERCENT_CHANGE = 2;
    private final int INDEX_COLUMN_CHANGE = 3;
    private final int INDEX_COLUMN_BIDPRICE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    //Inner class
    public class StockRemoteViewsFactory implements RemoteViewsFactory {

        private final String LOG_TAG = StockRemoteViewsFactory.class.getSimpleName();
        private Context context;
        private Cursor cursor;

        public StockRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    STOCK_COLUMNS,
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDataSetChanged() {
            Log.d(LOG_TAG, "rkakadia StockRemoteViewsFactory onDataSetChanged");
            cursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    STOCK_COLUMNS,
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );

            Log.d(LOG_TAG, "rkakadia StockRemoteViewsFactory onDataSetChanged cursor count " + cursor.getCount());
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
                String symbol = cursor.getString(INDEX_COLUMN_SYMBOL);
                remoteViews.setTextViewText(R.id.stock_symbol, symbol);
                remoteViews.setTextViewText(R.id.bid_price, cursor.getString(INDEX_COLUMN_PERCENT_CHANGE));
                remoteViews.setTextViewText(R.id.change, cursor.getString(INDEX_COLUMN_CHANGE));

                if (cursor.getInt(INDEX_COLUMN_BIDPRICE) == 1) {
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
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return this.cursor.getInt(0);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}