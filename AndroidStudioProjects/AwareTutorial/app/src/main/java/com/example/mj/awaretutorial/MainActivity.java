package com.example.mj.awaretutorial;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.aware.Aware;
import com.aware.Aware_Preferences;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent aware = new Intent(this,Aware.class);
        startService(aware);

        ContentValues new_data = new ContentValues();
        new_data.put(bandProvider.Band_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        new_data.put(bandProvider.Band_Data.TIMESTAMP, System.currentTimeMillis());
        new_data.put(bandProvider.Band_Data.HeartRate, 75);
        //put the rest of the columns you defined

        //Insert the data to the ContentProvider
        getContentResolver().insert(bandProvider.Band_Data.CONTENT_URI, new_data);

        //Activate Acceloerometer
        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
        //set sampling frequency
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 20000);
        //apply setting
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
