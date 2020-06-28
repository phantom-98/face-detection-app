package com.facecool.ui.profile

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemProfileDetailPartBinding
import com.facecool.utils.inflater

class ProfileItemAdapter(private val listener: Listener): RecyclerView.Adapter<ProfileItemAdapter.ViewHolder>() {

    private val items = mutableListOf<ProfileItemModel>()

    fun updateItems(items: List<ProfileItemModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemProfileDetailPartBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: ProfileItemModel) {
            binding.itemTitle.text = item.name
            binding.itemEditIcon.setOnClickListener {
                listener.onItemClicked(item)
            }
        }
    }

    interface Listener {
        fun onItemClicked(item: ProfileItemModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileDetailPartBinding.inflate(parent.inflater(), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

}
