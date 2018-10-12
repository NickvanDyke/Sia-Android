package com.vandyke.sia.ui.exchange

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.jakewharton.rxbinding2.widget.itemSelections
import com.jakewharton.rxbinding2.widget.textChanges
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.format
import com.vandyke.sia.util.getColorRes
import kotlinx.android.synthetic.main.activity_exchange.*
import javax.inject.Inject

class ExchangeActivity : MviActivity<ExchangeView, ExchangePresenter>(), ExchangeView {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    @Inject
    lateinit var presenter: ExchangePresenter

    private lateinit var coinsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent().inject(this)
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(when {
            Prefs.darkMode -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        })
        setTheme(when {
            Prefs.oldSiaColors -> R.style.AppTheme_DayNight_OldSiaColors
            else -> R.style.AppTheme_DayNight
        })
        setContentView(R.layout.activity_exchange)

        setSupportActionBar(toolbar)
        supportActionBar!!.run {
            title = "Exchange"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        coinsAdapter = ArrayAdapter(this, R.layout.spinner_selected_item_white)
        coinsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        from_spinner.run {
            adapter = coinsAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }
        to_spinner.run {
            adapter = coinsAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun createPresenter() = presenter

    override fun fromAmount() = from_amount.textChanges()
            .filter { from_amount.isFocused } /* so that it only triggers the intent when the user is manually changing it */
            .map { it.toString().toBigDecimal() }

    override fun toAmount() = to_amount.textChanges()
            .filter { to_amount.isFocused }
            .map { it.toString().toBigDecimal() }

    // from and to spinner use the same adapter, so might share selections. Be on the lookout for that potential bug
    override fun fromCoin() = from_spinner.itemSelections()
            .map(coinsAdapter::getItem)

    override fun toCoin() = to_spinner.itemSelections()
            .map(coinsAdapter::getItem)

    override fun render(state: ExchangeViewState) {
//        coinsAdapter.addAll(coinsAdapter.)
        from_spinner.setSelection(coinsAdapter.getPosition(state.fromCoin))
        to_spinner.setSelection(coinsAdapter.getPosition(state.toCoin))

        from_amount.setText(state.fromAmount.format())
        to_amount.setText(state.toAmount.format())
    }
}