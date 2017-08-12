/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.backend.coldstorage

import android.content.Context
import android.os.Handler
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import siawallet.Wallet
import vandyke.siamobile.backend.models.explorer.ExplorerTransactionModel
import vandyke.siamobile.backend.models.wallet.TransactionInputModel
import vandyke.siamobile.backend.models.wallet.TransactionModel
import vandyke.siamobile.backend.models.wallet.TransactionOutputModel
import vandyke.siamobile.backend.networking.Explorer
import vandyke.siamobile.backend.networking.SiaCallback
import vandyke.siamobile.prefs
import vandyke.siamobile.util.GenUtil
import vandyke.siamobile.util.SCUtil
import java.io.IOException
import java.math.BigDecimal

class ColdStorageHttpServer : NanoHTTPD("localhost", 9990) {
    private var handler: Handler = Handler()
    private val UNCACHED_DELAY: Long = 1000

    init {
        postRefreshRunnable()
    }

    private var seed: String = prefs.coldStorageSeed
    private var addresses: ArrayList<String> = ArrayList(prefs.coldStorageAddresses)
    private var password: String = prefs.coldStoragePassword
    private var exists: Boolean = prefs.coldStorageExists
    private var unlocked: Boolean = false

    private var balanceHastings: BigDecimal = BigDecimal.ZERO
    private var transactions: ArrayList<TransactionModel> = ArrayList()
    private var height: Long = 0
    private var synced: Boolean = false

    private var refreshRunnable: Runnable? = null

    private val unlockResponse
        get() = response(JSONObject().put("message", "wallet must be unlocked before it can be used"), Response.Status.BAD_REQUEST)
    private val createResponse
        get() = response(JSONObject().put("message", "wallet has not been encrypted yet"), Response.Status.BAD_REQUEST)

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val parms = session.parms
        try {
            session.parseBody(parms)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NanoHTTPD.ResponseException) {
            e.printStackTrace()
        }

