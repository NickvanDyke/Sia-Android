/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import vandyke.siamobile.misc.Utils;

@ReportsCrashes(mailTo = "siamobiledev@gmail.com",
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = R.drawable.sia_logo_transparent,
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. When defined, adds a user text field input with this text resource as a label
        resDialogTheme = R.style.AppTheme_Light //optional. default is Theme.Dialog
)
public class SiaMobileApplication extends Application {

    public static SharedPreferences prefs;
    public static RequestQueue requestQueue;
    public static String abi;
    public static String abi32;

    public void onCreate() {
        ACRA.init(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        requestQueue = Volley.newRequestQueue(this);
        Utils.createSiaNotificationChannel(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            abi = Build.CPU_ABI;
        else
            abi = Build.SUPPORTED_ABIS[0];
        if (abi.contains("arm"))
            abi32 = "arm32";
        else if (abi.contains("x86"))
            abi32 = "x86";
        if (abi.equals("arm64-v8a"))
            abi = "arm64";
        // TODO: maybe add mips binaries
        super.onCreate();
    }
}
