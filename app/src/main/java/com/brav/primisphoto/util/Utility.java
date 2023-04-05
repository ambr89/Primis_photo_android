package com.brav.primisphoto.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.io.File;

/**
 * Created by ambra on 26/10/2017.
 */

public class Utility {

    public static void setStringInternalPreference(Context ctx, String name_value, String value){
        SharedPreferences settings = ctx.getSharedPreferences(Constants.PREFS_INTERNAL_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name_value, value);
        editor.commit();
    }

    public static String getStringInternalPreference(Context ctx, String name_value){
        SharedPreferences settings = ctx.getSharedPreferences(Constants.PREFS_INTERNAL_DATA, Context.MODE_PRIVATE);
        return settings.getString(name_value,"");
    }

   /* public static String GetDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId.toUpperCase();
    }*/

    public static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
}
