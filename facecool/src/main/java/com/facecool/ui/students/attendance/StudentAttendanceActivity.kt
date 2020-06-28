package com.facecool.ui.students.attendance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.ActivityStudentAttendanceBinding
import com.facecool.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentAttendanceActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context, studentId:String) {
            val starter = Intent(context, StudentAttendanceActivity::class.java)
            context.startActivity(starter)
        }
    }

    private val viewModel: StudentAttendanceViewModel by viewModels()

    private lateinit var binding: ActivityStudentAttendanceBinding

    private val attendanceAdapter = AttendanceAdapter(application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.genericToolbarContainer.toolbar)
        supportActionBar?.title = getString(R.string.attendance)

        binding.attendanceList.adapter = attendanceAdapter
        binding.attendanceList.layoutManager = LinearLayoutManager(this)

        viewModel.getAttendanceForStudent(1)

        viewModel.attendanceList.observe(this) {
            attendanceAdapter.updateAttendanceList(it)
        }

    }


}
