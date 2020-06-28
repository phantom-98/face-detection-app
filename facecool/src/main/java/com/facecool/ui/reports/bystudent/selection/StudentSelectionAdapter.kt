package com.facecool.ui.reports.bystudent.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemStudentForSelectionBinding

class StudentSelectionAdapter(private val listener: Listener) :
    RecyclerView.Adapter<StudentSelectionAdapter.ViewModel>() {

    private val studentSelectionModels = mutableListOf<StudentSelectionModel>()

    fun setStudentSelectionModels(studentSelectionModel: List<StudentSelectionModel>) {
        this.studentSelectionModels.clear()
        this.studentSelectionModels.addAll(studentSelectionModel)
        notifyDataSetChanged()
    }

    inner class ViewModel(
        private val binding: ItemStudentForSelectionBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(studentSelectionModelTodelete: StudentSelectionModel) {
            binding.studentNameTextView.text = studentSelectionModelTodelete.studentName
            binding.root.setOnClickListener {
                listener.onStudentSelected(studentSelectionModelTodelete)
            }
        }
    }

    interface Listener {
        fun onStudentSelected(studentModel: StudentSelectionModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewModel {
        return ViewModel(
            ItemStudentForSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun getItemCount() = studentSelectionModels.size

    override fun onBindViewHolder(holder: ViewModel, position: Int) {
        holder.bind(studentSelectionModels[position])
    }

}