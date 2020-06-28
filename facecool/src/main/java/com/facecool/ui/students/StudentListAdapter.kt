package com.facecool.ui.students

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.face.cool.databasa.users.ErrorStatus
import com.facecool.R
import com.facecool.databinding.ItemStudentBinding
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.common.unknownUserImage
import com.facecool.utils.loadImageFromLocal

class StudentListAdapter(private val listener: Listener) :
    RecyclerView.Adapter<StudentListAdapter.StudentViewHolder>() {

    private val students = mutableListOf<CameraDetectionModel>()

    fun setStudents(students: List<CameraDetectionModel>) {
        this.students.clear()
        this.students.addAll(students)
        notifyDataSetChanged()
    }
    fun clearStudents(){
        this.students.clear()
        notifyDataSetChanged()
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: CameraDetectionModel) {

            if (student.error.isNotEmpty()) {
                binding.card.background.setTint(
                    itemView.context.getColor(R.color.issues)
                )
            }

            if (student.error.contains(ErrorStatus.IMAGE_NOT_PRESENT) || student.error.contains(ErrorStatus.NO_FACE_DETECTED)){
                binding.studentImage.setImageBitmap(unknownUserImage(binding.root.context))
            }else{
                binding.studentImage.loadImageFromLocal(student.imageName)
            }

            binding.studentName.text = "${student.name} ${student.lastName} : ${student.studentId}"
            binding.root.setOnClickListener {
                listener.onStudentClicked(student)
            }
            binding.btnRemoveStudent.setOnClickListener {
                listener.removeStudent(student)
            }
        }
    }

    interface Listener {
        fun onStudentClicked(student: CameraDetectionModel)
        fun removeStudent(student: CameraDetectionModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding, listener)
    }

    override fun getItemCount() = students.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position])
    }

}
