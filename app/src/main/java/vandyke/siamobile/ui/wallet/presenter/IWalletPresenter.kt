/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.wallet.presenter

interface IWalletPresenter {
    fun refresh()
    fun unlock(password: String)
    fun lock()
    fun create(password: String, force: Boolean, seed: String? = null)
    fun send(amount: String, destination: String)
    fun sweep(seed: String)
    fun changePassword(currentPassword: String, newPassword: String)
}