package vandyke.siamobile.backend.networking

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import vandyke.siamobile.R
import vandyke.siamobile.backend.coldstorage.ColdStorageHttpServer
import vandyke.siamobile.util.SnackbarUtil
import java.net.SocketException
import java.net.SocketTimeoutException

class SiaError {
    val reason: Reason

    constructor(errorMessage: String) {
        reason = getReasonFromMsg(errorMessage)
    }

    constructor(t: Throwable) {
        reason = getReasonFromThrowable(t)
    }

    fun getReasonFromMsg(errorMessage: String): SiaError.Reason {
        when {
            errorMessage.contains("wallet must be unlocked before it can be used") -> return SiaError.Reason.WALLET_LOCKED
            errorMessage.contains("provided encryption key is incorrect") -> return SiaError.Reason.WALLET_PASSWORD_INCORRECT
            errorMessage.contains("wallet has already been unlocked") -> return SiaError.Reason.WALLET_ALREADY_UNLOCKED
            errorMessage.contains("could not read 'amount'") -> return SiaError.Reason.INVALID_AMOUNT
            errorMessage.contains("a password must be provided to newpassword") -> return SiaError.Reason.NO_NEW_PASSWORD
            errorMessage.contains("could not read address") -> return SiaError.Reason.COULD_NOT_READ_ADDRESS
            errorMessage.contains("transaction cannot have an output or payout that has zero value") -> return SiaError.Reason.AMOUNT_ZERO
            errorMessage.contains("unable to fund transaction") -> return SiaError.Reason.INSUFFICIENT_FUNDS
            errorMessage.contains("wallet is already encrypted, cannot encrypt again") -> return SiaError.Reason.EXISTING_WALLET
            errorMessage.contains("API authentication failed") -> return SiaError.Reason.INCORRECT_API_PASSWORD
            errorMessage.contains("another wallet rescan is already underway") -> return SiaError.Reason.WALLET_SCAN_IN_PROGRESS
            errorMessage.contains("wallet has not been encrypted yet") -> return SiaError.Reason.WALLET_NOT_ENCRYPTED
            errorMessage.contains("cannot init from seed until blockchain is synced") -> return SiaError.Reason.CANNOT_INIT_FROM_SEED_UNTIL_SYNCED
            errorMessage.contains("unsupported on cold storage wallet") -> return SiaError.Reason.UNSUPPORTED_ON_COLD_WALLET
            errorMessage.contains("word not found in dictionary for given language") -> return SiaError.Reason.INVALID_WORD_IN_SEED
            else -> {
                println("unaccounted for error message: $errorMessage")
                return SiaError.Reason.UNACCOUNTED_FOR_ERROR
            }
        }
    }

    fun getReasonFromThrowable(t: Throwable): Reason {
        when (t) {
            is SocketTimeoutException -> return Reason.TIMEOUT
            is SocketException -> return Reason.NO_NETWORK_RESPONSE
            else -> {
                println("unaccounted for throwable: $t")
                return Reason.UNACCOUNTED_FOR_ERROR
            }
        }
    }

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

    fun snackbar(view: View?) {
        if (view == null)
            return
        if (reason == Reason.UNSUPPORTED_ON_COLD_WALLET) {
            val snackbar = Snackbar.make(view, reason.msg, Snackbar.LENGTH_LONG).setAction("Help") { v -> ColdStorageHttpServer.showColdStorageHelp(view.context) }
            snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorAccent))
            snackbar.setActionTextColor(ContextCompat.getColor(view.context, android.R.color.white))
            snackbar.show()
        } else
            SnackbarUtil.snackbar(view, reason.msg, Snackbar.LENGTH_SHORT)
    }
}