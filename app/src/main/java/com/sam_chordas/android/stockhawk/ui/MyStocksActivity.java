package com.sam_chordas.android.stockhawk.ui;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.DividerItemDecoration;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import org.w3c.dom.Text;

import java.util.Locale;

import butterknife.BindString;
import butterknife.ButterKnife;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final static String LOG_TAG = MyStocksActivity.class.getSimpleName();

    public static final String ACTION_DATA_UPDATE = "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATE";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    TextView emptyView;

//    @BindString(R.string.symbol_search)
//    String symbolsearch;
//    @BindString(R.string.content_test)
//    String content_test;
//    @BindString(R.string.input_hint)
//    String input_hint;
//    @BindString(R.string.input_prefill)
//    String input_prefill;
//    @BindString(R.string.stock_already_saved_toast)
//    String stock_already_saved_toast;
//    @BindString(R.string.network_toast)
//    String network_toast;
//    @BindString(R.string.network_info_dialog_title)
//    String network_info_dialog_title;
//    @BindString(R.string.no_network_info)
//    String no_network_info;
//    @BindString(R.string.no_quotes_info)
//    String no_quotes_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_my_stocks);
//        ButterKnife.bind(this);

//        support RTL
//        String locale = Locale.getDefault().getDisplayLanguage();
//        Log.d(LOG_TAG, "rkakadia locale: " + locale);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (Utils.isNetworkConnected(this)) {
//                Log.d(LOG_TAG, "rkakadia  network is connected");
                startService(mServiceIntent);
            } else {
//                Log.d(LOG_TAG, "rkakadia  network is not connected");
                networkToast();
                //updateEmptyView();
            }
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        // do something on item click
                        //Toast.makeText(mContext, "This is at position " + position, Toast.LENGTH_SHORT).show();
                        if (mCursor.moveToPosition(position)) {
                            Intent stockDetailScreen = new Intent(mContext, StockDetailsActivity.class);
                            stockDetailScreen.putExtra(getString(R.string.symbol), mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
                            startActivity(stockDetailScreen);
                        }
                    }
                }));

        mCursorAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateEmptyView();
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setContentDescription(getString(R.string.fab_desc));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkConnected(getApplicationContext())) {
                    new MaterialDialog.Builder(mContext).title(getString(R.string.symbol_search))
                            .content(getString(R.string.content_test))
                            .itemsGravity(GravityEnum.START)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(getString(R.string.input_hint), getString(R.string.input_prefill), new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    //Make the input string uppercase to check if the quote already exists
                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{input.toString().toUpperCase()}, null);
                                    if (c.getCount() != 0) {
//                                        Log.d(LOG_TAG, "rkakadia stock quote exists? " + c.getCount());
                                        Toast toast =
                                                Toast.makeText(MyStocksActivity.this, getString(R.string.stock_already_saved_toast),
                                                        Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra(getString(R.string.symbol), input.toString().toUpperCase());
                                        startService(mServiceIntent);
                                    }
                                }
                            })
                            .show();
                } else {
                    //networkToast();
                    updateEmptyView();
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (Utils.isNetworkConnected(this)) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public void updateEmptyView() {
        emptyView = (TextView) findViewById(R.id.empty_view);
        MaterialDialog.Builder materialDialog = new MaterialDialog.Builder(mContext);
        materialDialog.title(getString(R.string.network_info_dialog_title))
                .content(getString(R.string.no_network_info))
                .cancelable(true);

        if (Utils.isNetworkConnected(this) == false) {
            if (mCursorAdapter.getItemCount() == 0) {
                //emptyView.setText("No network available");
                //emptyView.setVisibility(View.VISIBLE);
                materialDialog.show();
            } else {
                //emptyView.setText("No Data available");
                emptyView.setVisibility(View.GONE);
                materialDialog.show();
            }
        } else {
            if (mCursorAdapter.getItemCount() == 0) {
                emptyView.setText(getString(R.string.no_quotes_info));
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    //RTL language support
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
//        Log.d(LOG_TAG, "rkakadia forceRTLIfSupported()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }
}
