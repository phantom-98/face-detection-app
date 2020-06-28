package com.facecool.ui.classes.details.ocurrences.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.facecool.R
import com.facecool.databinding.ActivityAddNewOccurreceBinding
import com.facecool.ui.BaseActivity
import com.facecool.utils.getReadableDate
import com.facecool.utils.openDateSelector
import com.facecool.utils.openTimeSelector
import com.facecool.utils.setIcon
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityAddNewOccurrence : BaseActivity() {

    companion object {

        private const val CLASS_ID = "CLASS_ID"
        private const val LESSON_ID = "LESSON_ID"

        @JvmStatic
        fun start(context: Context, classID: Long) {
            val starter = Intent(context, ActivityAddNewOccurrence::class.java)
                .putExtra(CLASS_ID, classID)
            context.startActivity(starter)
        }
        fun start(context: Context, classID: Long, lessonID: Long) {
            val starter = Intent(context, ActivityAddNewOccurrence::class.java)
                .putExtra(LESSON_ID, lessonID)
                .putExtra(CLASS_ID, classID)
            context.startActivity(starter)
        }
    }

    private val viewModel by viewModels<AddNewOccurrenceViewModel>()

    private lateinit var binding: ActivityAddNewOccurreceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewOccurreceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.genericToolbar.toolbar)


        val lessonID = intent.getLongExtra(LESSON_ID, -1)
        if (lessonID > -1) {
            val classID = intent.getLongExtra(CLASS_ID, -1)
            if (classID > -1){
                viewModel.getClassName(classID)
            }
            supportActionBar?.title = getString(R.string.edit_lesson)
            viewModel.setupForEditLesson(lessonID)

            binding.addButton.setText(R.string.add_new_user_activity_update_button)

        } else {
            supportActionBar?.title = getString(R.string.add_lesson_title)
            val classID = intent.getLongExtra(CLASS_ID,-1)
            viewModel.setupForAddingNewClass(classID)

        }
        viewModel.className.observe(this){
            supportActionBar?.title = getString(R.string.edit_lesson) + ": $it"
        }
        binding.addButton.setOnClickListener {
            viewModel.addNewLesson()
        }

        viewModel.genericErrorMessage.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.genericProgress.observe(this){

        }

        viewModel.finishEvent.observe(this){
            finish()
        }


        binding.date.icon.setIcon(R.drawable.ic_date)
        binding.startTime.icon.setIcon(R.drawable.ic_time)
        binding.endTime.icon.setIcon(R.drawable.ic_time)

        binding.date.label.text = getString(R.string.add_lesson_date)
        binding.startTime.label.text = getString(R.string.add_lesson_start_time)
        binding.endTime.label.text = getString(R.string.add_lesson_end_time)

        viewModel.date.observe(this){
            binding.date.inputField.text = it.getReadableDate(pattern = "MMM dd")
        }
        binding.date.inputField.setOnClickListener {
            openDateSelector(default = viewModel.lessonModel.date ?: 0) {
                viewModel.onNewDateSelected(it)
                binding.date.inputField.text = it.getReadableDate(pattern = "MMM dd")
            }
        }
        viewModel.startTime.observe(this){
            binding.startTime.inputField.text = it.getReadableDate(pattern = "HH:mm")
        }
        binding.startTime.inputField.setOnClickListener {
            openTimeSelector(default = viewModel.lessonModel.startLesson ?: 0) {
                viewModel.onNewStartTimeSelected(it)
                binding.startTime.inputField.text = it.getReadableDate(pattern = "HH:mm")
            }
        }
        viewModel.endTime.observe(this){
            binding.endTime.inputField.text = it.getReadableDate(pattern = "HH:mm")
        }
        binding.endTime.inputField.setOnClickListener {
            openTimeSelector(default = viewModel.lessonModel.endLesson ?: 0) {
                viewModel.onNewEndTimeSelected(it)
                binding.endTime.inputField.text = it.getReadableDate(pattern = "HH:mm")
            }
        }
        viewModel.autoKiosk.observe(this){
            binding.checkboxLessonKiosk.isChecked = it
        }
        binding.checkboxLessonKiosk.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.onNewAutoKiosk(isChecked)
        }
        viewModel.liveness.observe(this){
            binding.checkboxLessonLiveness.isChecked = it
        }
        binding.checkboxLessonLiveness.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.onNewLiveness(isChecked)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }



    }


}
