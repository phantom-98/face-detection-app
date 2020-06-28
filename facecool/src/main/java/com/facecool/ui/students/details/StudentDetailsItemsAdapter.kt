package com.facecool.ui.students.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemStudentDetailPartBinding
import com.facecool.utils.setIcon

class StudentDetailsItemsAdapter(private val listener: Listener) :
    RecyclerView.Adapter<StudentDetailsItemsAdapter.ViewHolder>() {

    private val items = mutableListOf<StudentDetailItemModel>()

    fun setItems(items: List<StudentDetailItemModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val listener: Listener,
        private val binding: ItemStudentDetailPartBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StudentDetailItemModel) {
            binding.itemIcon.setIcon(item.icon)
            binding.itemLabel.text = item.label
            binding.itemTitle.text = item.value
            binding.itemEditIcon.setOnClickListener {
                listener.onItemEditClicked(item)
            }
        }
    }

    interface Listener {
        fun onItemEditClicked(item: StudentDetailItemModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStudentDetailPartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(listener, binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

}
