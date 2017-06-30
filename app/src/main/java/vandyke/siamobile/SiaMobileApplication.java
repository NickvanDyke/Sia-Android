package vandyke.siamobile;

import android.app.Application;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(mailTo = "siamobiledev@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class SiaMobileApplication extends Application {

    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
    }
}
