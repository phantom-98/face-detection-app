package com.facecool.ui.students.attendance

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ItemAttendanceBinding

class AttendanceAdapter(private val app: Application) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    private val attendanceList = mutableListOf<AttendanceModel>()

    fun updateAttendanceList(list: List<AttendanceModel>) {
        attendanceList.clear()
        attendanceList.addAll(list)
        notifyDataSetChanged()
    }

    inner class AttendanceViewHolder(
        private val binding: ItemAttendanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AttendanceModel) {
            binding.date.text = item.date
            binding.status.text = if (item.status) app.getString(R.string.attendance_present) else app.getString(R.string.attendance_absent)
            binding.classTitle.text = item.className
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding =
            ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun getItemCount() = attendanceList.size

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendanceList[position])
    }

}
