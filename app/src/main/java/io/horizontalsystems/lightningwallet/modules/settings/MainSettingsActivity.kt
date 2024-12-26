package io.horizontalsystems.lightningwallet.modules.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.core.BuildConfig
import io.horizontalsystems.core.CoreApp
import io.horizontalsystems.currencyswitcher.CurrencySwitcherModule
import io.horizontalsystems.languageswitcher.LanguageSettingsActivity
import io.horizontalsystems.languageswitcher.LanguageSwitcherModule
import io.horizontalsystems.lightningwallet.BaseActivity
import io.horizontalsystems.lightningwallet.R
import io.horizontalsystems.lightningwallet.databinding.ActivityMainSettingsBinding
import io.horizontalsystems.lightningwallet.helpers.ModuleCode
import io.horizontalsystems.lightningwallet.modules.main.MainModule
import io.horizontalsystems.lightningwallet.modules.settings.security.SecuritySettingsModule


class MainSettingsActivity : BaseActivity() {
    private lateinit var binding: ActivityMainSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val presenter = ViewModelProvider(this, MainSettingsModule.Factory()).get(MainSettingsPresenter::class.java)
        val presenterView = presenter.view as MainSettingsView
        val router = presenter.router as MainSettingsRouter

        bindViewListeners(presenter)
        subscribeToViewEvents(presenterView, presenter)
        subscribeToRouterEvents(router)

        presenter.viewDidLoad()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ModuleCode.LANGUAGE_SWITCH && resultCode == LanguageSwitcherModule.LANGUAGE_CHANGED) {
            MainModule.startAsNewTask(this)
        }
    }

    private fun bindViewListeners(presenter: MainSettingsPresenter) {
        binding.securityCenter.setOnClickListener {
            presenter.didTapSecurity()
        }

        binding.notifications.setOnClickListener {
            presenter.didTapNotifications()
        }

        binding.baseCurrency.setOnClickListener {
            presenter.didTapBaseCurrency()
        }

        binding.language.setOnClickListener {
            presenter.didTapLanguage()
        }

        binding.lightMode.setOnClickListener {
            binding.lightMode.switchToggle()
        }

        binding.about.setOnClickListener {
            presenter.didTapAbout()
        }

        binding.report.setOnClickListener {
            presenter.didTapReportProblem()
        }

        binding.shareApp.setOnClickListener {
            presenter.didTapTellFriends()
        }

        binding.companyLogo.setOnClickListener {
            presenter.didTapCompanyLogo()
        }
    }

    private fun subscribeToViewEvents(presenterView: MainSettingsView, presenter: MainSettingsPresenter) {
        presenterView.baseCurrency.observe(this, Observer { currency ->
            currency?.let {
                binding.baseCurrency.rightTitle = it
            }
        })

        presenterView.backedUp.observe(this, Observer { wordListBackedUp ->
            binding.securityCenter.badge = !wordListBackedUp
        })

        presenterView.language.observe(this, Observer { languageCode ->
            languageCode?.let {
                binding.language.rightTitle = it
            }
        })

        presenterView.lightMode.observe(this, Observer { lightModeValue ->
            lightModeValue?.let {
                binding.lightMode.apply {
                    switchIsChecked = it

                    switchOnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
                        presenter.didSwitchLightMode(isChecked)
                    }
                }
            }
        })

        presenterView.appVersion.observe(this, Observer { version ->
            version?.let {
                var appVersion = getString(R.string.Settings_InfoTitleWithVersion, it)
                appVersion = "$appVersion (${BuildConfig.VERSION_CODE})"
                binding.appName.text = appVersion
            }
        })
    }

    private fun subscribeToRouterEvents(router: MainSettingsRouter) {
        router.showBaseCurrencySettingsLiveEvent.observe(this, Observer {
            CurrencySwitcherModule.start(this)
        })

        router.showLanguageSettingsLiveEvent.observe(this, Observer {
            startActivityForResult(Intent(this, LanguageSettingsActivity::class.java), ModuleCode.LANGUAGE_SWITCH)
        })

        router.showAboutLiveEvent.observe(this, Observer {
            // AboutSettingsActivity.start(it)
        })

        router.showNotificationsLiveEvent.observe(this, Observer {
            // NotificationsModule.start(it)
        })

        router.showReportProblemLiveEvent.observe(this, Observer {
            // ContactModule.start(it)
        })

        router.showSecuritySettingsLiveEvent.observe(this, Observer {
            SecuritySettingsModule.start(this)
        })

        router.openLinkLiveEvent.observe(this, Observer { link ->
            val uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })

        router.shareAppLiveEvent.observe(this, Observer { appWebPageLink ->
            val shareMessage = getString(R.string.SettingsShare_Text) + "\n" + appWebPageLink + "\n"
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.SettingsShare_Title)))
        })

        router.reloadAppLiveEvent.observe(this, Observer {
            val nightMode = if (CoreApp.themeStorage.isLightModeOn)
                AppCompatDelegate.MODE_NIGHT_NO else
                AppCompatDelegate.MODE_NIGHT_YES

            AppCompatDelegate.setDefaultNightMode(nightMode)
        })
    }
}
