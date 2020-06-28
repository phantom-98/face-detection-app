package com.facecool.ui.reports.byclass.selection

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentClassSelectionBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.reports.byclass.ClassReportNavigator
import com.facecool.ui.reports.byclass.SelectClassViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClassSelectionFragment :
    BaseFragment<FragmentClassSelectionBinding>(FragmentClassSelectionBinding::inflate),
    ClassSelectionAdapter.Listener {

    companion object{
        fun newInstance() = ClassSelectionFragment()
    }

    override fun getTitle(): String = getString(R.string.class_report_class_list)

    private val viewModel: SelectClassViewModel by viewModels()

    private val adapter = ClassSelectionAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.classSelectionRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.classSelectionRecyclerView.adapter = adapter

        viewModel.classes.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }

        viewModel.getClasses()

        viewModel.selectedClass.observe(viewLifecycleOwner) {
            (parentFragment as? ClassReportNavigator)?.onClassSelected(it)
        }

    }

    override fun onClassSelected(classSelectionModel: ClassSelectionModel) {
        viewModel.onClassSelected(classSelectionModel)
    }

}