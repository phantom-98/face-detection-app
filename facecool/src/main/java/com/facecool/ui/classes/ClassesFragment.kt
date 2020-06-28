package com.facecool.ui.classes

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentClassesBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.classes.list.ClassModel
import com.facecool.ui.classes.list.ClassesAdapter
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class ClassesFragment : BaseFragment<FragmentClassesBinding>(FragmentClassesBinding::inflate),
    ClassesAdapter.Listener {

    @Inject
    lateinit var navigator: NavigatorContract

    private val viewModel: ClassesViewModel by viewModels()

    private val adapter = ClassesAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
//        activity?.let {
//            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.classesClassList.layoutManager = LinearLayoutManager(this.requireContext())
        binding.classesClassList.adapter = adapter

        binding.buttonAddClass.setOnClickListener {
            navigator.openAddNewClassActivity(this.requireActivity())
        }
        binding.searchForClass.textChanges()
            .onEach { viewModel.searchForClass(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.classList.observe(this.viewLifecycleOwner) {
            adapter.updateData(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getClassData()
    }

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.classes_screen_title)

    override fun onItemClick(classModel: ClassModel) {
        navigator.openClassDetailsActivity(this.requireActivity(), classModel.uuid)
    }

    override fun onDeleteClass(classModel: ClassModel) {
        FaceCoolDialog.newInstance(
            getString(R.string.classes_dialog_warning_class_delete),
            object : FaceCoolDialog.Listener {
                override fun onPositiveClicked() {
                    viewModel.deleteClass(classModel)
                }
            }
        ).show(parentFragmentManager, FaceCoolDialog.TAG)
    }

    override fun onEditClass(classModel: ClassModel) {
        navigator.openAddNewClassActivity(requireActivity(), classModel.uuid)
    }

}
