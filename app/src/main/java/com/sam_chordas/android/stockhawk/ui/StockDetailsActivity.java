package com.sam_chordas.android.stockhawk.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.sync.HttpRequestResponse;
import com.sam_chordas.android.stockhawk.sync.StockHistoryParcelable;
import com.squareup.okhttp.internal.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class StockDetailsActivity extends AppCompatActivity {

    final static String LOG_TAG = StockDetailsActivity.class.getSimpleName();
    LineChart lineChart;
    TextView quoteNameTV;
    TextView companyNameTV;
    TextView prevClosePriceTV;
    TextView high52wkTV;
    TextView low52wkTV;

    String symbol = "";
    String companyName = "";
    String prevClosePrice = "";
    String currency = "";
    String high_52wk = "";
    String low_52wk = "";
//    String open = "";
//    String high = "";
//    String low = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        quoteNameTV = (TextView) findViewById(R.id.quote_name);
        companyNameTV = (TextView) findViewById(R.id.company_name);
        prevClosePriceTV = (TextView) findViewById(R.id.prev_close_price);
        high52wkTV = (TextView) findViewById(R.id.high_52_wk);
        low52wkTV = (TextView) findViewById(R.id.low_52_wk);

        Intent intent = getIntent();
        symbol = intent.getStringExtra("symbol");
        Log.d(LOG_TAG, "rkakadia position received in stockdetails : " + symbol);

        if (symbol != null) {
            quoteNameTV.setText(symbol);
        }

        if (Utils.isNetworkConnected(this)) {
            StockAsyncTask s = new StockAsyncTask(this);
            s.execute(symbol);
        } else {
            companyNameTV.setVisibility(View.INVISIBLE);
            prevClosePriceTV.setVisibility(View.INVISIBLE);
            high52wkTV.setVisibility(View.INVISIBLE);
            low52wkTV.setVisibility(View.INVISIBLE);
            //lineChart.setVisibility(View.INVISIBLE);

            Toast.makeText(this, getString(R.string.no_network_info), Toast.LENGTH_LONG).show();
        }

    }

    //Format date
    public String formatDate(String dateData) {
        //Sample date: 20160428
        //Convert to: 2016-04-28
        StringBuilder sb = new StringBuilder(dateData);
        sb.insert(4, '-');
        sb.insert(7, '-');
        return sb.toString();
    }

    //Fetch stock data AsyncTask
    class StockAsyncTask extends AsyncTask<String, Void, ArrayList<StockHistoryParcelable>> {

        private final String LOG_TAG = StockAsyncTask.class.getSimpleName();

        final Context mContext;

        public StockAsyncTask(Context context) {
            mContext = context;
        }

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Loading chart...");
            pDialog.show();
        }

        @Override
        protected ArrayList<StockHistoryParcelable> doInBackground(String... params) {
            Log.d(LOG_TAG, "rkakadia inside doInBackground() method");
            ArrayList<StockHistoryParcelable> ArrayList_SHP = new ArrayList<>();

            if (params.length == 0) {
                return null;
            }

            final String YAHOO_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
            final String CHART_DATA_URL = "/chartdata;type=quote;range=1y/json";

            String stock_quote = params[0];

            String jsonResponse = "";

            try {

                String query = YAHOO_URL + stock_quote + CHART_DATA_URL;
                Log.d(LOG_TAG, "rkakadia Stock query = " + query);

                URL stockUrl = new URL(query);
                Log.d(LOG_TAG, "rkakadia Stock URL = " + stockUrl.toString());

                //Request/Response
                HttpRequestResponse hrr = new HttpRequestResponse();
                jsonResponse = hrr.doGetRequest(stockUrl.toString());
                Log.d(LOG_TAG, "rkakadia json response: " + jsonResponse);

                //Get data from Json and insert it in table
                ArrayList_SHP = getStockdataFromJson(jsonResponse);

            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException ", e);
                return null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Exception", e);
                return null;
            } finally {

            }
            return ArrayList_SHP;
        }

        public ArrayList<StockHistoryParcelable> getStockdataFromJson(String jsonResponseString) throws JSONException {
            Log.d(LOG_TAG, "rkakadia inside getStockdataFromJson() method");
            ArrayList<StockHistoryParcelable> ArrayList_SHB = new ArrayList<>();

            if (jsonResponseString != null && jsonResponseString.length() > 1) {
                Log.d(LOG_TAG, "rkakadia jsonResponseString len " + jsonResponseString.length());

                final String MAIN_TAG = "finance_charts_json_callback";
                final String SERIES = "series";
                final String DATE = "Date";
                final String CLOSE = "close";

                final String META = "meta";
                final String COMPANY_NAME = "Company-Name";
                final String PREV_CLOSE_PRICE = "previous_close_price";
                final String CURRENCY = "currency";
                final String RANGES = "ranges";
                final String RANGES_HIGH = "high";
                final String RANGES_HIGH_MAX = "max";
                final String RANGES_LOW = "low";
                final String RANGES_LOW_MIN = "min";

                String json_string = jsonResponseString.substring(jsonResponseString.indexOf("(") + 1, jsonResponseString.lastIndexOf(")"));
                JSONObject in = new JSONObject(json_string);

                JSONObject meta = in.getJSONObject(META);
                companyName = meta.getString(COMPANY_NAME);
                Log.d(LOG_TAG, "rkakadia company name: " + companyName);

                prevClosePrice = meta.getString(PREV_CLOSE_PRICE);
                Log.d(LOG_TAG, "rkakadia previous close price: " + prevClosePrice);

                currency = meta.getString(CURRENCY);
                Log.d(LOG_TAG, "rkakadia currency: " + currency);

                JSONObject ranges = in.getJSONObject(RANGES);
                JSONObject ranges_high = ranges.getJSONObject(RANGES_HIGH);
                high_52wk = ranges_high.getString(RANGES_HIGH_MAX);
                Log.d(LOG_TAG, "rkakadia high_52wk: " + high_52wk);

                JSONObject ranges_low = ranges.getJSONObject(RANGES_LOW);
                low_52wk = ranges_low.getString(RANGES_LOW_MIN);
                Log.d(LOG_TAG, "rkakadia low_52wk: " + low_52wk);

                JSONArray stockArray = in.getJSONArray(SERIES);
                Log.d(LOG_TAG, "rkakadia JSON array length " + stockArray.length());

                for (int i = 0; i < stockArray.length(); i += 20) {
                    JSONObject stockEntry = stockArray.getJSONObject(i);
                    String date = stockEntry.getString(DATE);
                    double close = stockEntry.getDouble(CLOSE);
                    ArrayList_SHB.add(new StockHistoryParcelable(date, close));
                }
            }
            return ArrayList_SHB;
        }

