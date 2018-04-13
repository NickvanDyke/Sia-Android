/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.remote

import android.util.Log
import com.vandyke.sia.data.models.renter.name
import okhttp3.Request
import org.json.JSONObject

/** An exception resulting from the Sia node returning some HTTP error response code and message */
sealed class SiaException(msg: String) : Throwable(msg) {
    companion object {
        /** Given the body of a response, converts it to JSON and looks for the "message" key that
         *  Sia API error responses have. If it's not found, null is returned. Otherwise, the
         *  exception that's appropriate for the message is returned */
        fun fromError(request: Request, body: String): SiaException? {
            val errJson = JSONObject(body)
            val msg = errJson.getString("message") ?: return null
            return when {
                msg.contains("siad is not ready") -> SiadNotReady()
                msg.contains("API authentication failed") -> APIAuthFailed()
                msg.contains("404 - Refer to API.md") -> ModuleNotEnabled(request.url().pathSegments()[0])

                msg.contains("wallet has already been unlocked") -> WalletAlreadyUnlocked()
                msg.contains("wallet must be unlocked before it can be used") -> WalletLocked()
                msg.contains("provided encryption key is incorrect") -> WalletPasswordIncorrect()
                msg.contains("could not read amount") -> CouldNotReadAmount()
                msg.contains("a password must be provided to newpassword") -> NewPasswordRequired()
                msg.contains("could not read address") -> CouldNotReadAddress()
                msg.contains("transaction cannot have an output or payout that has zero value") -> ZeroAmount()
                msg.contains("unable to fund transaction") -> InsufficientFunds()
                msg.contains("wallet is already encrypted, cannot encrypt again") -> ExistingWallet()
                msg.contains("another wallet rescan is already underway") -> ScanInProgress()
                msg.contains("wallet has not been encrypted yet") -> NoWallet()
                msg.contains("cannot init from seed until blockchain is synced") -> CannotInitFromSeedUntilSynced()
                msg.contains("cannot sweep until blockchain is synced") -> CannotSweepUntilSynced()
                msg.contains("nothing to sweep") -> NothingToSweep()
                msg.contains("word not found in dictionary for given language") -> WordNotFoundInDictionary()
                msg.contains("seed failed checksum verification") -> SeedFailedChecksum()

                msg.contains("download failed: no file with that path") -> NoFileWithThatPath(msg.substring(42))
                msg.contains("upload failed: a file already exists at that location") -> FileAlreadyExists(request.url().pathSegments()[2].name())

                msg.contains("unrecognized hash used as input to /explorer/hash") -> ExplorerUnrecognizedHash()

                else -> UncaughtSiaError(msg)
            }
        }
    }
}

// TODO: maybe I should begin all these class names with 'Sia' to make it more clear they're related to this
class SiadNotReady : SiaException("Sia is still loading")
class SiadNotRunning : SiaException("Sia node isn't running")
class APIAuthFailed : SiaException("Incorrect API password")
class ModuleNotEnabled(val module: String) : SiaException("Required module isn't enabled: $module")

class WalletAlreadyUnlocked : SiaException("Wallet is already unlocked")
class WalletLocked : SiaException("Please unlock the wallet first")
class WalletPasswordIncorrect : SiaException("Incorrect wallet password")
class CouldNotReadAmount : SiaException("Invalid amount")
class NewPasswordRequired : SiaException("A new password is required")
class CouldNotReadAddress : SiaException("Invalid address")
class ZeroAmount : SiaException("Amount cannot be zero")
class InsufficientFunds : SiaException("Insufficient funds")
class ExistingWallet : SiaException("Wallet already exists - use force to overwrite")
class ScanInProgress : SiaException("Scan in progress - please wait...")
class NoWallet : SiaException("Please create a wallet first")
class CannotInitFromSeedUntilSynced : SiaException("Cannot create a wallet from an existing seed until synced")
class CannotSweepUntilSynced : SiaException("Cannot sweep until synced")
class NothingToSweep : SiaException("Seed has nothing to sweep")
class WordNotFoundInDictionary : SiaException("Word not found in dictionary for given language")
class SeedFailedChecksum : SiaException("Seed failed checksum verification - verify it's correct")

class NoFileWithThatPath(filepath: String) : SiaException("Download failed: no file with path $filepath")
class FileAlreadyExists(fileName: String) : SiaException("File named \"$fileName\" already exists here")

class ExplorerUnrecognizedHash : SiaException("Unrecognized hash")

class UncaughtSiaError(errMsg: String) : SiaException(errMsg) {
    init {
        Log.d("Uncaught Sia error", errMsg)
    }
}
