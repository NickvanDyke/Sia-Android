package com.vandyke.sia.ui.exchange

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.util.getColorRes
import com.vandyke.sia.util.rx.observe
import com.vandyke.sia.util.snackbar
import kotlinx.android.synthetic.main.activity_exchange.*
import java.math.BigDecimal
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
        supportActionBar!!.run {
            title = "Exchange"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val vm = ViewModelProviders.of(this, factory).get(ExchangeViewModel::class.java)

        val coinsAdapter = ArrayAdapter<String>(this, R.layout.spinner_selected_item_white)
        coinsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        from_spinner.run {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    vm.from.value = coinsAdapter.getItem(position)
                }
            }
            adapter = coinsAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }
        to_spinner.run {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    vm.to.value = coinsAdapter.getItem(position)
                }
            }
            adapter = coinsAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }

        from_amount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty())
                    vm.fromAmount.value = BigDecimal(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        to_amount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty())
                    vm.toAmount.value = BigDecimal(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        vm.coins.observe(this) {
            println(it)
            coinsAdapter.clear()
            coinsAdapter.addAll(it)
            coinsAdapter.notifyDataSetChanged()
        }

        vm.from.observe(this) {
            from_spinner.setSelection(vm.coins.value.indexOfFirst { coin -> coin == it })
        }

        vm.to.observe(this) {
            to_spinner.setSelection(vm.coins.value.indexOfFirst { coin -> coin == it })
        }

        vm.fromAmount.observe(this) {
            if (it.toPlainString() != from_amount.text.toString())
                from_amount.setText(it.toPlainString())
        }

        vm.toAmount.observe(this) {
            if (it.toPlainString() != to_amount.text.toString())
                to_amount.setText(it.toPlainString())
        }

        vm.error.observe(this) {
            it.printStackTrace()
            it.snackbar(exchange_layout)
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
}