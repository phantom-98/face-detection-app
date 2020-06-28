package com.facecool.ui.classes.details.students

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ItemClassStudentBinding
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.classes.details.ClassDetailsActivity
import com.facecool.utils.loadImageFromLocal

class ClassStudentAdapter (private val listener: Listener) : RecyclerView.Adapter<ClassStudentAdapter.ViewHolder>() {

    private val studentList = mutableListOf<CameraDetectionModel>()

    fun setStudentList(studentList: List<CameraDetectionModel>) {
        this.studentList.clear()
        this.studentList.addAll(studentList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemClassStudentBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: CameraDetectionModel) {

            binding.ivStudentPhoto.loadImageFromLocal(student.imageName)

            binding.root.setOnClickListener {
                listener.onStudentClicked(student)
            }
            binding.ivStudentRemove.setOnClickListener {
                listener.onDeleteStudentClicked(student)
            }
            binding.tvStudentName.text = "${student.name} ${student.lastName}"
            binding.ivStudentRemove.text = (listener as StudentListFragment).getString(R.string.remove)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount() = studentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
           holder.bind(studentList[position])
    }

    interface Listener {
        fun onStudentClicked(student: CameraDetectionModel)
        fun onDeleteStudentClicked(student: CameraDetectionModel)
    }

}
