/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Prefs(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var theme: String
        get() = prefs.getString("theme", "light")
        set(value) = prefs.edit().putString("theme", value).apply()

    var operationMode: String
        get() = prefs.getString("operationMode", "cold_storage")
        set(value) = prefs.edit().putString("operationMode", value).apply()

    var address: String
        get() = prefs.getString("address", "localhost:9990")
        set(value) = prefs.edit().putString("address", value).apply()

    var remoteAddress: String
        get() = prefs.getString("remoteAddress", "192.168.1.100:9980")
        set(value) = prefs.edit().putString("remoteAddress", value).apply()

    var refreshInterval: Int
        get() = prefs.getString("refreshInterval", "1").toInt()
        set(value) = prefs.edit().putString("refreshInterval", value.toString()).apply()

    var runLocalNodeOffWifi: Boolean
        get() = prefs.getBoolean("runLocalNodeOffWifi", false)
        set(value) = prefs.edit().putBoolean("runLocalNodeOffWifi", value).apply()

    var localNodeMinBattery: Int
        get() = prefs.getString("localNodeMinBattery", "20").toInt()
        set(value) = prefs.edit().putString("localNodeMinBattery", value.toString()).apply()

    var runInBackground: Boolean
        get() = prefs.getBoolean("runInBackground", false)
        set(value) = prefs.edit().putBoolean("runInBackground", value).apply()

    var firstTime: Boolean
        get() = prefs.getBoolean("firstTime", true)
        set(value) = prefs.edit().putBoolean("firstTime", value).apply()

    var startupPage: String
        get() = prefs.getString("startupPage", "walletModel")
        set(value) = prefs.edit().putString("startupPage", value).apply()

    var transparentBars: Boolean
        get() = prefs.getBoolean("transparentBars", false)
        set(value) = prefs.edit().putBoolean("transparentBars", value).apply()

    var customBgBase64: String
        get() = prefs.getString("customBgBase64", "")
        set(value) = prefs.edit().putString("customBgBase64", value).apply()

    var hideZero: Boolean
        get() = prefs.getBoolean("hideZero", false)
        set(value) = prefs.edit().putBoolean("hideZero", value).apply()

    var useExternal: Boolean
        get() = prefs.getBoolean("useExternal", false)
        set(value) = prefs.edit().putBoolean("useExternal", value).apply()

    var feesEnabled: Boolean
        get() = prefs.getBoolean("feesEnabled", false)
        set(value) = prefs.edit().putBoolean("feesEnabled", value).apply()

    var apiPass: String
        get() = prefs.getString("apiPass", "")
        set(value) = prefs.edit().putString("apiPass", value).apply()

    var mostRecentTxId: String
        get() = prefs.getString("mostRecentTxId", "")
        set(value) = prefs.edit().putString("mostRecentTxId", value).apply()

    var displayedDecimalPrecision: Int
        get() = prefs.getString("displayedDecimalPrecision", "2").toInt()
        set(value) = prefs.edit().putString("displayedDecimalPrecision", value.toString()).apply()

    var coldStorageExists: Boolean
        get() = prefs.getBoolean("coldStorageExists", false)
        set(value) = prefs.edit().putBoolean("coldStorageExists", value).apply()

    var coldStorageSeed: String
        get() = prefs.getString("coldStorageSeed", "")
        set(value) = prefs.edit().putString("coldStorageSeed", value).apply()

    var coldStoragePassword: String
        get() = prefs.getString("coldStoragePassword", "")
        set(value) = prefs.edit().putString("coldStoragePassword", value).apply()

    var coldStorageAddresses: Set<String>
        get() = prefs.getStringSet("coldStorageAddresses", HashSet())
        set(value) = prefs.edit().putStringSet("coldStorageAddresses", value).apply()

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
}