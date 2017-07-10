package vandyke.siamobile.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import vandyke.siamobile.R;

public class AboutActivity extends AppIntro {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
//        addSlide(firstFragment);
//        addSlide(secondFragment);
//        addSlide(thirdFragment);
//        addSlide(fourthFragment);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Completely private",
                "Sia splits apart, encrypts, and distributes your files across a decentralized network. Since you hold the keys, you own your data. No outside company can access or control your files, unlike traditional cloud storage providers.",
                R.drawable.siainfo1, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Far more affordable",
                "Sia's decentralized cloud is on average 10x less expensive than current cloud storage providers. Storing 1TB on Sia costs about $2 per month, compared with $23 on Amazon S3.",
                R.drawable.siainfo2, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Highly redundant",
                "Sia stores tiny pieces of your files on dozens of nodes across the globe. This eliminates any single point of failure and ensures highest possible uptime, on par with other cloud storage providers.",
                R.drawable.siainfo3, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Open source",
                "Sia is completely open source. Over a dozen individuals have contributed to Sia's software, and there is an active community building innovative applications on top of the Sia API.",
                R.drawable.siainfo4, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Blockchain marketplace",
                "Using the Sia blockchain, Sia creates a decentralized storage marketplace in which hosts compete for your business – this leads to the lowest possible prices. Renters pay using Siacoin, which can also be mined and traded.",
                R.drawable.siainfo5, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
//        showSkipButton(false);
//        setProgressButtonEnabled(false);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
//        setVibrate(true);
//        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
