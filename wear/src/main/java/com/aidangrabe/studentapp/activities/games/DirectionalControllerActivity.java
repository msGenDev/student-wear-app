package com.aidangrabe.studentapp.activities.games;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.aidangrabe.common.SharedConstants;
import com.aidangrabe.common.bluetooth.BluetoothClient;
import com.aidangrabe.common.wearable.WearUtil;
import com.aidangrabe.studentapp.R;
import com.aidangrabe.studentapp.views.DirectionalControllerView;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

/**
 * Created by aidan on 13/01/15.
 *
 */
public class DirectionalControllerActivity extends Activity implements DirectionalControllerView.DirectionalControllerListener {

    private BluetoothClient mClient;
    private static final String CONTROLLER_UUID = "4b6e3550-9f63-11e4-bcd8-0800200c9a66";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_directional_controller);

        mClient = new BluetoothClient(CONTROLLER_UUID);

        DirectionalControllerView view = (DirectionalControllerView) findViewById(R.id.directional_controller_view);
        view.setDirectionalControllerListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("D", "onResume, connecting");
        mClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();

        mClient.disconnect();

    }

    @Override
    public void onDirectionPressed(DirectionalControllerView.Dir direction) {

        switch (direction) {
            case UP:
                mClient.write(SharedConstants.Wearable.MESSAGE_GAME_CONTROLLER_UP);
                break;
            case DOWN:
                mClient.write(SharedConstants.Wearable.MESSAGE_GAME_CONTROLLER_DOWN);
                break;
            case RIGHT:
                mClient.write(SharedConstants.Wearable.MESSAGE_GAME_CONTROLLER_RIGHT);
                break;
            case LEFT:
                mClient.write(SharedConstants.Wearable.MESSAGE_GAME_CONTROLLER_LEFT);
                break;
        }

    }

}