//        protected void onProgressUpdate(Integer... progress) {
//            pDialog.show();
//        }

        protected void onPostExecute(ArrayList<StockHistoryParcelable> SHP) {
            Log.d(LOG_TAG, "Inside  onPostExecute ");
            super.onPostExecute(SHP);

            companyNameTV.setText(companyNameTV.getText() + " " + companyName);
            getSupportActionBar().setTitle(companyName);
            prevClosePriceTV.setText(prevClosePriceTV.getText() + " " + prevClosePrice + " " + currency);
            high52wkTV.setText(high52wkTV.getText() + " " + high_52wk + " " + currency);
            low52wkTV.setText(low52wkTV.getText() + " " + low_52wk + " " + currency);

            ArrayList<StockHistoryParcelable> ArrayList_SHB = new ArrayList<>();
            ArrayList_SHB = SHP;

            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            for (int i = 0; i < ArrayList_SHB.size(); i++) {

                StockHistoryParcelable stockParcel = ArrayList_SHB.get(i);
                String dateValue = formatDate(stockParcel.date);
                //String dateValue = stockParcel.date;
                Log.d(LOG_TAG, "rkakadia dateValue " + dateValue);
                double closeValue = stockParcel.close;
                Log.d(LOG_TAG, "rkakadia closeValue " + closeValue);

                entries.add(new Entry((float) closeValue, i));
                labels.add(dateValue);
            }

            LineDataSet dataset = new LineDataSet(entries, "Close Values");
            dataset.setDrawCubic(true);
            dataset.setDrawFilled(true);
            dataset.setColors(ColorTemplate.COLORFUL_COLORS);
            LineData data = new LineData(labels, dataset);

            pDialog.dismiss();
            lineChart = (LineChart) findViewById(R.id.linechart);
            lineChart.setDescription("LineChart for " + companyName);
            lineChart.setData(data);
            lineChart.animate();
            lineChart.invalidate();

            Log.d(LOG_TAG, "rkakadia End of execution...");
        }
    }

    //RTL language support
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        Log.d(LOG_TAG, "rkakadia forceRTLIfSupported()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }
}
