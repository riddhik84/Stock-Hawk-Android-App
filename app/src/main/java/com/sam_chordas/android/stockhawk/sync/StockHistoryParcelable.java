package com.sam_chordas.android.stockhawk.sync;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by RKs on 6/27/2016.
 */
public class StockHistoryParcelable implements Parcelable{

    public String date;
    public double close;

    public final Parcelable.Creator<StockHistoryParcelable> CREATOR = new Parcelable.Creator<StockHistoryParcelable>() {
        @Override
        public StockHistoryParcelable createFromParcel(Parcel parcel) {
            return new StockHistoryParcelable(parcel);
        }

        @Override
        public StockHistoryParcelable[] newArray(int size) {
            return new StockHistoryParcelable[size];
        }
    };

    public StockHistoryParcelable(String _date, double _close) {
        date = _date;
        close = _close;
    }

    private StockHistoryParcelable(Parcel in) {
        date = in.readString();
        close = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(date);
        parcel.writeDouble(close);
    }
}
