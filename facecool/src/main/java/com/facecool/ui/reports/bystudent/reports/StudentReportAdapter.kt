package com.facecool.ui.reports.bystudent.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemStudentReportBinding

class StudentReportAdapter(private val listener: Listener) :
    RecyclerView.Adapter<StudentReportAdapter.StudentReportViewHolder>() {

    private val studentReportList = mutableListOf<StudentReportModel>()

    fun updateData(studentReportList: List<StudentReportModel>) {
        this.studentReportList.clear()
        this.studentReportList.addAll(studentReportList)
        notifyDataSetChanged()
    }

    inner class StudentReportViewHolder(
        private val binding: ItemStudentReportBinding,
        private val listener: Listener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(studentReportModel: StudentReportModel) {

            adapterPosition.let {
                if (it % 2 == 0) {
                    binding.root.setBackgroundColor(binding.root.context.getColor(com.design.R.color.btnBackgroundColor))
                } else {
                    binding.root.setBackgroundColor(binding.root.context.getColor(com.design.R.color.primaryColor))
                }
            }

            binding.className.text =
                "${studentReportModel.studentClassId} ${studentReportModel.studentClass}"
            binding.barDateName.text = studentReportModel.date
            binding.inName.text = studentReportModel.timeIn
            binding.outName.text = studentReportModel.timeOut
        }
    }


    interface Listener {
        fun onItemClick(studentReportModel: StudentReportModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentReportViewHolder {
        val binding =
            ItemStudentReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentReportViewHolder(binding, listener)
    }

    override fun getItemCount() = studentReportList.size

    override fun onBindViewHolder(holder: StudentReportViewHolder, position: Int) {
        holder.bind(studentReportList[position])
    }

}
