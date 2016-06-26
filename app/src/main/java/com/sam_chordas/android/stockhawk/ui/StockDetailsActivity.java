package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class StockDetailsActivity extends Activity {

    final static String LOG_TAG = StockDetailsActivity.class.getSimpleName();
    String symbol;

    final String[] QUOTES_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textViewQuoteName = (TextView) findViewById(R.id.quote_name);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra("symbol");
        Log.d(LOG_TAG, "rkakadia Symbol got: " + symbol);
//        Integer dbPosition = Integer.parseInt(position);
//        dbPosition = dbPosition + 1;

//        Cursor cursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, QUOTES_COLUMNS,
//                QuoteColumns._ID + "=?", new String[]{dbPosition+""}, null);

//        if (cursor != null && cursor.getCount() > 0) {
//            //cursor.moveToFirst();
//            cursor.moveToPosition(dbPosition);
//            Log.d(LOG_TAG, "rkakadia cursor count is " + cursor.getCount());
//            //symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
//            Log.d(LOG_TAG, "rkakdia Symbol clicked is " + symbol);
//            textViewQuoteName.setText(symbol);
//        } else {
//            Log.d(LOG_TAG, "rkakadia cursor count is " + cursor.getCount());
//        }

        if(symbol != null && symbol.length() > 0){
            textViewQuoteName.setText(symbol);
        }
        //cursor.close();

        //BarChart
        BarChart barChart = (BarChart) findViewById(R.id.barchart);

        BarData data = new BarData(getXAxisValues(), getDataSet());
        barChart.setData(data);
        barChart.setDescription("My Chart");
        barChart.animateXY(2000, 2000);
        barChart.invalidate();
    }

    private ArrayList<IBarDataSet> getDataSet() {
        ArrayList<IBarDataSet> dataSets = null;

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(110.000f, 0); // Jan
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry(40.000f, 1); // Feb
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry(60.000f, 2); // Mar
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry(30.000f, 3); // Apr
        valueSet1.add(v1e4);
        BarEntry v1e5 = new BarEntry(90.000f, 4); // May
        valueSet1.add(v1e5);
        BarEntry v1e6 = new BarEntry(100.000f, 5); // Jun
        valueSet1.add(v1e6);

        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        BarEntry v2e1 = new BarEntry(150.000f, 0); // Jan
        valueSet2.add(v2e1);
        BarEntry v2e2 = new BarEntry(90.000f, 1); // Feb
        valueSet2.add(v2e2);
        BarEntry v2e3 = new BarEntry(120.000f, 2); // Mar
        valueSet2.add(v2e3);
        BarEntry v2e4 = new BarEntry(60.000f, 3); // Apr
        valueSet2.add(v2e4);
        BarEntry v2e5 = new BarEntry(20.000f, 4); // May
        valueSet2.add(v2e5);
        BarEntry v2e6 = new BarEntry(80.000f, 5); // Jun
        valueSet2.add(v2e6);

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Brand 1");
        barDataSet1.setColor(Color.rgb(0, 155, 0));
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "Brand 2");
        barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("JAN");
        xAxis.add("FEB");
        xAxis.add("MAR");
        xAxis.add("APR");
        xAxis.add("MAY");
        xAxis.add("JUN");
        return xAxis;
    }
}
