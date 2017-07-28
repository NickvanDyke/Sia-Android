/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.api

import com.android.volley.Request.Method.GET
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.SiaMobileApplication
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import java.math.BigDecimal
import java.math.RoundingMode

object Wallet {

    fun wallet(callback: SiaRequest.VolleyCallback) {
        SiaRequest(GET, "/wallet", callback).send()
    }

    fun unlock(password: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/unlock", callback)
                .addParam("encryptionpassword", password)
                .send()
    }

    fun lock(callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/lock", callback).send()
    }

    fun newAddress(callback: SiaRequest.VolleyCallback) {
        SiaRequest(GET, "/wallet/address", callback).send()
    }

    fun addresses(callback: SiaRequest.VolleyCallback) {
        SiaRequest(GET, "/wallet/addresses", callback).send()
    }

    fun init(password: String, force: Boolean, dictionary: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/init", callback)
                .addParam("encryptionpassword", password)
                .addParam("force", if (force) "true" else "false")
                .addParam("dictionary", dictionary)
                .send()
    }

    fun initSeed(password: String, force: Boolean, dictionary: String, seed: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/init/seed", callback)
                .addParam("encryptionpassword", password)
                .addParam("force", if (force) "true" else "false")
                .addParam("dictionary", dictionary)
                .addParam("seed", seed)
                .send()
    }

    fun seed(password: String, dictionary: String, seed: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/seed", callback)
                .addParam("encryptionpassword", password)
                .addParam("dictionary", dictionary)
                .addParam("seed", seed)
                .send()
    }

    fun seeds(dictionary: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(GET, String.format("/wallet/seeds?dictionary=%s", dictionary), callback)
                .send()
    }


    fun changePassword(currentPassword: String, newPassword: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/changepassword", callback)
                .addParam("encryptionpassword", currentPassword)
                .addParam("newpassword", newPassword)
                .send()
    }

    /** amount should be in hastings  */
    fun sendSiacoins(amount: BigDecimal, recipient: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/siacoins", callback)
                .addParam("amount", amount.setScale(0, 0).toPlainString())
                .addParam("destination", recipient)
                .send()
    }

    fun sendSiacoinsWithDevFee(amount: BigDecimal, recipient: String, callback: SiaRequest.VolleyCallback) {
        val outputs = JSONArray()
        val regOutput = JSONObject()
        val feeOutput = JSONObject()
        try {
            outputs.put(regOutput)
            outputs.put(feeOutput)
            feeOutput.put("unlockhash", Utils.devAddresses[(Math.random() * Utils.devAddresses.size).toInt()])
            val devAmount = calculateDevFee(amount)
            feeOutput.put("value", devAmount)
            regOutput.put("unlockhash", recipient)
            regOutput.put("value", amount.setScale(0, 0).toPlainString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // TODO: request isn't formatted properly? responds that it couldn't read amount
        SiaRequest(POST, "/wallet/siacoins", callback)
                .addParam("outputs", outputs.toString())
                //                .addParam("amount", "")
                //                .addParam("destination", "")
                .send()
    }

    fun sendSiafunds(amount: String, recipient: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/siafunds", callback)
                .addParam("amount", amount)
                .addParam("destination", recipient)
                .send()
    }

    fun sweepSeed(dictionary: String, seed: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(POST, "/wallet/sweep/seed", callback)
                .addParam("dictionary", dictionary)
                .addParam("seed", seed)
                .send()
    }

    fun transaction(id: String, callback: SiaRequest.VolleyCallback) {
        SiaRequest(GET, String.format("/wallet/transaction/%s", id), callback)
                .send()
    }

    fun transactions(callback: SiaRequest.VolleyCallback) {
        // TODO: maybe use actual value instead of really big literal lol
        SiaRequest(GET, String.format("/wallet/transactions?startheight=%s&endheight=%s", "0", "2000000000"), callback)
                .send()
    }

    fun coincapSC(listener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        val request = StringRequest(GET, "http://www.coincap.io/page/SC", listener, errorListener)
        SiaMobileApplication.requestQueue!!.add(request)
    }

    fun hastingsToSC(hastings: String): BigDecimal {
        return BigDecimal(hastings).divide(BigDecimal("1000000000000000000000000"))
    }

    fun hastingsToSC(hastings: BigDecimal): BigDecimal {
        return hastings.divide(BigDecimal("1000000000000000000000000"))
    }

    fun scToHastings(sc: String): BigDecimal {
        if (sc == "")
            return BigDecimal("0").multiply(BigDecimal("1000000000000000000000000"))
        else
            return BigDecimal(sc).multiply(BigDecimal("1000000000000000000000000"))
    }

    fun scToHastings(sc: BigDecimal): BigDecimal {
        return sc.multiply(BigDecimal("1000000000000000000000000"))
    }

    fun round(num: BigDecimal): String {
        return num.setScale(prefs.displayedDecimalPrecision, BigDecimal.ROUND_CEILING).toPlainString()
    }

    /** will return value in the same units they were passed in, without decimal  */
    fun calculateDevFee(amount: BigDecimal): String {
        return amount.multiply(Utils.devFee).setScale(0, RoundingMode.FLOOR).toPlainString()
    }

    fun calculateDevFee(amount: String): String {
        return BigDecimal(amount).multiply(Utils.devFee).setScale(0, RoundingMode.FLOOR).toPlainString()
    }

    fun usdInSC(usdPrice: Double, targetUsd: String): BigDecimal {
        return BigDecimal(targetUsd).divide(BigDecimal(usdPrice), 5, BigDecimal.ROUND_CEILING)
    }

    fun scToUsd(usdPrice: Double, numHastings: BigDecimal): BigDecimal {
        return numHastings.multiply(BigDecimal(usdPrice))
    }
}
