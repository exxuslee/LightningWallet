package io.horizontalsystems.lightningwallet.modules.nodeconnect

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.lightningkit.remote.RemoteLndCredentials
import io.horizontalsystems.lightningwallet.BaseActivity
import io.horizontalsystems.lightningwallet.R
import io.horizontalsystems.lightningwallet.modules.main.MainModule
import io.horizontalsystems.lightningwallet.databinding.ActivityConnectBinding // ViewBinding import

class NodeConnectActivity : BaseActivity() {

    lateinit var presenter: NodeConnectPresenter
    lateinit var router: NodeConnectRouter
    lateinit var view: NodeConnectView

    private lateinit var binding: ActivityConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val credentials = intent.getParcelableExtra<RemoteLndCredentials>("credentials") ?: run {
            finish()
            return
        }

        title = getString(R.string.Remote_Connect)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = ViewModelProvider(this, NodeConnectModule.Factory(credentials)).get(NodeConnectPresenter::class.java)
        router = presenter.router as NodeConnectRouter
        view = presenter.view as NodeConnectView

        presenter.onLoad()

        observeEvents()
        observeActions()
    }

    private fun observeEvents() {
        view.showAddress.observe(this, Observer {
            binding.addressValue.text = it
        })

        view.showConnecting.observe(this, Observer {
            binding.buttonConnect.isEnabled = false
            binding.progress.visibility = View.VISIBLE
        })

        view.hideConnecting.observe(this, Observer {
            binding.buttonConnect.isEnabled = true
            binding.progress.visibility = View.GONE
        })

        view.showError.observe(this, Observer {
            binding.addressValue.text = it
        })

        router.openMainModule.observe(this, Observer {
            MainModule.start(this)
        })
    }

    private fun observeActions() {
        binding.buttonConnect.setOnClickListener {
            presenter.onClickConnect()
        }
    }
}