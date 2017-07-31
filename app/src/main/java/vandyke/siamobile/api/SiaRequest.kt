/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.api

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.view.View
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import vandyke.siamobile.R
import vandyke.siamobile.SiaMobileApplication
import vandyke.siamobile.backend.coldstorage.ColdStorageHttpServer
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import java.io.UnsupportedEncodingException
import java.util.*

class SiaRequest(method: Int, destination: String, command: String, callback: VolleyCallback) : StringRequest(method, "http://" + destination + command, Response.Listener<String> { response ->
    try {
        val responseJson = if (response.isEmpty()) JSONObject() else JSONObject(response)
        callback.onSuccess(responseJson)
    } catch (e: JSONException) {
        e.printStackTrace()
    }
}, Response.ErrorListener { error -> callback.onError(Error(error)) }) {

    private val headers: HashMap<String, String> = HashMap()
    private val params: HashMap<String, String> = HashMap()

    init {
        headers.put("User-agent", "Sia-Agent")
        headers.put("Authorization", "Basic " + Base64.encodeToString(":${prefs.apiPass}".toByteArray(), 0))
    }

    constructor(method: Int, command: String, callback: VolleyCallback) : this(method, prefs.address, command, callback)

    override fun getHeaders(): Map<String, String> {
        return headers
    }

    override fun getParams(): Map<String, String> {
        return params
    }

    fun addHeader(key: String, value: String): SiaRequest {
        headers.put(key, value)
        return this
    }

    fun addParam(key: String, value: String): SiaRequest {
        params.put(key, value)
        return this
    }

    fun send() {
        SiaMobileApplication.requestQueue.add(this)
    }

    interface VolleyCallback {
        fun onSuccess(response: JSONObject)
        fun onError(error: Error)
    }

    class Error(error: VolleyError) {

        enum class Reason(val msg: String) {
            TIMEOUT("Response timed out"),
            NO_NETWORK_RESPONSE("No network response"),
            WALLET_PASSWORD_INCORRECT("Wallet password incorrect"),
            WALLET_LOCKED("Wallet must be unlocked first"),
            WALLET_ALREADY_UNLOCKED("Wallet already unlocked"),
            INVALID_AMOUNT("Invalid amount"),
            NO_NEW_PASSWORD("Must provide new password"),
            COULD_NOT_READ_ADDRESS("Could not read address"),
            AMOUNT_ZERO("Amount cannot be zero"),
            INSUFFICIENT_FUNDS("Insufficient funds"),
            EXISTING_WALLET("A wallet already exists. Use force option to overwrite"),
            INCORRECT_API_PASSWORD("Incorrect API password"),
            UNACCOUNTED_FOR_ERROR("Unexpected error"),
            WALLET_SCAN_IN_PROGRESS("Wallet scan in progress. Please wait"),
            WALLET_NOT_ENCRYPTED("Wallet has not been created yet"),
            INVALID_WORD_IN_SEED("Invalid word in seed"),
            CANNOT_INIT_FROM_SEED_UNTIL_SYNCED("Cannot create wallet from seed until blockchain is synced"),
            UNSUPPORTED_ON_COLD_WALLET("Unsupported on cold storage wallet")
        }

        var reason: Reason

        init {
            if (error is TimeoutError)
                reason = Reason.TIMEOUT
            else if (error.networkResponse != null) {
                try {
                    val response = String(error.networkResponse.data)
                    val responseJson = JSONObject(response)
                    val errorMessage = responseJson.getString("message")
                    reason = determineReason(errorMessage)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    reason = Reason.NO_NETWORK_RESPONSE
                } catch (e: JSONException) {
                    e.printStackTrace()
                    reason = Reason.NO_NETWORK_RESPONSE
                }

            } else {
                reason = Reason.NO_NETWORK_RESPONSE
            }
        }

        fun determineReason(errorMessage: String): Reason {
            if (errorMessage.contains("wallet must be unlocked before it can be used"))
                return Reason.WALLET_LOCKED
            else if (errorMessage.contains("provided encryption key is incorrect"))
                return Reason.WALLET_PASSWORD_INCORRECT
            else if (errorMessage.contains("wallet has already been unlocked"))
                return Reason.WALLET_ALREADY_UNLOCKED
            else if (errorMessage.contains("could not read 'amount'"))
                return Reason.INVALID_AMOUNT
            else if (errorMessage.contains("a password must be provided to newpassword"))
                return Reason.NO_NEW_PASSWORD
            else if (errorMessage.contains("could not read address"))
                return Reason.COULD_NOT_READ_ADDRESS
            else if (errorMessage.contains("transaction cannot have an output or payout that has zero value"))
                return Reason.AMOUNT_ZERO
            else if (errorMessage.contains("unable to fund transaction"))
                return Reason.INSUFFICIENT_FUNDS
            else if (errorMessage.contains("wallet is already encrypted, cannot encrypt again"))
                return Reason.EXISTING_WALLET
            else if (errorMessage.contains("API authentication failed"))
                return Reason.INCORRECT_API_PASSWORD
            else if (errorMessage.contains("another wallet rescan is already underway"))
                return Reason.WALLET_SCAN_IN_PROGRESS
            else if (errorMessage.contains("wallet has not been encrypted yet"))
                return Reason.WALLET_NOT_ENCRYPTED
            else if (errorMessage.contains("cannot init from seed until blockchain is synced"))
                return Reason.CANNOT_INIT_FROM_SEED_UNTIL_SYNCED
            else if (errorMessage.contains("unsupported on cold storage wallet"))
                return Reason.UNSUPPORTED_ON_COLD_WALLET
            else if (errorMessage.contains("word not found in dictionary for given language"))
                return Reason.INVALID_WORD_IN_SEED
            else {
                println(errorMessage)
                return Reason.UNACCOUNTED_FOR_ERROR
            }
        }

        val msg: String
            get() = reason.msg

        fun snackbar(view: View?) {
            if (view == null)
                return
            if (reason == Reason.UNSUPPORTED_ON_COLD_WALLET) {
                val snackbar = Snackbar.make(view, reason.msg, Snackbar.LENGTH_LONG).setAction("Help") { v -> ColdStorageHttpServer.showColdStorageHelp(view.context) }
                snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorAccent))
                snackbar.setActionTextColor(ContextCompat.getColor(view.context, android.R.color.white))
                snackbar.show()
            } else
                Utils.snackbar(view, reason.msg, Snackbar.LENGTH_SHORT)
        }
    }
}
