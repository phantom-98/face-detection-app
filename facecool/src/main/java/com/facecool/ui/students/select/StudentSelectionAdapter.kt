package com.facecool.ui.students.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemStudentSelectionBinding
import com.facecool.utils.loadImageFromLocal

class StudentSelectionAdapter(private val listener: Listener) :
    RecyclerView.Adapter<StudentSelectionAdapter.ViewHolder>() {

    private val students = mutableListOf<StudentSelectionModel>()

    fun setStudents(students: List<StudentSelectionModel>) {
        this.students.clear()
        this.students.addAll(students)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemStudentSelectionBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: StudentSelectionModel) {
            binding.checkboxStudent.setOnCheckedChangeListener(null)
            binding.checkboxStudent.isChecked = student.isSelected
            binding.ivStudent.loadImageFromLocal(student.studentImagePath)
            binding.tvStudentName.text = student.name
            binding.checkboxStudent.setOnCheckedChangeListener { _, isChecked ->
                listener.onStudentSelected(student, isChecked)
            }
            binding.root.setOnClickListener {
                listener.onStudentClick(student)
            }
        }

    }

    interface Listener {
        fun onStudentSelected(student: StudentSelectionModel, selectionStatus: Boolean)
        fun onStudentClick(student: StudentSelectionModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStudentSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount() = students.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(students[position])
    }

}
