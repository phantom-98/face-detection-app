package com.facecool.ui.classes.details.ocurrences.details

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ItemClassOccurrenceStudentBinding
import com.facecool.ui.students.common.Attendance
import com.facecool.utils.getReadableDate
import com.facecool.utils.gone
import com.facecool.utils.loadImageFromLocal
import com.facecool.utils.visible


class ClassOccurrenceStudentsAdapter(private val listener: Listener) :
    RecyclerView.Adapter<ClassOccurrenceStudentsAdapter.ViewHolder>() {

    private val students = mutableListOf<ClassOccurrenceStudentModel>()

    fun submitList(newStudents: List<ClassOccurrenceStudentModel>) {
        students.clear()
        students.addAll(newStudents)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemClassOccurrenceStudentBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: ClassOccurrenceStudentModel) {
            binding.tvClassOccurrenceStudentName.text = student.name

            val time =
                if (student.time == -1L) "" else student.time.getReadableDate(pattern = "HH:mm")

            binding.imgStudent.loadImageFromLocal(student.imageName)

            if (student.attendance == Attendance.ABSENT) {
                val colorMatrix = ColorMatrix()
                colorMatrix.setSaturation(0f)
                val filter = ColorMatrixColorFilter(colorMatrix)
                binding.imgStudent.colorFilter = filter
            }

            binding.tvClassOccurrenceStudentStatus.text = when (student.attendance) {
                Attendance.PRESENT, Attendance.MANUAL -> binding.root.context.getString(R.string.attendance_present) + " $time"
                Attendance.ABSENT -> binding.root.context.getString(R.string.attendance_absent)
                Attendance.LATE -> binding.root.context.getString(R.string.attendance_late) + " $time"
                Attendance.TBD -> ""
            }
            binding.root.setOnClickListener {
                listener.onStudentClicked(student)
            }

            if (student.attendance == Attendance.PRESENT || student.attendance == Attendance.TBD) {
                binding.btnManualAttendance.visibility = ViewGroup.INVISIBLE
            } else {
                binding.btnManualAttendance.visible()
            }

            if (student.attendance == Attendance.MANUAL){
                binding.btnManualAttendance.setText(itemView.resources.getString(R.string.class_report_revert_manual))
                binding.btnManualAttendance.setBackgroundColor(itemView.resources.getColor(R.color.revert_manual))
            }

            binding.btnManualAttendance.setOnClickListener {
                if(binding.btnManualAttendance.text == itemView.resources.getString(R.string.class_report_manual_present)) {
                    listener.setStudentPresentManually(student)
                    binding.tvClassOccurrenceStudentStatus.text = itemView.resources.getString(R.string.attendance_present) + student.time.getReadableDate(pattern = " HH:mm")
                    binding.btnManualAttendance.text = itemView.resources.getString(R.string.class_report_revert_manual)
                    binding.btnManualAttendance.setBackgroundColor(itemView.resources.getColor(R.color.revert_manual))
                    binding.imgStudent.colorFilter = null
                }
                else {
                    listener.reverseStudentManually(student)
                    if (student.attendance == Attendance.ABSENT) {
                        binding.tvClassOccurrenceStudentStatus.text =
                            itemView.resources.getString(R.string.attendance_absent)
                        binding.imgStudent.colorFilter = ColorMatrixColorFilter(ColorMatrix().also { it.setSaturation(0f) })
                    }
                    else {
                        binding.tvClassOccurrenceStudentStatus.text = itemView.resources.getString(R.string.attendance_late) + student.time.getReadableDate(pattern = " HH:mm")
                        binding.imgStudent.colorFilter = null
                    }
                    binding.btnManualAttendance.text = itemView.resources.getString(R.string.class_report_manual_present)
                    binding.btnManualAttendance.setBackgroundColor(itemView.resources.getColor(R.color.manual_present))
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassOccurrenceStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, listener)
    }

    override fun getItemCount() = students.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(students[position])
    }

    interface Listener {
        fun onStudentClicked(student: ClassOccurrenceStudentModel)
        fun setStudentPresentManually(student: ClassOccurrenceStudentModel)
        fun reverseStudentManually(student: ClassOccurrenceStudentModel)
    }

}
