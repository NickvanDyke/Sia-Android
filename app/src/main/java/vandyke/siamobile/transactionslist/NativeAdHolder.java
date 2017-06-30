package vandyke.siamobile.transactionslist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import vandyke.siamobile.R;

public class NativeAdHolder extends RecyclerView.ViewHolder {
    protected NativeExpressAdView adView;

    public NativeAdHolder(View itemView) {
        super(itemView);
        adView = (NativeExpressAdView)itemView.findViewById(R.id.nativeListAd);
        adView.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
    }
}