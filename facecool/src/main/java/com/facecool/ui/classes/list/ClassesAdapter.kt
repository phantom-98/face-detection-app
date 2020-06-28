package com.facecool.ui.classes.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemClassBinding

class ClassesAdapter(private val listener: Listener) :
    RecyclerView.Adapter<ClassesAdapter.ClassViewHolder>() {

    private val classList = mutableListOf<ClassModel>()

    fun updateData(newCLassList: List<ClassModel>) {
        classList.clear()
        classList.addAll(newCLassList)
        notifyDataSetChanged()
    }

    inner class ClassViewHolder(
        private val binding: ItemClassBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(classModel: ClassModel) {
            binding.itemClassId.text = classModel.id
            binding.itemClassTitle.text = classModel.name
            binding.tvClassStartDate.text = classModel.nextLessonTime
            binding.root.setOnClickListener { listener.onItemClick(classModel) }
            binding.btnEdit.setOnClickListener {
                listener.onEditClass(classModel)
            }
            binding.btnRemove.setOnClickListener {
                listener.onDeleteClass(classModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding = ItemClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassViewHolder(binding, listener)
    }

    override fun getItemCount() = classList.size

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) =
        holder.bind(classList[position])

    interface Listener {
        fun onItemClick(classModel: ClassModel)
        fun onDeleteClass(classModel: ClassModel)
        fun onEditClass(classModel: ClassModel)
    }

}
