/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.data.remote

import com.vandyke.sia.data.models.consensus.ConsensusData
import com.vandyke.sia.data.models.gateway.GatewayData
import com.vandyke.sia.data.models.gateway.GatewayPeerData
import com.vandyke.sia.data.models.renter.*
import com.vandyke.sia.data.models.txpool.FeeData
import com.vandyke.sia.data.models.wallet.*
import com.vandyke.sia.util.HASTINGS_PER_SC
import com.vandyke.sia.util.UNCONFIRMED_TX_TIMESTAMP
import com.vandyke.sia.util.toHastings
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

/** This class attempts to simulate the API endpoints and internal behavior of the Sia node.
 * It's far from exact, but enough that it can usually be used as a replacement when testing. */
class MockSiaApi : SiaApi {
    // obviously using a nonce when setting up internal values won't give very reproducible tests. Should do some other way.
    // maybe throw in some real data or something
    // maybe have some factory functions that initialize to often-used values, like with a wallet already created
    private val counter = AtomicInteger()
    private val nonce
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
    var confirmedTxs: MutableList<TransactionData> = MutableList(20, { _ ->
        TransactionData(nonce.toString(), BigDecimal(nonce * 10), BigDecimal(nonce * 100), BigDecimal("12312").toHastings())
    })
    var unconfirmedTxs: MutableList<TransactionData> = MutableList(2, { _ ->
        TransactionData(nonce.toString(), BigDecimal(nonce * 10), BigDecimal(nonce * 100), BigDecimal("12").toHastings())

    })

//    private val files = mutableListOf(
//            SiaFile("legos/brick/picture.jpg", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("legos/brick/manual", "eh", 56743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("legos/brick/blueprint.b", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("legos/brick/draft.txt", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("legos/brick/ad.doc", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("legos/brick/writeup.txt", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("legos/brick/buyers.db", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("picture.jpg", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/brick/listing.html", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("manual", "eh", 56743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/brick/colors.rgb", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("blueprint.b", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/block/picture.jpg", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("draft.txt", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/block/blueprint", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("ad.doc", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/block/vector.svg", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("writeup.txt", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/block/colors.rgb", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("buyers.db", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("legos/blue/brick/picture.jpg", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534),
//            SiaFile("listing.html", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534), SiaFile("my/name/is/nick/and/this/is/my/story.txt", "eh", 156743, true, false, 2.0, 663453, 100f, 1235534)
//    )

    private val files = mutableListOf(
            SiaFile.withDefaults("Cat pics/Kittens/Oliver.png", redundancy = 2.5, uploadprogress = 76f, available = true, size = 130800308),
            SiaFile.withDefaults("Cat pics/Kittens/Chloe.jpg", redundancy = 0.3, uploadprogress = 10f, available = false, size = 15761397),
            SiaFile.withDefaults("Cat pics/Kittens/Smokey.pdf", redundancy = 3.2, uploadprogress = 100f, available = true, size = 267608),
            SiaFile.withDefaults("Cat pics/Kittens/Tiger.gif", redundancy = 0.9, uploadprogress = 27f, available = true, size = 695988),
            SiaFile.withDefaults("Cat pics/Standing up/Persian.png", redundancy = 3.0, uploadprogress = 100f, available = true, size = 154501069),
            SiaFile.withDefaults("Cat pics/Standing up/Siamese.png", redundancy = 3.4, uploadprogress = 100f, available = true, size = 146637271),
            SiaFile.withDefaults("Cat pics/Standing up/Tabby.jpg", redundancy = 2.3, uploadprogress = 63f, available = true, size = 25299502),
            SiaFile.withDefaults("Cat pics/Zeus.mp4", redundancy = 3.1, uploadprogress = 100f, available = true, size = 16217241),
            SiaFile.withDefaults("Cat pics/Dakota.png", redundancy = 3.3, uploadprogress = 100f, available = true, size = 593008),
            SiaFile.withDefaults("Cat pics/Catloaf.png", redundancy = 1.3, uploadprogress = 41f, available = true, size = 267608),
            SiaFile.withDefaults("Cat pics/Rolling.gif", redundancy = 2.4, uploadprogress = 69f, available = true, size = 128942330),
            SiaFile.withDefaults("Cat pics/Blep.jpg", redundancy = 3.0, uploadprogress = 100f, available = true, size = 517599),
            SiaFile.withDefaults("Cat pics/With yarn.png", redundancy = 0.7, uploadprogress = 21f, available = false, size = 478644)
    )

    private var renterData = RenterData(
            RenterSettingsData(RenterSettingsAllowanceData(
                    BigDecimal("3629").toHastings(),
                    50,
                    12000,
                    4000
            )),
            RenterFinancialMetricsData(
                    System.currentTimeMillis(),
                    BigDecimal("167").toHastings(),
                    BigDecimal("154").toHastings(),
                    BigDecimal("690").toHastings(),
                    BigDecimal("274").toHastings(),
                    BigDecimal("1085").toHastings()
            ),
            139000
    )

    private val contracts = listOf(
            ContractData("dkvjsadkljfoweinfskjdnf", BigDecimal("143").toHastings(), BigDecimal("100").toHastings(), BigDecimal("156").toHastings(), BigDecimal("463").toHastings(),
                    BigDecimal("56").toHastings(), BigDecimal("673").toHastings(), 130000, 200000,
                    "56.127.97.231", BigDecimal("6324677445"), true, true)
    )

    override fun daemonStop(): Completable {
        TODO("not implemented")
    }

