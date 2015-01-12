package com.aidangrabe.studentapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aidangrabe.studentapp.activities.FindMyPhoneActivity;
import com.aidangrabe.studentapp.activities.ToDoListActivity;
import com.aidangrabe.studentapp.activities.games.MineSweeperActivity;

public class MainWearActivity extends Activity implements WearableListView.ClickListener {

    private WearableListView mListView;
    private String[] mMenuOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);

        mMenuOptions = getMenuOptions();

        mListView = (WearableListView) findViewById(R.id.wearable_list);
        mListView.setAdapter(new Adapter(this, mMenuOptions));
        mListView.setClickListener(this);

    }

    private String[] getMenuOptions() {
        return new String[] {
                getResources().getString(R.string.menu_todo_list),
                getResources().getString(R.string.menu_timetable),
                getResources().getString(R.string.menu_games),
                getResources().getString(R.string.find_my_phone)
        };
    }



    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        Class newActivityClass = null;
        int tag = (int) viewHolder.itemView.getTag();

        // ToDoList
        if (mMenuOptions[tag].equals(getResources().getString(R.string.menu_todo_list))) {
            newActivityClass = ToDoListActivity.class;
        }
        // Timetable
        else if (mMenuOptions[tag].equals(getResources().getString(R.string.menu_timetable))) {

        }
        // Games
        else if (mMenuOptions[tag].equals(getResources().getString(R.string.menu_games))) {
            newActivityClass = MineSweeperActivity.class;
        }
        // Find My Phone
        else if (mMenuOptions[tag].equals(getResources().getString(R.string.find_my_phone))) {
            newActivityClass = FindMyPhoneActivity.class;
        }

        if (newActivityClass != null) {
            Intent intent = new Intent(this, newActivityClass);
            startActivity(intent);
        }

    }

    @Override
    public void onTopEmptyRegionClick() {
        Log.d("DEBUG", "onTopEmptyRegionClick!");
    }

    private static class Adapter extends WearableListView.Adapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private String[] mDataset;

        public Adapter(Context context, String[] dataset) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mDataset = dataset;
        }

        // Provide a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;
            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.name);
            }
        }
        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items
            return new ItemViewHolder(mInflater.inflate(R.layout.list_item_main_menu, null));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            // replace text contents
            view.setText(mDataset[position]);
            // replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

}
