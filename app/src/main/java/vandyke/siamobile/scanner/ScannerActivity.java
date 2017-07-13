package vandyke.siamobile.scanner;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import vandyke.siamobile.R;

import static vandyke.siamobile.wallet.fragments.WalletSendFragment.SCAN_RESULT_KEY;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        scannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {

        String result = rawResult.getText();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SCAN_RESULT_KEY, result);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();

/*
        // If you would like to resume scanning, call this method below:
        scannerView.resumeCameraPreview(this);
        */
    }
}
