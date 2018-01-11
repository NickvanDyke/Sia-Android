/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.remote

import com.vandyke.sia.data.SiaError
import com.vandyke.sia.data.SiaError.Reason.*
import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.gateway.GatewayData
import com.vandyke.sia.data.models.gateway.GatewayPeerData
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.models.txpool.FeeData
import com.vandyke.sia.data.models.wallet.*
import com.vandyke.sia.util.HASTINGS_PER_SC
import com.vandyke.sia.util.UNCONFIRMED_TX_TIMESTAMP
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

/** This class attempts to simulate the API endpoints and internal behavior of the Sia node.
 * It's far from exact, but enough that it can be used as a replacement when testing. */
class MockSiaApi : SiaApiInterface {
    // obviously using a nonce when setting up internal values won't give very reproducible tests. Should do some other way.
    // maybe throw in some real data or something
    // maybe have some factory functions that initialize to often-used values, like with a wallet already created
    val counter = AtomicInteger()
    val nonce
        get() = counter.getAndIncrement()
    /* the fields are left public so that they can be modified for testing particular things if needed */
    var password = ""
    /* wallet stuff */
    var unlocked = false
    var encrypted = false
    var rescanning = false
    var confirmedSiacoinBalance = BigDecimal("100") * HASTINGS_PER_SC
    var unconfirmedIncomingSiacoins = BigDecimal("20") * HASTINGS_PER_SC // should use txs to calculate these SC values
    var unconfirmedOutgoingSiacoins = BigDecimal("5") * HASTINGS_PER_SC
    var dustThreshold = BigDecimal("100")
    var siacoinClaimBalance = BigDecimal.ZERO
    var siafundBalance = 0
    var seed = ""
    var addresses = listOf("address1", "address2", "address3")
    var confirmedTxs: MutableList<TransactionData> = MutableList(7, { index ->
        val inputs = listOf(TransactionInputData(walletaddress = nonce % 2 == 0, value = BigDecimal(nonce * 2) * HASTINGS_PER_SC))
        val outputs = listOf(TransactionOutputData(walletaddress = nonce % 2 == 1, value = BigDecimal(nonce) * HASTINGS_PER_SC))
        TransactionData(nonce.toString(), nonce * 10, BigDecimal(nonce * 100), inputs, outputs)
    })
    var unconfirmedTxs: MutableList<TransactionData> = MutableList(2, { index ->
        val inputs = listOf(TransactionInputData(walletaddress = nonce % 2 == 1, value = BigDecimal(nonce) * HASTINGS_PER_SC))
        val outputs = listOf(TransactionOutputData(walletaddress = nonce % 2 == 0, value = BigDecimal(nonce * 3) * HASTINGS_PER_SC))
        TransactionData(nonce.toString(), nonce * 10, UNCONFIRMED_TX_TIMESTAMP, inputs, outputs)
    })


    override fun daemonStop(): Completable {
        TODO("not implemented")
    }

    override fun wallet(): Single<WalletData> {
        return Single.fromCallable {
            WalletData(encrypted, unlocked, rescanning, confirmedSiacoinBalance, unconfirmedOutgoingSiacoins,
                    unconfirmedIncomingSiacoins, siafundBalance, siacoinClaimBalance, dustThreshold)
        }
    }

    override fun walletSiacoins(amount: String, destination: String): Completable {
        return Completable.fromAction {
            val input = TransactionInputData(walletaddress = true, value = BigDecimal(amount))
            val output = TransactionOutputData(walletaddress = false, value = BigDecimal(amount))
            unconfirmedTxs.add(TransactionData(nonce.toString(), nonce,
                    UNCONFIRMED_TX_TIMESTAMP, listOf(input), listOf(output)))
        }
    }

    override fun walletAddress(): Single<AddressData> {
        return Single.fromCallable {
            checkUnlocked()
            AddressData(addresses[0])
        }
    }

    override fun walletAddresses(): Single<AddressesData> {
        return Single.fromCallable {
            AddressesData(addresses)
        }
    }

