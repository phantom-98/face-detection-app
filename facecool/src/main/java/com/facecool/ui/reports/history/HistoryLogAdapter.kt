package com.facecool.ui.reports.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemHistoryLogBinding
import com.facecool.ui.common.unknownUserImage
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.loadImageFromLocal

class HistoryLogAdapter(private val listener: Listener) :
    RecyclerView.Adapter<HistoryLogAdapter.ViewHolder>() {

    private val itemList = mutableListOf<HistoryItemModel>()

    fun updateItems(newItems: List<HistoryItemModel>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemHistoryLogBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItemModel) {
            binding.root.setOnClickListener { listener.onItemClick(item) }
            binding.root.setOnLongClickListener { listener.onLongClick(item) }

            binding.historyLogTitle.text = item.eventCreation
            binding.historyLogMatchTitle.text = item.studentIdAndName
            binding.historyLogImage.loadImageFromLocal(item.detectedImageAddress)

            when(item.enrollmentStatus){
                EnrollmentStatus.UNKNOWN -> binding.historyLogMatchImage.setImageBitmap(unknownUserImage(binding.root.context))
                else -> binding.historyLogMatchImage.loadImageFromLocal(item.enrolledImageAddress)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHistoryLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    interface Listener {
        fun onItemClick(item: HistoryItemModel)
        fun onLongClick(item: HistoryItemModel): Boolean
    }

}
