package io.horizontalsystems.lightningwallet.modules.channels

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.lightningwallet.R
import io.horizontalsystems.views.TopMenuItem
import io.horizontalsystems.lightningwallet.databinding.ActivityChannelsBinding

class ChannelsActivity : AppCompatActivity(), ChannelsAdapter.Listener {

    private lateinit var binding: ActivityChannelsBinding
    private val presenter by lazy {
        ViewModelProvider(this, ChannelsModule.Factory()).get(ChannelsPresenter::class.java)
    }

    private val channelsAdapter = ChannelsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChannelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()

        presenter.onLoad()

        binding.channelsRecycler.adapter = channelsAdapter

        observeActions()
        observeEvents()
    }

    private fun observeActions() {
        binding.open.isSelected = true
        binding.open.setOnClickListener {
            selectItem(it)
        }

        binding.closed.setOnClickListener {
            selectItem(it)
        }
    }

    private fun observeEvents() {
        val view = presenter.view as ChannelsView

        view.updateChannels.observe(this, Observer {
            channelsAdapter.setItems(it)
        })
    }

    private fun setActionBar() {
        val leftBtn = TopMenuItem(text = R.string.Main_Backup, onClick = {})

        val rightBtn = TopMenuItem(text = R.string.Button_Close, onClick = {
            onBackPressed()
        })

        binding.shadowlessToolbar.bind(title = getString(R.string.Main_Channels), leftBtnItem = leftBtn, rightBtnItem = rightBtn)
    }

    private fun selectItem(item: View) {
        binding.closed.isSelected = false
        binding.open.isSelected = false
        item.isSelected = true
    }

    // ChannelsAdapter.Listener

    override fun onItemClick(item: ChannelViewItem) {
        presenter.onSelectItem(item)
    }

    override fun onClickInfo(item: ChannelViewItem) {
    }

    override fun onClickManage(item: ChannelViewItem) {
    }
}