    override fun walletSeeds(dictionary: String): Single<SeedsData> {
        return Single.fromCallable {
            checkUnlocked()
            SeedsData(seed)
        }
    }

    override fun walletSweepSeed(dictionary: String, seed: String): Completable {
        TODO("not implemented")
    }

    override fun walletTransactions(startHeight: String, endHeight: String): Single<TransactionsData> {
        return Single.fromCallable {
            if (!encrypted)
                TransactionsData()
            else
                TransactionsData(confirmedTxs, unconfirmedTxs)
        }
    }

    override fun walletInit(password: String, dictionary: String, force: Boolean): Single<WalletInitData> {
        return Single.fromCallable {
            if (!force && encrypted)
                throw SiaError(EXISTING_WALLET)
            this.password = password
            this.seed = "random testing seed"
            encrypted = true
            WalletInitData(this.seed)
        }
    }

    override fun walletInitSeed(password: String, dictionary: String, seed: String, force: Boolean): Completable {
        return Completable.fromAction {
            if (!force && encrypted)
                throw SiaError(EXISTING_WALLET)
            this.password = password
            this.seed = seed
            encrypted = true
        }
    }

    override fun walletLock(): Completable {
        return Completable.fromAction {
            checkUnlocked()
            unlocked = false
        }
    }

    override fun walletUnlock(password: String): Completable {
        return Completable.fromAction {
            checkEncrypted()
            checkUnlocked(false)
            checkPassword(password)
            unlocked = true
        }
    }

    override fun walletChangePassword(password: String, newPassword: String): Completable {
        return Completable.fromAction {
            checkPassword(password)
            this.password = newPassword
        }
    }

    override fun getScPrice(url: String): Single<ScValueData> {
        return Single.just(ScValueData(BigDecimal("0.07")))
    }

    override fun renter(): Single<RenterData> {
        TODO("not implemented")
    }

    override fun renter(funds: BigDecimal, hosts: Int, period: Int, renewwindow: Int): Completable {
        TODO("not implemented")
    }

    override fun renterContracts(): Single<ContractsData> {
        TODO("not implemented")
    }

    override fun renterDownloads(): Single<DownloadsData> {
        TODO("not implemented")
    }

    override fun renterFiles(): Single<RenterFilesData> {
        TODO("not implemented")
    }

    override fun renterPrices(): Single<PricesData> {
        TODO("not implemented")
    }

    override fun renterRename(siapath: String, newSiaPath: String): Completable {
        TODO("not implemented")
    }

    override fun renterDelete(siapath: String): Completable {
        TODO("not implemented")
    }

    override fun renterUpload(siapath: String, source: String, dataPieces: Int, parityPieces: Int): Completable {
        TODO("not implemented")
    }

    override fun renterDownload(siapath: String, destination: String): Completable {
        TODO("not implemented")
    }

    override fun renterDownloadAsync(siapath: String, destination: String): Completable {
        TODO("not implemented")
    }

    override fun gateway(): Single<GatewayData> {
        return Single.just(GatewayData("536.623.53.8", listOf(
                GatewayPeerData("68.12.543.30", "1.3.1", true),
                GatewayPeerData("20.64.77.12", "1.3.0", true),
                GatewayPeerData("73.74.12.98", "1.6.1", true),
                GatewayPeerData("90.123.74.23", "1.3.1", true)
        )))
    }

    override fun consensus(): Single<ConsensusData> {
        return Single.just(ConsensusData(false, 135371, nonce.toString(), BigDecimal(nonce)))
    }

    override fun txPoolFee(): Single<FeeData> {
        TODO("not implemented")
    }

    fun checkPassword(password: String) {
        if (password != this.password)
            throw SiaError(WALLET_PASSWORD_INCORRECT)
    }

    fun checkUnlocked(desired: Boolean = true) {
        if (unlocked != desired)
            throw SiaError(WALLET_LOCKED)
    }

    fun checkEncrypted(desired: Boolean = true) {
        if (encrypted != desired)
            throw SiaError(WALLET_NOT_ENCRYPTED)
    }
}