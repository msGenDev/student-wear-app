package com.aidangrabe.studentapp.wearable;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.aidangrabe.common.SharedConstants;
import com.aidangrabe.common.model.todolist.ToDoItem;
import com.aidangrabe.common.model.todolist.ToDoItemManager;
import com.aidangrabe.common.requests.StaticMapRequest;
import com.aidangrabe.common.util.MyVolley;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aidan on 09/01/15.
 * Service for listening for Wearable connections and messages on the Data Layer
 */
public class DataLayerListenerService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String path = messageEvent.getPath();

        // request todoitems
        if (path.endsWith(SharedConstants.Wearable.MESSAGE_REQUEST_TODO_ITEMS)) {
            sendToDoItems();
        }
        // find my phone
        else if (path.equals(SharedConstants.Wearable.MESSAGE_FIND_MY_PHONE)) {
            findMyPhone();
        }
        // map
        else if (path.equals(SharedConstants.Wearable.REQUEST_MAP)) {
            Log.d("D", "Downloading map image");
            StaticMapRequest request = new StaticMapRequest(51.8928387,-8.4942547, 17, 320, 320, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    Log.d("D", "Map successfully downloaded, width:  " + response.getWidth());
                    Asset map = createAssetFromBitmap(response);
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SharedConstants.Wearable.REQUEST_MAP);
                    DataMap dataMap = putDataMapRequest.getDataMap();
                    dataMap.putAsset("map", map);
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("D", "Error downloading Static Map: \n\n" + error.toString());
                }
            });
            MyVolley.getInstance(this).add(request);
        }

    }

    /**
     * Helper method to create an Asset from a Bitmap
     * @param bitmap the Bitmap to convert
     * @return the Asset
     */
    public Asset createAssetFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Asset.createFromBytes(byteArrayOutputStream.toByteArray());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("DEBUG", "onDataChanged");

        for (DataEvent event : dataEvents) {
            DataItem dataItem = event.getDataItem();

            // create or update a single ToDoItem
            if (dataItem.getUri().getPath().equals(SharedConstants.Wearable.MESSAGE_CREATE_TODO_ITEM)) {
                DataMap dataMap = DataMap.fromByteArray(dataItem.getData());
                ToDoItemManager itemManager = new ToDoItemManager(this);
                ToDoItem item = ToDoItemManager.fromDataMap(dataMap);

                if (item.getId() == -1) {
                    // create new
                    Log.d("D", "Saving new ToDoItem");
                    itemManager.save(item);
                } else {
                    // update existing
                    Log.d("D", "Updating existing ToDoItem");
                    itemManager.update(item);
                }

                // send over the updated items
                sendToDoItems();
            }
        }

    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
    }

    // sync the ToDoItems List using the DataApi
    private void sendToDoItems() {

        List<ToDoItem> items = queryToDoItems();
        ToDoItemManager.sync(mGoogleApiClient, items);

    }

    // create a list of ToDoItems from the database
    private List<ToDoItem> queryToDoItems() {

        ToDoItemManager manager = new ToDoItemManager(this);
        Cursor cursor = manager.getAll();
        List<ToDoItem> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(ToDoItemManager.instanceFromCursor(cursor));
        }
        cursor.close();
        return items;

    }

    // plays a noise so the user can find their phone
    private void findMyPhone() {

        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_RING);

        // set max volume
        manager.setStreamVolume(AudioManager.STREAM_RING, manager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