        return when (session.uri) {
            "/wallet/address" -> address()
            "/wallet/addresses" -> addresses()
            "/wallet/seeds" -> seeds()
            "/wallet/init" -> init(parms["encryptionpassword"], parms["force"] == "true")
//            "/wallet/init/seed" -> initSeed(parms["encryptionpassword"], parms["seed"], parms["force"] == "true") TODO: need to check given seed for validity
            "/wallet/unlock" -> unlock(parms["encryptionpassword"])
            "/wallet/lock" -> lock()
            "/wallet" -> wallet()
            "/wallet/transactions" -> transactions()
            "/consensus" -> consensus()
            else -> response(JSONObject().put("message", "unsupported on cold storage wallet"), Response.Status.NOT_IMPLEMENTED)
        }
    }

    fun postRefreshRunnable() { // TODO: if it's not synced and receives wallet, transaction, or consensus call, it should make blocking calls to sync and then respond after
        if (refreshRunnable != null)
            handler.removeCallbacks(refreshRunnable)
        val refreshInterval = 59000 * 1
        if (refreshInterval == 0)
            return
        refreshRunnable = Runnable {
            refresh()
            handler.postDelayed(refreshRunnable, refreshInterval.toLong())
        }
        handler.post(refreshRunnable)
    }

    fun refresh() {
        Explorer.siaTech(SiaCallback({ it ->
            synced = true
            if (height != it.height) {
                height = it.height
//                addresses = arrayListOf("20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3", "4c06e08c8689625ddf9831415706529673077325d08e9c16be401d348270937c2db7e284a57f")
                for (address in addresses) {
                    Explorer.siaTechHash(address, SiaCallback({ it ->
                        val txModels: ArrayList<TransactionModel> = ArrayList()
                        for (tx in it.transactions) txModels += tx.toTransactionModel()
                        for (tx in txModels) {
                            if (tx !in transactions) {
                                transactions.add(tx)
                                balanceHastings += tx.netValue
                            }
                        }
                    }, {

                    }))
                }
            }
        }, {
            synced = false
        }))
    }

    fun init(password: String?, force: Boolean): Response {
        if (exists && !force)
            return response(JSONObject().put("message", "wallet is already encrypted, cannot encrypt again"), Response.Status.BAD_REQUEST)
        initWallet(password ?: "")
        return response(JSONObject().put("primaryseed", seed))
    }

    fun initSeed(password: String?, seed: String?, force: Boolean): Response {
        if (exists && !force)
            return response(JSONObject().put("message", "wallet is already encrypted, cannot encrypt again"), Response.Status.BAD_REQUEST)
        initWallet(password ?: "", seed ?: "")
        return response()
    }

    fun wallet(): Response {
        if (!synced) runBlocking { delay(UNCACHED_DELAY) }
        return response(JSONObject().put("encrypted", exists)
                .put("unlocked", unlocked)
                .put("rescanning", false)
                .put("confirmedsiacoinbalance", balanceHastings)
                .put("unconfirmedoutgoingsiacoins", 0)
                .put("unconfirmedincomingsiacoins", 0)
                .put("siafundbalance", 0)
                .put("siacoinclaimbalance", 0))
    }

    fun seeds(): Response = when {
        !exists -> createResponse
        !unlocked -> unlockResponse
        else -> response(JSONObject().put("allseeds", JSONArray().put(seed)))
    }

    fun unlock(password: String?): Response {
        if (!exists)
            return createResponse
        if (password == this.password) {
            unlocked = true
            return response()
        }
        return response(JSONObject().put("message", "provided encryption key is incorrect"), Response.Status.BAD_REQUEST)
    }

    fun lock(): Response = when {
        !exists -> createResponse
        else -> {
            unlocked = false
            response()
        }
    }

    fun transactions(): Response {
        if (!synced) runBlocking { delay(UNCACHED_DELAY) }
        val array = JSONArray()
        for (tx in transactions.sortedWith(Comparator<TransactionModel> { p0, p1 -> (p0.confirmationheight - p1.confirmationheight).toInt() })) {
            array.put(createJsonFromTx(tx))
        }
        return response(JSONObject().put("confirmedtransactions", array))
    }

    fun ExplorerTransactionModel.toTransactionModel(): TransactionModel {
        val inputsList = ArrayList<TransactionInputModel>()
        for (input in siacoininputoutputs)
            inputsList.add(TransactionInputModel(walletaddress = addresses.contains(input.unlockhash), value = input.value))
        val outputsList = ArrayList<TransactionOutputModel>()
        for (output in rawtransaction.siacoinoutputs)
            outputsList.add(TransactionOutputModel(walletaddress = addresses.contains(output.unlockhash), value = output.value))
        return TransactionModel(id, BigDecimal(height), BigDecimal(SCUtil.estimatedTimeAtHeight(height)), inputsList, outputsList)
    }

    fun createJsonFromTx(tx: TransactionModel): JSONObject {
        val txJson = JSONObject()
        // put the basic tx info
        txJson.put("transactionid", tx.transactionid)
        txJson.put("confirmationheight", tx.confirmationheight)
        txJson.put("confirmationtimestamp", tx.confirmationtimestamp)
        // put the tx inputs and outputs
        val inputs = JSONArray()
        for (input in tx.inputs) {
            val inputJson = JSONObject()
            inputJson.put("walletaddress", input.walletaddress)
            inputJson.put("relatedaddress", input.relatedaddress)
            inputJson.put("value", input.value)
            inputs.put(inputJson)
        }
        txJson.put("inputs", inputs)
        val outputs = JSONArray()
        for (output in tx.outputs) {
            val outputJson = JSONObject()
            outputJson.put("walletaddress", output.walletaddress)
            outputJson.put("relatedaddress", output.relatedaddress)
            outputJson.put("value", output.value)
            outputs.put(outputJson)
        }
        txJson.put("outputs", outputs)
        return txJson
    }

    fun address(): Response = when {
        !exists -> createResponse
        !unlocked -> unlockResponse
        else -> response(JSONObject().put("address", addresses[(Math.random() * addresses.size).toInt()]))
    }

    fun addresses(): Response = when {
        !exists -> createResponse
        !unlocked -> unlockResponse
        else -> {
            val addressArray = JSONArray()
            for (address in addresses)
                addressArray.put(address)
            response(JSONObject().put("addresses", addressArray))
        }
    }

    fun consensus(): Response {
        if (!synced) runBlocking { delay(UNCACHED_DELAY) }
        return response(JSONObject().put("synced", synced).put("height", height))
    }

    fun response(json: JSONObject = JSONObject(), status: Response.Status = Response.Status.OK): Response {
        val response = NanoHTTPD.newFixedLengthResponse(json.toString())
        response.status = status
        return response
    }

    fun initWallet(password: String, seed: String? = null) {
        val wallet = Wallet()
        if (seed == null) {
            try {
                wallet.generateSeed()
                this.seed = wallet.seed
            } catch (e: Exception) {
                e.printStackTrace()
                this.seed = "Failed to generate seed"
            }
        } else {
            wallet.seed = seed
        }

        addresses.clear()
        for (i in 0..4)
            addresses.add(wallet.getAddress(i.toLong()))

        this.password = password
        exists = true
        unlocked = false
        prefs.coldStorageSeed = this.seed
        prefs.coldStorageAddresses = HashSet(addresses)
        prefs.coldStoragePassword = password
        prefs.coldStorageExists = exists
    }

    companion object {
        fun showColdStorageHelp(context: Context) {
            GenUtil.getDialogBuilder(context)
                    .setTitle("Cold storage help")
                    .setMessage("Sia Mobile's cold storage wallet operates independently of the Sia network." +
                            " Since it doesn't have a copy of the Sia blockchain and is not connected to the " +
                            "Sia network, it cannot perform certain functions that require this. It will still attempt to ESTIMATE your CONFIRMED balance and transactions" +
                            " using explore.sia.tech." +
                            "\n\nIf you wish to use unsupported functions, or view your cold wallet balance and transactions, you can, at any time, run a full" +
                            " Sia node (either in Sia Mobile or using Sia-UI on your computer), and then load your" +
                            " wallet seed on that full node.")
                    .setPositiveButton("OK", null)
                    .show()
        }
    }
}
