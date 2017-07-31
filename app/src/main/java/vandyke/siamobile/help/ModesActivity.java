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
import android.widget.Toast;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import vandyke.siamobile.R;
import vandyke.siamobile.util.StorageUtil;

public class ModesActivity extends AppIntro {

    public static int PAPER_WALLET = 1;
    public static int COLD_STORAGE = 2;
    public static int REMOTE_FULL_NODE = 3;
    public static int LOCAL_FULL_NODE = 4;

    private int currentSlide;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Modes",
                "Sia Mobile can operate in multiple modes, explained in the following slides. The modes are independent - changes " +
                        "made while in one mode will not affect other modes. You can change mode at any time in Settings, " +
                        "and view this again by selecting Help from the drawer.",
                R.drawable.sia_logo_svg, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Paper walletModel",
                "Generates a fresh seed and addresses from it. You can send coins to any of the addresses, and later load" +
                        " the seed on a full node to access the coins. Sia Mobile does not save any of this" +
                        " info for you - record it elsewhere.",
                R.drawable.paper_wallet_svg, ContextCompat.getColor(this, android.R.color.white),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray)));
        addSlide(AppIntroFragment.newInstance("Cold storage",
                "Similar to a paper walletModel, except Sia Mobile will store the generated seed and addresses for you. Only for receiving and storing coins. " +
                        "Completely offline the Sia network - like a paper walletModel, to see your correct balance and transactions and access/send your coins, you'll" +
                        " have to load your seed on a full node.",
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

        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        switch (currentSlide) {
            case 1:
                setResult(PAPER_WALLET);
                break;
            case 2:
                setResult(COLD_STORAGE);
                break;
            case 3:
                setResult(REMOTE_FULL_NODE);
                break;
            case 4:
                if (StorageUtil.INSTANCE.isSiadSupported()) {
                    setResult(LOCAL_FULL_NODE);
                } else {
                    Toast.makeText(this, "Sorry, but your device's CPU architecture is not supported by Sia's full node", Toast.LENGTH_LONG).show();
                    return;
                }
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
                setSkipText("Generate");
                break;
            case 2:
                setSkipText("Create");
                break;
            case 3:
                setSkipText("Setup");
                break;
            case 4:
                setSkipText("Start");
                showSkipButton(true);
                break;
        }
    }
}
