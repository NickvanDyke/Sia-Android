package vandyke.siamobile.misc;

import android.app.Application;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import vandyke.siamobile.R;

@ReportsCrashes(mailTo = "siamobiledev@gmail.com",
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = R.drawable.sia_logo_transparent,
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. When defined, adds a user text field input with this text resource as a label
        resDialogTheme = R.style.AppTheme_Light //optional. default is Theme.Dialog
)
public class SiaMobileApplication extends Application {

    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
    }
}
