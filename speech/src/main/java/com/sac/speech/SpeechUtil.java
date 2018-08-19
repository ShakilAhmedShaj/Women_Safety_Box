package com.sac.speech;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;


public class SpeechUtil {

    // private constructor to avoid instantiation
    private SpeechUtil() {}

    /**
     * Opens the Google App page on Play Store
     * @param context application context
     */
    public static void redirectUserToGoogleAppOnPlayStore(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("market://details?id=com.google.android.googlequicksearchbox")));
    }
}
