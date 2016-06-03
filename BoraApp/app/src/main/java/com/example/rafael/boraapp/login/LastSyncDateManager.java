package com.example.rafael.boraapp.login;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rafael on 30/05/16.
 */
public class LastSyncDateManager {


    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "LastSyncManager";

    public static final String KEY_LAST_SYNC = "last_synced";

    //VAI VIRAR UM SINGLETON PRA TER SO UM NO PROGRAMA
    private static LastSyncDateManager lastSyncDateManager;

    private LastSyncDateManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public static LastSyncDateManager getLastSyncDateManager(Context context){
        if(lastSyncDateManager == null){
            lastSyncDateManager = new LastSyncDateManager(context);
        }
        return lastSyncDateManager;
    }


    public void setKeyLastSync(Date lastSync){

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        String last_sync_str = df.format(lastSync);
        //Log.i("SESSION", "SET last_sync date: " + last_sync_str);

        editor.putString(KEY_LAST_SYNC, last_sync_str);
        editor.commit();
    }


    public Date getKeyLastSync(){

        String last_sync_str = pref.getString(KEY_LAST_SYNC, null);

        Date last_sync = null;
        if(last_sync_str !=null){

            //Log.i("SESSION", "GET last_sync date: " + last_sync_str);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            try {
                last_sync = sdf.parse(last_sync_str);
                //Log.i("SESSION", "GET last_sync converted: " + String.valueOf(last_sync));
                //last_sync = some_datetime;

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        // return user
        return last_sync;
    }


}
