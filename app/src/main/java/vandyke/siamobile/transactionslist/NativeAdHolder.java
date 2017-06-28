package vandyke.siamobile.transactionslist;

import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import vandyke.siamobile.R;

public class NativeAdHolder extends GroupViewHolder {
    protected NativeExpressAdView adView;

    public NativeAdHolder(View itemView) {
        super(itemView);
        adView = (NativeExpressAdView)itemView.findViewById(R.id.nativeListAd);
        adView.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
    }
}