    override fun wallet(): Single<WalletData> {
        return Single.just(
                WalletData(System.currentTimeMillis(), encrypted, unlocked, rescanning, confirmedSiacoinBalance, unconfirmedOutgoingSiacoins,
                        unconfirmedIncomingSiacoins, siafundBalance, siacoinClaimBalance, dustThreshold))
    }

    override fun walletSiacoins(amount: String, destination: String): Completable {
        return Completable.fromAction {
            checkEncrypted()
            checkUnlocked()
            if (amount.toBigDecimal() == BigDecimal.ZERO)
                throw ZeroAmount()
            if (destination.isBlank())
                throw CouldNotReadAddress()
            unconfirmedTxs.add(TransactionData(nonce.toString(), BigDecimal(nonce),
                    UNCONFIRMED_TX_TIMESTAMP, amount.toBigDecimal()))
        }
    }

    override fun walletAddress(): Single<AddressData> {
        return Single.fromCallable {
            checkEncrypted()
            checkUnlocked()
            AddressData(addresses[0])
        }
    }

    override fun walletAddresses(): Single<AddressesData> {
        return Single.fromCallable {
            checkEncrypted()
            checkUnlocked()
            AddressesData(addresses)
        }
    }

    override fun walletSeeds(dictionary: String): Single<SeedsData> {
        return Single.fromCallable {
            checkEncrypted()
            checkUnlocked()
            SeedsData(seed, 100, listOf(seed))
        }
    }

    override fun walletSweepSeed(dictionary: String, seed: String): Completable {
        return Completable.error(NotImplementedError())
    }

    override fun walletTransactions(startHeight: Int, endHeight: Int): Single<TransactionsData> {
        return Single.just(when (encrypted) {
            false -> TransactionsData(null, null)
            else -> TransactionsData(confirmedTxs, unconfirmedTxs)
        })
    }

    override fun walletInit(password: String, dictionary: String, force: Boolean): Single<WalletInitData> {
        return Single.fromCallable {
            if (!force && encrypted)
                throw ExistingWallet()
            this.password = password
            this.seed = "random testing seed"
            encrypted = true
            WalletInitData(this.seed)
        }
    }

    override fun walletInitSeed(password: String, dictionary: String, seed: String, force: Boolean): Completable {
        return Completable.fromAction {
            if (!force && encrypted)
                throw ExistingWallet()
            this.password = password
            this.seed = seed
            encrypted = true
        }
    }

    override fun walletLock(): Completable {
        return Completable.fromAction {
            checkEncrypted()
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
        return Single.just(ScValueData(System.currentTimeMillis(),
                BigDecimal("0.01"), BigDecimal("0.02"), BigDecimal("0.03"),
                BigDecimal("0.04"), BigDecimal("0.05"), BigDecimal("0.06"),
                BigDecimal("0.07"), BigDecimal("0.08"), BigDecimal("0.09"),
                BigDecimal("0.10")
        ))
    }

    override fun renter(): Single<RenterData> {
        return Single.just(renterData)
    }

    override fun renter(funds: BigDecimal, hosts: Int, period: Int, renewwindow: Int): Completable {
        return Completable.fromAction {
            renterData = renterData.copy(
                    settings = renterData.settings.copy(
                            allowance = RenterSettingsAllowanceData(funds, hosts, period, renewwindow)))
        }
    }

    override fun renterContracts(): Single<ContractsData> {
        return Single.just(ContractsData(contracts))
    }

    override fun renterDownloads(): Single<DownloadsData> {
        return Single.error(NotImplementedError())
    }

    override fun renterFiles(): Single<RenterFilesData> {
        return Single.just(RenterFilesData(files))
    }

    override fun renterPrices(): Single<PricesData> {
        return Single.just(PricesData(
                System.currentTimeMillis(),
                BigDecimal("26").toHastings(),
                BigDecimal("100").toHastings(),
                BigDecimal("200").toHastings(),
                BigDecimal("75").toHastings()))
    }

    override fun renterRename(siapath: String, newSiaPath: String) = Completable.fromAction {
        val index = files.indexOfFirst { it.path == siapath }
        if (index != -1)
            files[index] = files[index].copy(path = newSiaPath)
    }!!

    override fun renterDelete(siapath: String): Completable {
        return Completable.fromAction {
            var removed: SiaFile? = null
            files.forEach {
                if (it.path == siapath) {
                    removed = it
                    return@forEach
                }
            }
            removed?.let { files.remove(it) }
        }
    }

    override fun renterUpload(siapath: String, source: String, dataPieces: Int, parityPieces: Int): Completable {
        return Completable.fromAction {
            files.add(SiaFile(siapath, source, 156743, true, false,
                    2.0, 663453, 100f, 1235534))
        }
    }

    override fun renterDownload(siapath: String, destination: String): Completable {
        return Completable.fromAction { Thread.sleep(1000) }
    }

    override fun renterDownloadAsync(siapath: String, destination: String): Completable {
        return Completable.complete()
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
        return Single.just(ConsensusData(System.currentTimeMillis(), false, 140000, nonce.toString(), BigDecimal(nonce)))
    }

    override fun txPoolFee(): Single<FeeData> {
        TODO("not implemented")
    }

    private fun checkPassword(password: String) {
        if (password != this.password)
            throw WalletPasswordIncorrect()
    }

    private fun checkUnlocked(desired: Boolean = true) {
        if (unlocked != desired) {
            if (unlocked)
                throw WalletAlreadyUnlocked()
            else
                throw WalletLocked()
        }
    }

    private fun checkEncrypted(desired: Boolean = true) {
        if (encrypted != desired) {
            if (encrypted)
                throw ExistingWallet()
            else
                throw NoWallet()
        }
    }
}