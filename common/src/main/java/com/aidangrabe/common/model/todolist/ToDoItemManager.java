package com.aidangrabe.common.model.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.util.Log;

import com.aidangrabe.common.SharedConstants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aidan on 09/01/15.
 * Class to access SQLite Database for storing/retrieving ToDoItems
 */
public class ToDoItemManager extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ToDoList.db";
    public static final int DATABASE_VERSION = 1;

    /**
     * Class containing the column and table names
     */
    public static abstract class ToDoEntry {
        public static final String TABLE_NAME = "todolist";
        public static final String COL_ID = "id";
        public static final String COL_TITLE = "title";
        public static final String COL_CREATE_DATE = "created";
        public static final String COL_COMPLETE_DATE = "completed";
    }

    /**
     * Class containing relevant SQL queries for the Database
     */
    public static abstract class SqlStatement {
        public static final String CREATE_TABLE = String.format("CREATE TABLE %s (%s)", ToDoEntry.TABLE_NAME,
                        ToDoEntry.COL_ID + " INTEGER PRIMARY KEY," +
                        ToDoEntry.COL_TITLE + " VARCHAR (255)," +
                        ToDoEntry.COL_CREATE_DATE + " INTEGER," +
                        ToDoEntry.COL_COMPLETE_DATE + " INTEGER"
        );
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + ToDoEntry.TABLE_NAME;
    }

    public ToDoItemManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Get a single ToDoItem from the database by it's id
     * @param id the id of the ToDoItem
     * @return the ToDoItem or null
     */
    public ToDoItem get(int id) {

        ToDoItem item = null;
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = new String[] {
                ToDoEntry.COL_ID,
                ToDoEntry.COL_TITLE,
                ToDoEntry.COL_CREATE_DATE,
                ToDoEntry.COL_COMPLETE_DATE
        };
        String orderBy = ToDoEntry.COL_CREATE_DATE + " DESC";

        Cursor cursor = db.query(ToDoEntry.TABLE_NAME, projection, ToDoEntry.COL_ID + " = ?", new String[] {Integer.toString(id)}, null, null, orderBy);

        // create the ToDoItem from the cursor
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            item = instanceFromCursor(cursor);
        }
        cursor.close();

        return item;

    }

    /**
     * Get all the ToDoList items
     * @return a Cursor containing the results of all the ToDoList items
     */
    public Cursor getAll() {

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = new String[] {
                ToDoEntry.COL_ID,
                ToDoEntry.COL_TITLE,
                ToDoEntry.COL_CREATE_DATE,
                ToDoEntry.COL_COMPLETE_DATE
        };
        String orderBy = ToDoEntry.COL_CREATE_DATE + " DESC";

        return db.query(ToDoEntry.TABLE_NAME, projection, null, null, null, null, orderBy);

    }

    /**
     * Get all ToDoItems that are complete
     * @return a Cursor containing the results of the ToDoList items
     */
    public Cursor getComplete() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = new String[] {
                ToDoEntry.COL_ID,
                ToDoEntry.COL_TITLE,
                ToDoEntry.COL_CREATE_DATE,
                ToDoEntry.COL_COMPLETE_DATE
        };
        String orderBy = ToDoEntry.COL_CREATE_DATE + " DESC";
        String where = String.format("%s > ?", ToDoEntry.COL_COMPLETE_DATE);
        String[] whereArgs = new String[] {"0"};

        return db.query(ToDoEntry.TABLE_NAME, projection, where, whereArgs, null, null, orderBy);
    }

    /**
     * Converts a ToDoItem into a ContentValues object
     * @param item the ToDoItem to convert
     * @return A ContentValues with the ToDoItem values inside
     */
    private ContentValues makeContentValues(ToDoItem item) {
        // set the complete time to 0 if it is null
        long completeTime = item.getCompletionDate() == null ? 0 : item.getCompletionDate().getTime();

        ContentValues cv = new ContentValues();
        cv.put(ToDoEntry.COL_TITLE, item.getTitle());
        cv.put(ToDoEntry.COL_CREATE_DATE, item.getCreationDate().getTime());
        cv.put(ToDoEntry.COL_COMPLETE_DATE, completeTime);
        return cv;
    }


    /**
     * Permanently delete a ToDoItem
     * @param item the item to delete
     */
    public void delete(ToDoItem item) {
        if (item.getId() == -1) {
            return;
        }
        SQLiteDatabase db = getReadableDatabase();
        db.delete(ToDoEntry.TABLE_NAME, ToDoEntry.COL_ID + " = ?", new String[] {Integer.toString(item.getId())});
    }

    /**
     * Update a given ToDoItem
     * @param item the ToDoItem to update
     */
    public void update(ToDoItem item) {
        if (item.getId() == -1) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.update(ToDoEntry.TABLE_NAME, makeContentValues(item), ToDoEntry.COL_ID + " = ?", new String[]{Integer.toString(item.getId())});
    }

    /**
     * Sync a given list of ToDoItems using the Google Wear DataLayer
     * @param apiClient a connected GoogleApiClient
     * @param items the list of ToDoItems to sync
     */
    public static void sync(GoogleApiClient apiClient, List<ToDoItem> items) {

        Log.d("D", "syn list size: " + items.size());

        Parcel parcel = Parcel.obtain();
//        parcel.setDataPosition(0);  // TODO: check?
        parcel.writeTypedList(items);

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SharedConstants.Wearable.MESSAGE_REQUEST_TODO_ITEMS);
        DataMap dataMap = putDataMapRequest.getDataMap();
        byte[] bytes = parcel.marshall();
        dataMap.putByteArray("todolist", bytes);
        Log.d("D", "Byte Array size: " + bytes.length);

        parcel.recycle();
        Wearable.DataApi.putDataItem(apiClient, putDataMapRequest.asPutDataRequest()).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d("D", "sync successful");
            }
        });

    }

    /**
     * Get a list of ToDoItems from a DataMap
     * @param dataMap the DataMap to create a list from
     * @return a list of ToDoItems
     */
    public static List<ToDoItem> listFromDataMap(DataMap dataMap) {
        List<ToDoItem> items = new ArrayList<>();
        byte[] bytes = dataMap.getByteArray("todolist");
        if (bytes == null) {
            return items;
        }
        Log.d("D", "Byte Array size: " + bytes.length);
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        parcel.readTypedList(items, ToDoItem.CREATOR);
        Log.d("D", "list size: " + items.size());
        parcel.recycle();
        return items;
    }

    /**
     * Save a ToDoItem in the database
     * @param item the ToDoItem to save
     * @return
     */
    public long save(ToDoItem item) {
        SQLiteDatabase db = getWritableDatabase();
        long newId = db.insert(ToDoEntry.TABLE_NAME, "", makeContentValues(item));
        item.setId((int) newId);
        return newId;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SqlStatement.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SqlStatement.DROP_TABLE);
        onCreate(db);
    }

    /**
     * Create a ToDoItem from a Cursor
     * @param c the Cursor object to use
     * @return a new ToDoItem filled with the contents from the cursor
     */
    public static ToDoItem instanceFromCursor(Cursor c) {

        String title = c.getString(c.getColumnIndexOrThrow(ToDoItemManager.ToDoEntry.COL_TITLE));
        int id = c.getInt(c.getColumnIndexOrThrow(ToDoItemManager.ToDoEntry.COL_ID));
        String createDateString  = c.getString(c.getColumnIndexOrThrow(ToDoEntry.COL_CREATE_DATE));
        String completeDateString = c.getString(c.getColumnIndexOrThrow(ToDoEntry.COL_COMPLETE_DATE));

        long completeTime = Long.parseLong(completeDateString);

        Date createDate = new Date(Long.parseLong(createDateString));
        Date completeDate = completeTime == 0 ? null : new Date(completeTime);

        ToDoItem item = new ToDoItem(title, createDate, completeDate);
        item.setId(id);

        return item;

    }

    /**
     * Convert the given ToDoItem to a PutDataMapRequest
     * @param item the ToDoItem to convert
     * @return the new PutDataMapRequest object
     */
    public static PutDataMapRequest toPutDataMapRequest(ToDoItem item, String path) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putInt("id", item.getId());
        dataMap.putString("title", item.getTitle());
        dataMap.putLong("createDate", item.getCreationDate().getTime());
        dataMap.putLong("completeDate", item.getCompletionDate() == null ? 0 : item.getCompletionDate().getTime());
        return putDataMapRequest;
    }

    /**
     * Create a ToDoItem from a DataMap
     * @param dataMap the DataMap to create an item from
     * @return the new ToDoItem
     */
    public static ToDoItem fromDataMap(DataMap dataMap) {
        ToDoItem item = new ToDoItem(dataMap.getString("title"));
        item.setId(dataMap.getInt("id"));
        item.setCreationDate(new Date(dataMap.getLong("createDate")));
        long completeDate = dataMap.getLong("completeDate");
        if (completeDate > 0) {
            item.setCompletionDate(new Date(completeDate));
        }
        return item;
    }

}
