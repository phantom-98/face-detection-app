package com.facecool.ui.classes.add.general

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.facecool.R
import com.facecool.databinding.FragmentAddClassGeneralBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.classes.add.AddClassNavigator
import com.facecool.ui.classes.add.AddNewClassActivity
import com.facecool.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AddClassGeneralFragment :
    BaseFragment<FragmentAddClassGeneralBinding>(FragmentAddClassGeneralBinding::inflate) {

    companion object {
        fun newInstance() = AddClassGeneralFragment()
    }

    private val viewModel: AddClassGeneralInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AddClassNavigator)?.updateTitle(getString(R.string.add_class_general_info))
        val uuid = (activity as AddNewClassActivity).uuid
        if (uuid > -1) {
          viewModel.updateClassDetails(uuid)
          binding.nextButton.setText(getString(R.string.add_new_user_activity_update_button))
            binding.nextButton.setOnClickListener {
                viewModel.saveClassModel()
                activity?.finish()
            }
        } else {
            binding.nextButton.setOnClickListener {
                (activity as? AddClassNavigator)?.onGeneralInfoAdded()
            }
        }

        binding.cancelButton.setOnClickListener {
            activity?.finish()
        }

        binding.inputClassName.textChanges()
            .onEach { viewModel.updateClassName(it.toString()) }
            .launchIn(lifecycleScope)

        binding.inputClassId.textChanges()
            .onEach { viewModel.updateClassId(it.toString()) }
            .launchIn(lifecycleScope)


        viewModel.classDetails.observe(viewLifecycleOwner) {
            binding.inputClassName.setText(it.name)
            binding.inputClassId.setText(it.classId)
        }

    }
//
//    override fun onResume() {
//        super.onResume()
//        viewModel.updateClassDetails()
//    }

    override fun getTitle(): String = ""

}
