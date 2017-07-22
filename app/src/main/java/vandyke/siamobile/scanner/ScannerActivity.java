/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.zxing.Result;
import com.tbruyelle.rxpermissions2.RxPermissions;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static vandyke.siamobile.wallet.fragments.WalletSendFragment.SCAN_RESULT_KEY;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        new RxPermissions(this).request(Manifest.permission.CAMERA)
                .subscribe(this::onPermissionResult);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    public void onPermissionResult(boolean granted) {
        if (!granted) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        String result = rawResult.getText();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SCAN_RESULT_KEY, result);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
