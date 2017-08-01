/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import vandyke.siamobile.R;

public class AboutSiaActivity extends AppIntro {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: need to make this more memory efficient while still using sharp images

        addSlide(AppIntroFragment.newInstance("Completely private",
                "Sia splits apart, encrypts, and distributes your files across a decentralized network. Since you hold the keys, you own your data. No outside company can access or control your files, unlike traditional cloud storage providers.",
                R.drawable.siainfo1, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Far more affordable",
                "Sia's decentralized cloud is on average significantly less expensive than current cloud storage providers.",
                R.drawable.siainfo2, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Highly redundant",
                "Sia stores tiny pieces of your files on dozens of nodes across the globe. This eliminates any " +
                        "single point of failure and ensures highest possible uptime, superior to traditional cloud storage providers.",
                R.drawable.siainfo3, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Open source",
                "Sia is completely open source. Over a dozen individuals have contributed to Sia's software, and " +
                        "there is an active community building innovative applications on top of the Sia API.",
                R.drawable.siainfo4, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Blockchain marketplace",
                "Using the Sia blockchain, Sia creates a decentralized storage marketplace in which hosts compete for your business – this leads to the lowest possible prices. Renters pay using Siacoin, which can also be mined and traded.",
                R.drawable.siainfo5, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));

        setBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}
