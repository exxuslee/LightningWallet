package io.horizontalsystems.lightningwallet.modules.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.core.setOnSingleClickListener
import io.horizontalsystems.lightningwallet.databinding.ViewHolderChannelBinding
import io.horizontalsystems.lightningwallet.modules.channels.ChannelViewItem.UpdateType
import io.horizontalsystems.views.helpers.AnimationHelper
import io.horizontalsystems.views.showIf

class ChannelsAdapter(private val listener: Listener) : RecyclerView.Adapter<ViewHolderChannel>() {

    interface Listener {
        fun onItemClick(item: ChannelViewItem)
        fun onClickInfo(item: ChannelViewItem)
        fun onClickManage(item: ChannelViewItem)
    }

    private var items = listOf<ChannelViewItem>()

    fun setItems(viewItems: List<ChannelViewItem>) {
        if (items.isEmpty()) {
            items = viewItems
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(ChannelViewItemDiff(items, viewItems))
            items = viewItems
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderChannel {
        val binding = ViewHolderChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderChannel(binding, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolderChannel, position: Int) {
    }

    override fun onBindViewHolder(holder: ViewHolderChannel, position: Int, payloads: MutableList<Any>) {
        val item = items[position]

        if (payloads.isEmpty()) {
            holder.bind(item)
        } else {
            holder.bindUpdate(item, payloads)
        }
    }
}

class ViewHolderChannel(private val binding: ViewHolderChannelBinding, private val listener: ChannelsAdapter.Listener)
    : RecyclerView.ViewHolder(binding.root) {

    private var channelViewItem: ChannelViewItem? = null

    init {
        binding.root.setOnClickListener {
            channelViewItem?.let {
                listener.onItemClick(it)
            }
        }

        binding.buttonManage.setOnSingleClickListener {
            channelViewItem?.let {
                listener.onClickManage(it)
            }
        }

        binding.buttonInfo.setOnSingleClickListener {
            channelViewItem?.let {
                listener.onClickInfo(it)
            }
        }
    }

    fun bind(item: ChannelViewItem) {
        channelViewItem = item

        item.apply {
            binding.iconCoin.bind("BTC")
            binding.channelId.text = remotePubKey
            binding.channelState.text = state.name

            val total = localBalance + remoteBalance
            binding.balanceCoin.text = "$total"
            binding.balanceFiat.text = "$total"
            binding.canSentAmount.text = "$localBalance"
            binding.canReceiveAmount.text = "$remoteBalance"

            binding.root.isSelected = expanded
            binding.buttonsWrapper.showIf(expanded)
        }
    }

    fun bindUpdate(item: ChannelViewItem, payloads: MutableList<Any>) {
        payloads.forEach {
            when (it) {
                UpdateType.EXPAND -> bindUpdateExpanded(item)
                UpdateType.UPDATE -> bind(item)
            }
        }
    }

    private fun bindUpdateExpanded(item: ChannelViewItem) {
        binding.root.isSelected = item.expanded

        if (item.expanded) {
            AnimationHelper.expand(binding.buttonsWrapper)
        } else {
            AnimationHelper.collapse(binding.buttonsWrapper)
        }
    }
}
