/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data

import android.arch.persistence.room.EmptyResultSetException
import android.database.sqlite.SQLiteConstraintException
import android.support.design.widget.Snackbar
import android.view.View
import com.vandyke.sia.data.SiaError.Reason.*
import com.vandyke.sia.util.SnackbarUtil
import io.reactivex.exceptions.CompositeException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SiaError : Throwable {
    override val message: String?
        get() = reason.msg

    override fun getLocalizedMessage(): String {
        return reason.msg
    }

    val reason: Reason

    /** if this SiaError was constructed from a CompositeException, then it's list of exceptions will be stored here.
      * Otherwise, this is null. */
    var exceptions: List<Throwable>? = null

    constructor(errorMessage: String) {
        reason = getReasonFromMsg(errorMessage)
    }

    constructor(t: Throwable) {
        exceptions = listOf(t)
        reason = when (t) {
            is SiaError -> {
                println("Chaining SiaErrors")
                t.reason
            }
            /* So far I've only gotten a CompositeException from using mergeArrayDelayError.
             * So be wary that important ones aren't being swallowed by only taking the first error here */
            is CompositeException -> {
                exceptions = t.exceptions
                getReasonFromThrowable(t.exceptions[0])
            }
            else -> getReasonFromThrowable(t)
        }
    }

    constructor(reason: Reason) {
        this.reason = reason
    }

    private fun getReasonFromMsg(errorMessage: String) = when {
        /* common */
        errorMessage.contains("siad is not ready") -> SIAD_LOADING
        errorMessage.contains("API authentication failed") -> INCORRECT_API_PASSWORD
        errorMessage.contains("404 - Refer to API.md") -> SIA_FOUR_OH_FOUR
        /* wallet */
        errorMessage.contains("wallet must be unlocked before it can be used") -> WALLET_LOCKED
        errorMessage.contains("provided encryption key is incorrect") -> WALLET_PASSWORD_INCORRECT
        errorMessage.contains("wallet has already been unlocked") -> WALLET_ALREADY_UNLOCKED
        errorMessage.contains("could not read 'amount'") -> INVALID_AMOUNT
        errorMessage.contains("a password must be provided to newpassword") -> NO_NEW_PASSWORD
        errorMessage.contains("could not read address") -> COULD_NOT_READ_ADDRESS
        errorMessage.contains("transaction cannot have an output or payout that has zero value") -> AMOUNT_ZERO
        errorMessage.contains("unable to fund transaction") -> INSUFFICIENT_FUNDS
        errorMessage.contains("wallet is already encrypted, cannot encrypt again") -> EXISTING_WALLET
        errorMessage.contains("another wallet rescan is already underway") -> WALLET_SCAN_IN_PROGRESS
        errorMessage.contains("wallet has not been encrypted yet") -> WALLET_NOT_ENCRYPTED
        errorMessage.contains("cannot init from seed until blockchain is synced") -> CANNOT_INIT_FROM_SEED_UNTIL_SYNCED
        errorMessage.contains("cannot sweep until blockchain is synced") -> CANNOT_SWEEP_UNTIL_SYNCED
        errorMessage.contains("nothing to sweep") -> NOTHING_TO_SWEEP
        errorMessage.contains("word not found in dictionary for given language") -> INVALID_WORD_IN_SEED
        errorMessage.contains("seed failed checksum verification") -> INVALID_SEED
        /* explorer */
        errorMessage.contains("unrecognized hash used as input to /explorer/hash") -> UNRECOGNIZED_HASH
        /* renter */

        else -> {
            println("unaccounted for error message: $errorMessage")
            UNACCOUNTED_FOR_ERROR
        }
    }

    private fun getReasonFromThrowable(t: Throwable): Reason {
        t.printStackTrace()
        return when (t) {
            /* HTTPException is emitted by retrofit observables on HTTP non-2XX responses */
            is HttpException -> getReasonFromMsg(t.response().errorBody()!!.string())
            is EmptyResultSetException -> ROOM_EMPTY_RESULT_SET
            is SQLiteConstraintException -> DIRECTORY_ALREADY_EXISTS
            is SocketTimeoutException -> TIMEOUT
            is ConnectException -> COULDNT_CONNECT
            is SocketException -> NO_NETWORK_RESPONSE
            is UnknownHostException -> UNKNOWN_HOST_NAME
            is IOException -> UNEXPECTED_END_OF_STREAM
            else -> {
                println("unaccounted for throwable: $t")
                UNACCOUNTED_FOR_ERROR
            }
        }
    }

    // TODO: maybe it would be better if I had a separate throwable class for each error, instead of one throwable (SiaError)
    // that holds an enum containing the Reason for it. And then I'd have a static method that maps a more generic throwable
    // to one of the more specific ones
    enum class Reason(val msg: String) {
        /* common */
        SIAD_LOADING("Sia is still loading"),
        TIMEOUT("Response timed out"),
        COULDNT_CONNECT("Couldn't connect"),
        NO_NETWORK_RESPONSE("No network response"),
        /** seems to occur when attempting a network request to the outside world with wifi and data turned off? */
        UNKNOWN_HOST_NAME("Unknown hostname"),
        INCORRECT_API_PASSWORD("Incorrect API password"),
        ROOM_EMPTY_RESULT_SET("Nothing to display"),
        SIA_FOUR_OH_FOUR("Sia 404 - is the necessary module running?"),
        /**  occurs if an exception is thrown when parsing the network request (often due to Jackson, print the stack trace to see the problem) */
        UNEXPECTED_END_OF_STREAM("Unexpected end of stream"),
        UNACCOUNTED_FOR_ERROR("Unexpected error"),
        /* wallet */
        WALLET_PASSWORD_INCORRECT("Wallet password incorrect"),
        WALLET_LOCKED("Please unlock the wallet first"),
        WALLET_ALREADY_UNLOCKED("Wallet already unlocked"),
        INVALID_AMOUNT("Invalid amount"),
        NO_NEW_PASSWORD("Please provide a new password"),
        COULD_NOT_READ_ADDRESS("Invalid address"),
        AMOUNT_ZERO("Amount cannot be zero"),
        INSUFFICIENT_FUNDS("Insufficient funds"),
        EXISTING_WALLET("A wallet already exists. Use force option to overwrite"),
        WALLET_SCAN_IN_PROGRESS("Scanning the blockchain. Please wait, this can take a while"),
        WALLET_NOT_ENCRYPTED("Wallet has not been created yet"),
        INVALID_WORD_IN_SEED("Invalid word in seed"),
        INVALID_SEED("Invalid seed"),
        CANNOT_INIT_FROM_SEED_UNTIL_SYNCED("Cannot create wallet from seed until fully synced"),
        CANNOT_SWEEP_UNTIL_SYNCED("Cannot sweep until fully synced"),
        NOTHING_TO_SWEEP("Seed doesn't have anything sweep"),
        /* renter */
        DIRECTORY_ALREADY_EXISTS("Directory already exists"),
        /* explorer */
        UNRECOGNIZED_HASH("Unrecognized hash"),
    }

    fun snackbar(view: View, length: Int = Snackbar.LENGTH_SHORT) {
        // TODO: this better
//        if (reason == NO_NETWORK_RESPONSE) {
//            if (isSiadProcessStarting.value == false) {
//                val snackbar = SnackbarUtil.buildSnackbar(view, reason.msg)
//                snackbar.setAction("Start") {
//                    snackbar.context.sendBroadcast(Intent(SiadReceiver.START_SIAD))
//                }
//                snackbar.setActionTextColor(ContextCompat.getColor(snackbar.context, android.R.color.white))
//                snackbar.show()
//            } else {
//                return
//            }
//        } else {
            SnackbarUtil.showSnackbar(view, reason.msg, length)
//        }
    }
}