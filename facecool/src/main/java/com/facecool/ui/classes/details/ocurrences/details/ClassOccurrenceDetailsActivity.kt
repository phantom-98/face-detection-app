package com.facecool.ui.classes.details.ocurrences.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.ActivityClassOccurenceDetailsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.utils.getExtraClass
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClassOccurrenceDetailsActivity : BaseActivity(), ClassOccurrenceStudentsAdapter.Listener {

    companion object {
        private const val CLASS_OCCURRENCE_ID = "CLASS_OCCURRENCE_ID"

        @JvmStatic
        fun start(context: Context, classOccurrence: LessonModel) {
            val starter = Intent(context, ClassOccurrenceDetailsActivity::class.java)
                .putExtra(CLASS_OCCURRENCE_ID, classOccurrence)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    private lateinit var binding: ActivityClassOccurenceDetailsBinding
    private val viewModel: ClassOccurrenceDetailsViewModel by viewModels()
    private var classOccurrence: LessonModel? = null

    private val adapter = ClassOccurrenceStudentsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassOccurenceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarContainer.toolbar)

        classOccurrence = getExtraClass(CLASS_OCCURRENCE_ID)

        binding.classOccurrenceDetailsStudentList.layoutManager = LinearLayoutManager(this)
        binding.classOccurrenceDetailsStudentList.adapter = adapter

        binding.toolbarContainer.ivCloseJourney?.visibility = View.VISIBLE
        binding.toolbarContainer.ivCloseJourney?.setOnClickListener {
            navigator.closeJourney(this)
        }

        classOccurrence?.let { viewModel.setClassOccurrence(it) }

        viewModel.title.observe(this) {
            supportActionBar?.title = it
        }

        viewModel.lessonDetails.observe(this){
            binding.classOccurrenceDetailsStudentListLabel.text = getString(R.string.lesson_attendance_for_student, it)
        }

        viewModel.students.observe(this) {
            adapter.submitList(it)
        }
    }

    override fun onStudentClicked(student: ClassOccurrenceStudentModel) {
//        viewModel.onStudentClicked(student)
//        navigator.openStudentDetailsActivity(this, student.id.toLongOrNull() ?: -1)
    }

    override fun setStudentPresentManually(student: ClassOccurrenceStudentModel) {
        viewModel.setStudentToPResent(student)
    }

    override fun reverseStudentManually(student: ClassOccurrenceStudentModel) {
        viewModel.revertStudentManually(student)
    }
}
