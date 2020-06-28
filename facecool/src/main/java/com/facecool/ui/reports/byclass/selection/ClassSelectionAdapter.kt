package com.facecool.ui.reports.byclass.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemClassForSelectionBinding

class ClassSelectionAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<ClassSelectionAdapter.ViewHolder>() {

    private val items = mutableListOf<ClassSelectionModel>()

    fun setItems(items: List<ClassSelectionModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemClassForSelectionBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(classSelectionModel: ClassSelectionModel) {
            binding.classNameTextView.text = classSelectionModel.className
            binding.root.setOnClickListener {
                listener.onClassSelected(classSelectionModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemClassForSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface Listener {
        fun onClassSelected(classSelectionModel: ClassSelectionModel)
    }

}