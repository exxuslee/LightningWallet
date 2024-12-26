package io.horizontalsystems.lightningwallet.modules.welcome

import android.os.Bundle
import android.util.Log
import io.horizontalsystems.lightningwallet.BaseActivity
import io.horizontalsystems.lightningwallet.R
import io.horizontalsystems.lightningwallet.databinding.ActivityWelcomeBinding
import io.horizontalsystems.lightningwallet.modules.nodecredentials.NodeCredentialsModule
import io.horizontalsystems.lightningwallet.modules.send.SendModule

class WelcomeActivity : BaseActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonConnect.setOnClickListener {
            Log.d("WelcomeActivity", it.toString())
            NodeCredentialsModule.start(this)
        }

        binding.buttonSend.setOnClickListener {
            SendModule.start(this)
        }
    }

}
