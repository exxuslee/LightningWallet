package io.horizontalsystems.lightningwallet.modules.main

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.lightningwallet.BaseActivity
import io.horizontalsystems.lightningwallet.R
import io.horizontalsystems.lightningwallet.databinding.ActivityMainBinding
import io.horizontalsystems.lightningwallet.modules.channels.ChannelsModule
import io.horizontalsystems.lightningwallet.modules.settings.MainSettingsModule

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val presenter by lazy {
        ViewModelProvider(this, MainModule.Factory()).get(MainPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter.onLoad()

        observeEvents()
        observeActions()
    }

    private fun observeEvents() {
        val view = presenter.view as MainView

        view.showBalance.observe(this, Observer {
            binding.totalBalance.text = "$it.0 sat"
        })

        view.showSyncingText.observe(this, Observer {
            binding.syncingText.text = it
            binding.syncingText.visibility = View.VISIBLE
        })

        view.hideSyncingText.observe(this, Observer {
            binding.syncingText.visibility = View.GONE
        })
    }

    private fun observeActions() {
        binding.channels.setOnClickListener {
            ChannelsModule.start(this)
        }

        binding.settings.setOnClickListener {
            MainSettingsModule.start(this)
        }
    }
}
