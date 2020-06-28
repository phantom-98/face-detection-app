package com.facecool.ui.students.details

import android.app.Activity
import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ItemClassDataBinding

class StudentEnrolledClassesAdapter(private val listener: Listener) :
    RecyclerView.Adapter<StudentEnrolledClassesAdapter.StudentEnrolledClassesViewHolder>() {

    private val items = mutableListOf<StudentEnrolledClassModel>()

    fun setItems(items: List<StudentEnrolledClassModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class StudentEnrolledClassesViewHolder(
        private val binding: ItemClassDataBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(studentDetailsViewModel: StudentEnrolledClassModel) {
            binding.tvName.text = studentDetailsViewModel.name
            binding.ivClassRemove.text = itemView.context.getString(R.string.remove)
            binding.ivClassRemove.setOnClickListener {
                listener.onRemoveClass(studentDetailsViewModel)
            }
            binding.root.setOnClickListener {
                listener.onClassClicked(studentDetailsViewModel)
            }
        }
    }

    interface Listener {
        fun onClassClicked(studentEnrolledClassModel: StudentEnrolledClassModel)
        fun onRemoveClass(studentEnrolledClassModel: StudentEnrolledClassModel)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StudentEnrolledClassesViewHolder {
        val binding =
            ItemClassDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentEnrolledClassesViewHolder(binding, listener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: StudentEnrolledClassesViewHolder, position: Int) {
        holder.bind(items[position])
    }

}
