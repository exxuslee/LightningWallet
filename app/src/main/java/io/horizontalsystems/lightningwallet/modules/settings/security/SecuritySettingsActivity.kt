package io.horizontalsystems.lightningwallet.modules.settings.security

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.lightningwallet.BaseActivity
import io.horizontalsystems.lightningwallet.R
import io.horizontalsystems.lightningwallet.databinding.ActivitySettingsSecurityBinding
import io.horizontalsystems.lightningwallet.modules.main.MainModule
import io.horizontalsystems.pin.PinModule
import io.horizontalsystems.views.TopMenuItem
import kotlin.system.exitProcess

class SecuritySettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsSecurityBinding
    private lateinit var viewModel: SecuritySettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shadowlessToolbar.bind(getString(R.string.Settings_SecurityCenter), TopMenuItem(R.drawable.ic_back, onClick = { onBackPressed() }))

        viewModel = ViewModelProvider(this).get(SecuritySettingsViewModel::class.java)
        viewModel.init()

        binding.changePin.setOnClickListener { viewModel.delegate.didTapEditPin() }

        binding.fingerprint.switchOnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            viewModel.delegate.didSwitchBiometricEnabled(isChecked)
        }

        binding.fingerprint.setOnClickListener {
            binding.fingerprint.switchToggle()
        }

        binding.enablePin.switchOnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            viewModel.delegate.didSwitchPinSet(isChecked)
        }

        binding.enablePin.setOnClickListener {
            binding.enablePin.switchToggle()
        }

        //  Handling view model live events

        viewModel.pinSetLiveData.observe(this, Observer { pinEnabled ->
            binding.enablePin.switchIsChecked = pinEnabled
        })

        viewModel.editPinVisibleLiveData.observe(this, Observer { pinEnabled ->
            binding.changePin.visibility = if (pinEnabled) View.VISIBLE else View.GONE
            binding.enablePin.bottomBorder = !pinEnabled
        })

        viewModel.biometricSettingsVisibleLiveData.observe(this, Observer { enabled ->
            binding.fingerprint.visibility = if (enabled) View.VISIBLE else View.GONE
        })

        viewModel.biometricEnabledLiveData.observe(this, Observer {
            binding.fingerprint.switchIsChecked = it
        })

        //  router

        viewModel.openEditPinLiveEvent.observe(this, Observer {
            PinModule.startForEditPin(this)
        })

        viewModel.openSetPinLiveEvent.observe(this, Observer {
            PinModule.startForSetPin(this, REQUEST_CODE_SET_PIN)
        })

        viewModel.openUnlockPinLiveEvent.observe(this, Observer {
            PinModule.startForUnlock(this, REQUEST_CODE_UNLOCK_PIN_TO_DISABLE_PIN)
        })

        viewModel.restartApp.observe(this, Observer {
            finishAffinity()
            MainModule.startAsNewTask(this)
            SecuritySettingsModule.start(this)
            exitProcess(0)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SET_PIN) {
            when (resultCode) {
                PinModule.RESULT_OK -> viewModel.delegate.didSetPin()
                PinModule.RESULT_CANCELLED -> viewModel.delegate.didCancelSetPin()
            }
        }

        if (requestCode == REQUEST_CODE_UNLOCK_PIN_TO_DISABLE_PIN) {
            when (resultCode) {
                PinModule.RESULT_OK -> viewModel.delegate.didUnlockPinToDisablePin()
                PinModule.RESULT_CANCELLED -> viewModel.delegate.didCancelUnlockPinToDisablePin()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SET_PIN = 1
        const val REQUEST_CODE_UNLOCK_PIN_TO_DISABLE_PIN = 2
    }
}
