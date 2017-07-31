package vandyke.siamobile.api.networking

import retrofit2.Response

class SiaError<T>(response: Response<T>) {
    val reason: Reason

    init {
        if (!response.isSuccessful)
            reason = determineReason(response.errorBody().toString())
    }


    fun determineReason(errorMessage: String): SiaError.Reason {
        if (errorMessage.contains("walletModel must be unlocked before it can be used"))
            return SiaError.Reason.WALLET_LOCKED
        else if (errorMessage.contains("provided encryption key is incorrect"))
            return SiaError.Reason.WALLET_PASSWORD_INCORRECT
        else if (errorMessage.contains("walletModel has already been unlocked"))
            return SiaError.Reason.WALLET_ALREADY_UNLOCKED
        else if (errorMessage.contains("could not read 'amount'"))
            return SiaError.Reason.INVALID_AMOUNT
        else if (errorMessage.contains("a password must be provided to newpassword"))
            return SiaError.Reason.NO_NEW_PASSWORD
        else if (errorMessage.contains("could not read address"))
            return SiaError.Reason.COULD_NOT_READ_ADDRESS
        else if (errorMessage.contains("transaction cannot have an output or payout that has zero value"))
            return SiaError.Reason.AMOUNT_ZERO
        else if (errorMessage.contains("unable to fund transaction"))
            return SiaError.Reason.INSUFFICIENT_FUNDS
        else if (errorMessage.contains("walletModel is already encrypted, cannot encrypt again"))
            return SiaError.Reason.EXISTING_WALLET
        else if (errorMessage.contains("API authentication failed"))
            return SiaError.Reason.INCORRECT_API_PASSWORD
        else if (errorMessage.contains("another walletModel rescan is already underway"))
            return SiaError.Reason.WALLET_SCAN_IN_PROGRESS
        else if (errorMessage.contains("walletModel has not been encrypted yet"))
            return SiaError.Reason.WALLET_NOT_ENCRYPTED
        else if (errorMessage.contains("cannot init from seed until blockchain is synced"))
            return SiaError.Reason.CANNOT_INIT_FROM_SEED_UNTIL_SYNCED
        else if (errorMessage.contains("unsupported on cold storage walletModel"))
            return SiaError.Reason.UNSUPPORTED_ON_COLD_WALLET
        else if (errorMessage.contains("word not found in dictionary for given language"))
            return SiaError.Reason.INVALID_WORD_IN_SEED
        else {
            println(errorMessage)
            return SiaError.Reason.UNACCOUNTED_FOR_ERROR
        }
    }

    enum class Reason(val msg: String) {
        TIMEOUT("Response timed out"),
        NO_NETWORK_RESPONSE("No network response"),
        WALLET_PASSWORD_INCORRECT("WalletApiJava password incorrect"),
        WALLET_LOCKED("WalletApiJava must be unlocked first"),
        WALLET_ALREADY_UNLOCKED("WalletApiJava already unlocked"),
        INVALID_AMOUNT("Invalid amount"),
        NO_NEW_PASSWORD("Must provide new password"),
        COULD_NOT_READ_ADDRESS("Could not read address"),
        AMOUNT_ZERO("Amount cannot be zero"),
        INSUFFICIENT_FUNDS("Insufficient funds"),
        EXISTING_WALLET("A walletModel already exists. Use force option to overwrite"),
        INCORRECT_API_PASSWORD("Incorrect API password"),
        UNACCOUNTED_FOR_ERROR("Unexpected error"),
        WALLET_SCAN_IN_PROGRESS("WalletApiJava scan in progress. Please wait"),
        WALLET_NOT_ENCRYPTED("WalletApiJava has not been created yet"),
        INVALID_WORD_IN_SEED("Invalid word in seed"),
        CANNOT_INIT_FROM_SEED_UNTIL_SYNCED("Cannot create walletModel from seed until blockchain is synced"),
        UNSUPPORTED_ON_COLD_WALLET("Unsupported on cold storage walletModel")
    }
}