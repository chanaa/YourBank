package com.hps.esecure.yourbank.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by namri on 31/01/2018.
 */

public class SettingsPreferenceHelper {

    private static final String YOURBANK_SIMULATOR_PREFERENCES = "YOURBANK_SIMULATOR_PREFERENCES";

    private static final String OOB_VERIFY_URL = "OOB_VERIFY_URL";

    private SettingsPreferenceHelper() {
    }

    public static void setOobVerifyUrl(Context context, String uiInterface){
        SharedPreferences settings = context.getSharedPreferences(YOURBANK_SIMULATOR_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(OOB_VERIFY_URL, uiInterface);
        editor.apply();
    }

    public static String getOobVerifyUrl(Context context){
        SharedPreferences settings = context.getSharedPreferences(YOURBANK_SIMULATOR_PREFERENCES, Context.MODE_PRIVATE);
        return settings.getString(OOB_VERIFY_URL, "https://10.0.2.2:8443/acs-simulator/dummyOobVerifyServlet");
    }


}
