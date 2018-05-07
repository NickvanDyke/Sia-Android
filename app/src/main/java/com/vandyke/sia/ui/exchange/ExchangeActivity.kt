package com.vandyke.sia.ui.exchange

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.getColorRes
import kotlinx.android.synthetic.main.activity_exchange.*
import javax.inject.Inject

class ExchangeActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAppComponent().inject(this)

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
        supportActionBar!!.title = "Exchange"

        val vm = ViewModelProviders.of(this, factory).get(ExchangeViewModel::class.java)

        val fromAdapter = ArrayAdapter<String>(this, R.layout.spinner_selected_item_white)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        from_spinner.run {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                }
            }
            adapter = fromAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }
        to_spinner.run {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                }
            }
            adapter = fromAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }
    }
}
