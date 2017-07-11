/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import vandyke.siamobile.R;

public class ModesActivity extends AppIntro {

    public static int COLD_STORAGE = 0;
    public static int REMOTE_FULL_NODE = 1;
    public static int LOCAL_FULL_NODE = 2;

    private int currentSlide;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Modes",
                "Sia Mobile can operate in three different modes, which are explained in the following slides. Press the " +
                        "lower-left button on the slide of the mode you want. The modes are independent - changes " +
                        "made while in one mode will not affect other modes. You can change mode at any time in Settings, " +
                        "as well as view this again by selecting Help from the drawer.",
                R.drawable.ic_settings_black_48dp, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Cold storage",
                "Most secure. Limited functionality - can only view receive addresses and seed. Meant for securely" +
                        " storing coins for long periods of time. Completely offline - to see your correct balance and transactions, you'll" +
                        " have to load your seed on a full node, such as Sia-UI.",
                R.drawable.safe_image, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Remote full node",
                "Run a full node on your computer, and control it from Sia Mobile. Allows all Sia features. Some setup required.",
                R.drawable.remote_node_graphic, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Local full node",
                "Run a full node on your device. Completely independent. Allows all Sia features. Must " +
                        "sync Sia blockchain, which uses significant storage and bandwidth - about 5GB.",
                R.drawable.local_node_graphic, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));

        setBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setDoneText("Close");
//        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(false);
//        setProgressButtonEnabled(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        switch (currentSlide) {
            case 1:
                setResult(COLD_STORAGE);
                break;
            case 2:
                setResult(REMOTE_FULL_NODE);
                break;
            case 3:
                setResult(LOCAL_FULL_NODE);
                break;
        }
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
        int i;
        for (i = 0; i < getSlides().size(); i++) {
            if (newFragment == getSlides().get(i))
                break;
        }
        currentSlide = i;
        showSkipButton(currentSlide != 0);
        switch (currentSlide) {
            case 1:
                setSkipText("Create");
                break;
            case 2:
                setSkipText("Setup");
                break;
            case 3:
                setSkipText("Start");
                showSkipButton(true);
                break;
        }
    }
}
