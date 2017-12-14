/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.data.remote

import android.support.design.widget.Snackbar
import android.view.View
import retrofit2.HttpException
import vandyke.siamobile.util.SnackbarUtil
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException

class SiaError : Throwable {
    val reason: Reason
    val msg
        get() = reason.msg

    constructor(errorMessage: String) {
        reason = getReasonFromMsg(errorMessage)
    }

    constructor(t: Throwable) {
        reason = getReasonFromThrowable(t)
    }

    constructor(reason: Reason) {
        this.reason = reason
    }

    private fun getReasonFromMsg(errorMessage: String): SiaError.Reason {
        return when {
            /* common */
            errorMessage.contains("siad is not ready") -> Reason.SIAD_LOADING
            errorMessage.contains("API authentication failed") -> Reason.INCORRECT_API_PASSWORD
            /* wallet */
            errorMessage.contains("wallet must be unlocked before it can be used") -> Reason.WALLET_LOCKED
            errorMessage.contains("provided encryption key is incorrect") -> Reason.WALLET_PASSWORD_INCORRECT
            errorMessage.contains("wallet has already been unlocked") -> Reason.WALLET_ALREADY_UNLOCKED
            errorMessage.contains("could not read 'amount'") -> Reason.INVALID_AMOUNT
            errorMessage.contains("a password must be provided to newpassword") -> Reason.NO_NEW_PASSWORD
            errorMessage.contains("could not read address") -> Reason.COULD_NOT_READ_ADDRESS
            errorMessage.contains("transaction cannot have an output or payout that has zero value") -> Reason.AMOUNT_ZERO
            errorMessage.contains("unable to fund transaction") -> Reason.INSUFFICIENT_FUNDS
            errorMessage.contains("wallet is already encrypted, cannot encrypt again") -> Reason.EXISTING_WALLET
            errorMessage.contains("another wallet rescan is already underway") -> Reason.WALLET_SCAN_IN_PROGRESS
            errorMessage.contains("wallet has not been encrypted yet") -> Reason.WALLET_NOT_ENCRYPTED
            errorMessage.contains("cannot init from seed until blockchain is synced") -> Reason.CANNOT_INIT_FROM_SEED_UNTIL_SYNCED
            errorMessage.contains("word not found in dictionary for given language") -> Reason.INVALID_WORD_IN_SEED
            errorMessage.contains("seed failed checksum verification") -> Reason.INVALID_SEED
            /* explorer */
            errorMessage.contains("unrecognized hash used as input to /explorer/hash") -> Reason.UNRECOGNIZED_HASH
            errorMessage.contains("Cloudflare") -> Reason.RATE_LIMITING
            /* renter errors */

            else -> {
                println("unaccounted for error message: $errorMessage")
                Reason.UNACCOUNTED_FOR_ERROR
            }
        }
    }

    private fun getReasonFromThrowable(t: Throwable): Reason {
        return when (t) {
            is HttpException -> getReasonFromMsg(t.response().errorBody()!!.string())
            is SocketTimeoutException -> Reason.TIMEOUT
            is SocketException -> Reason.NO_NETWORK_RESPONSE
            is IOException -> Reason.UNEXPECTED_END_OF_STREAM
            else -> {
                println("unaccounted for throwable: $t")
                Reason.UNACCOUNTED_FOR_ERROR
            }
        }
    }

    // TODO: translate the msgs
    enum class Reason(val msg: String) {
        /* common */
        SIAD_LOADING("Sia is still loading"),
        TIMEOUT("Response timed out"),
        NO_NETWORK_RESPONSE("No network response"),
        INCORRECT_API_PASSWORD("Incorrect API password"),
        /* wallet */
        WALLET_PASSWORD_INCORRECT("Wallet password incorrect"),
        WALLET_LOCKED("Wallet must be unlocked first"),
        WALLET_ALREADY_UNLOCKED("Wallet already unlocked"),
        INVALID_AMOUNT("Invalid amount"),
        NO_NEW_PASSWORD("Must provide new password"),
        COULD_NOT_READ_ADDRESS("Could not read address"),
        AMOUNT_ZERO("Amount cannot be zero"),
        INSUFFICIENT_FUNDS("Insufficient funds"),
        EXISTING_WALLET("A wallet already exists. Use force option to overwrite"),
        UNACCOUNTED_FOR_ERROR("Unexpected error"),
        WALLET_SCAN_IN_PROGRESS("Scanning the blockchain. Please wait, this can take a while"),
        WALLET_NOT_ENCRYPTED("Wallet has not been created yet"),
        INVALID_WORD_IN_SEED("Invalid word in seed"),
        INVALID_SEED("Invalid seed"),
        CANNOT_INIT_FROM_SEED_UNTIL_SYNCED("Cannot create wallet from seed until fully synced"),
        UNEXPECTED_END_OF_STREAM("Connection unexpectedly closed"), // This occurs every time if both data and wifi are turned off
        /* renter */
        DIRECTORY_ALREADY_EXISTS("Directory already exists"),
        /* explorer */
        UNRECOGNIZED_HASH("Unrecognized hash"),
        RATE_LIMITING("Hit request rate-limit")
    }

    fun snackbar(view: View?, length: Int = Snackbar.LENGTH_SHORT) {
        if (view == null)
            return
        SnackbarUtil.snackbar(view, reason.msg, length)
    }
}