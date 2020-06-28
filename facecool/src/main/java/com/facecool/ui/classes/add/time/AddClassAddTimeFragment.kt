package com.facecool.ui.classes.add.time

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentAddClassAddTimeBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.classes.add.AddClassNavigator
import com.facecool.ui.classes.common.SharedAddNewClassData
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.common.SpinnerSelectionListener
import com.facecool.utils.openDateSelector
import com.facecool.utils.openTimeSelector
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddClassAddTimeFragment :
    BaseFragment<FragmentAddClassAddTimeBinding>(FragmentAddClassAddTimeBinding::inflate),
    WeekDaysAdapter.Listener, CustomDaysAdapter.Listener {

    companion object {
        fun newInstance() = AddClassAddTimeFragment()
    }

    private val viewModel: AddClassTimeViewModel by viewModels()

    private val weekAdapter = WeekDaysAdapter(this)
    private val customAdapter = CustomDaysAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AddClassNavigator)?.updateTitle(getString(R.string.add_class_add_time_title))

        setupViewModelObservables()
        setupViewBindings()

    }

    private fun setupViewBindings() {

        val wrapLayoutManager = GridLayoutManager(context, 7)

        binding.spinnerRepeat.onItemSelectedListener = object : SpinnerSelectionListener() {
            override fun onItemSelected(position: Int) {
                viewModel.repeatTypeSelected(position)
            }
        }

        binding.weekList.layoutManager = wrapLayoutManager
        binding.weekList.adapter = weekAdapter

        binding.customDaysList.layoutManager = LinearLayoutManager(context)
        binding.customDaysList.adapter = customAdapter

        binding.nextButton.setOnClickListener {
            viewModel.generateLessons()
        }

        binding.cancelButton.setOnClickListener {
            activity?.finish()
        }

        binding.inputStartDate.setOnClickListener {
            requireContext().openDateSelector(default = SharedAddNewClassData.startDate, max = SharedAddNewClassData.endDate) {
                viewModel.updateStartDate(it)
            }
        }

        binding.inputEndDate.setOnClickListener {
            requireContext().openDateSelector(default = SharedAddNewClassData.endDate, min = SharedAddNewClassData.startDate) {
                viewModel.updateEndDate(it)
            }
        }

        binding.inputStartTime.setOnClickListener {
            requireContext().openTimeSelector(default = SharedAddNewClassData.startTime) {
                viewModel.updateStartTime(it)
            }
        }

        binding.inputEndTime.setOnClickListener {
            requireContext().openTimeSelector(default = SharedAddNewClassData.endTime) {
                viewModel.updateEndTime(it)
            }
        }
        binding.checkboxLessonKiosk.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateAutoKiosk(isChecked)
        }
        binding.checkboxLessonLiveness.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateLiveness(isChecked)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun setupViewModelObservables() {
        viewModel.updateRepeatType()
        viewModel.repeatType.observe(viewLifecycleOwner) {
            val spinnerAdapter =
                ArrayAdapter(requireContext(), R.layout.item_spinner_custom, it)
            binding.spinnerRepeat.adapter = spinnerAdapter
        }

        viewModel.viewMode.observe(viewLifecycleOwner) {
            when (it) {
                RepeatType.WEEKLY -> {
                    viewModel.updateWeekData()
                    binding.weekList.visibility = View.GONE
                    binding.weekList.visibility = View.VISIBLE
                    binding.customDaysList.visibility = View.GONE

                    binding.timeLabel.visibility = View.VISIBLE
                    binding.timeToLabel.visibility = View.VISIBLE
                    binding.iconTime.visibility = View.VISIBLE
                    binding.inputEndTime.visibility = View.VISIBLE
                    binding.inputStartTime.visibility = View.VISIBLE
                }

                RepeatType.MONTHLY -> {
                    viewModel.updateMonthData()
                    binding.weekList.visibility = View.GONE
                    binding.weekList.visibility = View.VISIBLE
                    binding.customDaysList.visibility = View.GONE

                    binding.timeLabel.visibility = View.VISIBLE
                    binding.timeToLabel.visibility = View.VISIBLE
                    binding.iconTime.visibility = View.VISIBLE
                    binding.inputEndTime.visibility = View.VISIBLE
                    binding.inputStartTime.visibility = View.VISIBLE
                }
                RepeatType.DAILY -> {
                    binding.weekList.visibility = View.GONE
                    binding.customDaysList.visibility = View.GONE

                    binding.timeLabel.visibility = View.VISIBLE
                    binding.timeToLabel.visibility = View.VISIBLE
                    binding.iconTime.visibility = View.VISIBLE
                    binding.inputEndTime.visibility = View.VISIBLE
                    binding.inputStartTime.visibility = View.VISIBLE
                }

                RepeatType.CUSTOM -> {
                    binding.weekList.visibility = View.GONE
                    binding.customDaysList.visibility = View.VISIBLE

                    binding.timeLabel.visibility = View.GONE
                    binding.timeToLabel.visibility = View.GONE
                    binding.iconTime.visibility = View.GONE
                    binding.inputEndTime.visibility = View.GONE
                    binding.inputStartTime.visibility = View.GONE
                }
            }
        }

        viewModel.startDate.observe(viewLifecycleOwner) {
            binding.inputStartDate.text = it
        }

        viewModel.endDate.observe(viewLifecycleOwner) {
            binding.inputEndDate.text = it
        }

        viewModel.startTime.observe(viewLifecycleOwner) {
            binding.inputStartTime.text = it
        }

        viewModel.endTime.observe(viewLifecycleOwner) {
            binding.inputEndTime.text = it
        }

        viewModel.customDays.observe(viewLifecycleOwner) {
            customAdapter.setDays(it)
        }

        viewModel.weekDays.observe(viewLifecycleOwner) {
            weekAdapter.setDays(it)
        }

        viewModel.onClassCreated.observe(viewLifecycleOwner) {
            (activity as? AddClassNavigator)?.onOccurrenceAdded(it)
        }

        viewModel.onLessonsCreated.observe(viewLifecycleOwner) {
            FaceCoolDialog.newInstance(
                getString(R.string.lesson_create_confirm_message, it),
                object : FaceCoolDialog.Listener {
                    override fun onPositiveClicked() {
                        viewModel.createClass()
                    }
                }
            ).show(this.requireActivity().supportFragmentManager, FaceCoolDialog.TAG)
        }
    }

    override fun onDaySelected(day: DayModel) {
        viewModel.onDaySelected(day)
    }

    override fun onDataChanged(day: CustomDayModel) {

    }

    override fun onItemAdd(item: CustomDayModel) {
        TODO("Not yet implemented")
    }

    override fun onItemRemove(item: CustomDayModel) {
        TODO("Not yet implemented")
    }

    override fun getTitle(): String = ""

}
