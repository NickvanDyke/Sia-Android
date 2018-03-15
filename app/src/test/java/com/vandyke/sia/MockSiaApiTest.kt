/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia

import com.vandyke.sia.data.remote.MockSiaApi
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeBlank
import org.junit.Test

/** Some basic tests for the class that mocks Sia's API endpoints and internal behavior */
class MockSiaApiTest {
    private val api: MockSiaApi = MockSiaApi()

    @Test
    fun create() {
        api.walletInit("password", "english", false).blockingGet()
        api.password shouldEqual "password"
        api.seed.shouldNotBeBlank()
        api.encrypted.shouldBeTrue()
        api.unlocked.shouldBeTrue()
    }

    @Test
    fun unlockAndLock() {
        api.encrypted = true
        api.unlocked = false
        api.password = "password"
        api.wallet().blockingGet().unlocked.shouldBeFalse()
        api.walletUnlock("password").blockingAwait()
        api.unlocked.shouldBeTrue()
        api.wallet().blockingGet().unlocked.shouldBeTrue()
        api.walletLock().blockingAwait()
        api.unlocked.shouldBeTrue()
    }
}