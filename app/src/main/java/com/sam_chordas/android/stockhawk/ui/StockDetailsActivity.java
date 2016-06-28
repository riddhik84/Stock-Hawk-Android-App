package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

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
import com.sam_chordas.android.stockhawk.sync.HttpRequestResponse;
import com.sam_chordas.android.stockhawk.sync.StockHistoryParcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class StockDetailsActivity extends Activity {

    final static String LOG_TAG = StockDetailsActivity.class.getSimpleName();
    LineChart lineChart;
    TextView quoteNameTV;
    TextView companyNameTV;
    TextView prevClosePriceTV;

    String symbol = "";
    String companyName = "";
    String prevClosePrice = "";
    String currency = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        quoteNameTV = (TextView) findViewById(R.id.quote_name);
        companyNameTV = (TextView) findViewById(R.id.company_name);
        prevClosePriceTV = (TextView) findViewById(R.id.prev_close_price);

        Intent intent = getIntent();
        //int position = intent.getIntExtra("position", 0);
        symbol = intent.getStringExtra("symbol");
        Log.d(LOG_TAG, "rkakadia position received in stockdetails : " + symbol);

        if (symbol != null) {
            quoteNameTV.setText(symbol);
        }

        StockAsyncTask s = new StockAsyncTask(this);
        s.execute(symbol);
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

                String json_string = jsonResponseString.substring(jsonResponseString.indexOf("(") + 1, jsonResponseString.lastIndexOf(")"));
                JSONObject in = new JSONObject(json_string);

                JSONObject meta = in.getJSONObject(META);
                companyName = meta.getString(COMPANY_NAME);
                Log.d(LOG_TAG, "rkakadia company name: " + companyName);

                prevClosePrice = meta.getString(PREV_CLOSE_PRICE);
                Log.d(LOG_TAG, "rkakadia previous close price: " + prevClosePrice);

                currency = meta.getString(CURRENCY);
                Log.d(LOG_TAG, "rkakadia currency: " + currency);

                JSONArray stockArray = in.getJSONArray(SERIES);
                Log.d(LOG_TAG, "rkakadia JSON array length " + stockArray.length());

                for (int i = 0; i < stockArray.length(); i += 30) {
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
            prevClosePriceTV.setText(prevClosePriceTV.getText() + " " + prevClosePrice + " " + currency);

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

}
