package com.example.android.harjoitukset9ja10.data;

import android.provider.BaseColumns;

public class DatabaseContract {

    public static final class DatabaseEntry implements BaseColumns {

        public static final String TABLE_LOCATIONS = "lokaatiot";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ACCURACY = "accuracy";
        public static final String COLUMN_PROVIDER = "provider";
        public static final String COLUMN_TIME = "time";

    }
}
