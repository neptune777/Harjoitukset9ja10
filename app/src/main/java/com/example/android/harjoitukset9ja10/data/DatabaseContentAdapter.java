/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.harjoitukset9ja10.data;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.harjoitukset9ja10.R;
//import com.example.android.harjoitus.R;

/**
 * We couldn't come up with a good name for this class. Then, we realized
 * that this lesson is about RecyclerView.
 * Avoid unnecessary garbage collection by using RecyclerView and ViewHolders.
 *
 */
public class DatabaseContentAdapter extends RecyclerView.Adapter<DatabaseContentAdapter.CalculationViewHolder> {

    private static final String TAG = DatabaseContentAdapter.class.getSimpleName();


    private Cursor mCursor;
    private Context mContext;


    public DatabaseContentAdapter(Context context, Cursor cursor, Location location) {

        mCursor = cursor;
        this.mContext = context;
    }
    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */
    @Override
    public CalculationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.database_content_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        CalculationViewHolder viewHolder = new CalculationViewHolder(view);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(CalculationViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(DatabaseContract.DatabaseEntry._ID);

        if (!mCursor.moveToPosition(position))
            return; // bail if returned null

        // Determine the value of the wanted data
        final int id = mCursor.getInt(idIndex);
        //Set value
        holder.itemView.setTag(id);



            String longi = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_LONGITUDE));
            String lati = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_LATITUDE));
            String accuracy = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_ACCURACY));
            String provider = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_PROVIDER));
            String time = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.DatabaseEntry.COLUMN_TIME));

            Location location = new Location("" + provider);
            location.setLatitude(Double.parseDouble(lati));
            location.setLongitude(Double.parseDouble(longi));
            location.setAccuracy(Float.parseFloat(accuracy));
            location.setTime(Long.parseLong(time));

           // mLocation = location;
           // locationTextView.setText("AccuracyFromDB " +mLocation.getAccuracy());
            Log.d("onLoadFinished1 ","");


        String content = "Tarkkuus "+accuracy ;


        holder.listItemNumberView.setText(content);



    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {

        int returnable;

        try {
            returnable = mCursor.getCount();

            if (returnable == 0) {
                return 0;
            } else if (returnable > 0) {
                return returnable;
            }
        }catch(NullPointerException e) {
            e.printStackTrace();
            return 0;
        }

        return 0;
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    /**
     * Cache of the children views for a list item.
     */
    class CalculationViewHolder extends RecyclerView.ViewHolder {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listItemNumberView;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link DatabaseContentAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public CalculationViewHolder(View itemView) {
            super(itemView);

            listItemNumberView = (TextView) itemView.findViewById(R.id.tv_item_number);
        }

    }
}
