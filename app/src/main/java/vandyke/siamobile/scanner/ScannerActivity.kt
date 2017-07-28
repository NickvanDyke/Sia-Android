/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import vandyke.siamobile.wallet.fragments.WalletSendFragment

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)
//        RxPermissions(this).request(Manifest.permission.CAMERA)
//                .subscribe(???({ this.onPermissionResult(it) }))
    }

    public override fun onResume() {
        super.onResume()
        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        scannerView?.stopCamera()
    }

    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }
    }

    override fun handleResult(rawResult: Result) {
        val result = rawResult.text
        val returnIntent = Intent()
        returnIntent.putExtra(WalletSendFragment.SCAN_RESULT_KEY, result)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
