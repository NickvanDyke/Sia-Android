/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.coldstorage

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import siawallet.Wallet
import vandyke.siamobile.misc.Utils
import vandyke.siamobile.prefs
import java.io.IOException
import java.util.ArrayList
import kotlin.collections.HashSet

class ColdStorageHttpServer(private val context: Context) : NanoHTTPD("localhost", 9990) {

    private var seed: String? = null
    private val addresses: ArrayList<String>
    private var password: String? = null
    private var exists: Boolean = false
    private var unlocked: Boolean = false

    init {
        seed = prefs.coldStorageSeed
        addresses = ArrayList(prefs.coldStorageAddresses)
        password = prefs.coldStoragePassword
        exists = prefs.coldStorageExists
        unlocked = false
    }

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val uri = session.uri
        val response = JSONObject()
        var status: Response.Status = NanoHTTPD.Response.Status.OK
        val parms = session.parms
        try {
            session.parseBody(parms)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NanoHTTPD.ResponseException) {
            e.printStackTrace()
        }

        try {
            when (uri) {
                "/wallet/addresses" -> if (checkExists(response) && checkUnlocked(response)) {
                    val addressArray = JSONArray()
                    for (address in addresses)
                        addressArray.put(address)
                    response.put("addresses", addressArray)
                } else
                    status = NanoHTTPD.Response.Status.BAD_REQUEST
                "/wallet/address" -> if (checkExists(response) && checkUnlocked(response)) {
                    response.put("address", addresses[(Math.random() * addresses.size).toInt()])
                } else
                    status = NanoHTTPD.Response.Status.BAD_REQUEST
                "/wallet/seeds" -> if (checkExists(response) && checkUnlocked(response)) {
                    val seedsArray = JSONArray()
                    seedsArray.put(seed)
                    response.put("allseeds", seedsArray)
                } else
                    status = NanoHTTPD.Response.Status.BAD_REQUEST
                "/wallet/init" -> if (!exists || parms["force"] == "true") {
                    newWallet(parms["encryptionpassword"]!!)
                    response.put("primaryseed", seed)
                } else {
                    response.put("message", "wallet is already encrypted, cannot encrypt again")
                    status = NanoHTTPD.Response.Status.BAD_REQUEST
                }
                "/wallet/unlock" -> if (checkExists(response) && parms["encryptionpassword"] == password) {
                    unlocked = true
                } else {
                    status = NanoHTTPD.Response.Status.BAD_REQUEST
                    response.put("message", "provided encryption key is incorrect")
                }
                "/wallet/lock" -> if (checkExists(response))
                    unlocked = false
                else
                    status = NanoHTTPD.Response.Status.BAD_REQUEST
                "/wallet" -> {
                    response.put("encrypted", exists)
                    response.put("unlocked", unlocked)
                    response.put("rescanning", false)
                    response.put("confirmedsiacoinbalance", 0)
                    response.put("unconfirmedoutgoingsiacoins", 0)
                    response.put("unconfirmedincomingsiacoins", 0)
                    response.put("siafundbalance", 0)
                    response.put("siacoinclaimbalance", 0)
                }
                "/wallet/transactions" -> {
                }
                "/consensus" -> {
                    response.put("synced", false)
                    response.put("height", 0)
                }
                else -> {
                    response.put("message", "unsupported on cold storage wallet")
                    status = NanoHTTPD.Response.Status.NOT_IMPLEMENTED
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val httpResponse = NanoHTTPD.newFixedLengthResponse(response.toString())
        httpResponse.status = status
        return httpResponse
    }

    @Throws(JSONException::class)
    private fun checkUnlocked(response: JSONObject): Boolean {
        if (!unlocked) {
            response.put("message", "wallet must be unlocked before it can be used")
        }
        return unlocked
    }

    @Throws(JSONException::class)
    private fun checkExists(response: JSONObject): Boolean {
        if (!exists) {
            response.put("message", "wallet has not been encrypted yet")
        }
        return exists
    }

    fun newWallet(password: String) {
        val wallet = Wallet()
        try {
            wallet.generateSeed()
            seed = wallet.seed
        } catch (e: Exception) {
            e.printStackTrace()
            seed = "Failed to generate seed"
        }

        addresses.clear()
        for (i in 0..19)
            addresses.add(wallet.getAddress(i.toLong()))

        this.password = password
        exists = true
        unlocked = false
        prefs.coldStorageSeed = seed!!
        prefs.coldStorageAddresses = HashSet(addresses)
        prefs.coldStoragePassword = password
        prefs.coldStorageExists = exists
    }

    companion object {

        fun showColdStorageHelp(context: Context) {
            Utils.getDialogBuilder(context)
                    .setTitle("Cold storage help")
                    .setMessage("Sia Mobile's cold storage wallet operates independently of the Sia network." +
                            " Since it doesn't have a copy of the Sia blockchain and is not connected to the " +
                            "Sia network, it cannot perform certain functions that require this. It also cannot display your correct balance and transactions." +
                            "\n\nIf you wish to use unsupported functions, or view your cold wallet balance and transactions, you will have to run a full" +
                            " Sia node (either in Sia Mobile or using something like Sia-UI on your computer), and then load your" +
                            " wallet seed on that full node. Your coins are not \"lost\" - if you did everything properly, they will be there when you load your seed" +
                            " on a full node at any time in the future. No need to worry.")
                    .setPositiveButton("OK", null)
                    .show()
        }
    }
}
