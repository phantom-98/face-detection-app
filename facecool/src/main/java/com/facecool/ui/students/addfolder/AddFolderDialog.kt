package com.facecool.ui.students.addfolder

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.facecool.R
import com.facecool.databinding.DialogAddFolderAlertBinding
import com.facecool.utils.gone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFolderDialog(private val items: List<Uri>, private val listener: Listener) :
    DialogFragment() {

    private lateinit var binding: DialogAddFolderAlertBinding

    private val viewModel by viewModels<EnrollmentDialogViewModel>()

    private var follow = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddFolderAlertBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getExistingData()

        isCancelable = false
        binding.btnCancel.setOnClickListener {
            viewModel.closeDetector()
            listener.onDone()
            dismiss()
        }

        val adapter = EnrollmentAdapter()

        binding.rvEnrollList.layoutManager = GridLayoutManager(this.context, 2)
        binding.rvEnrollList.adapter = adapter

        viewModel.updateUrls(items)

        binding.btnStart.setOnClickListener {
            if (binding.btnStart.text == getString(R.string.add_new_user_activity_resume_button) || binding.btnStart.text == getString(R.string.add_new_user_activity_start_button)){
                binding.btnStart.text = getString(R.string.add_new_user_activity_pause_button)
                viewModel.start()
            } else {
                viewModel.stop()
                binding.btnStart.text = getString(R.string.add_new_user_activity_resume_button)
            }
        }
        viewModel.isFinished.observe(this){
            it?.let {
                if (it) binding.btnStart.gone()
            }
        }


        binding.checkFollow.setOnCheckedChangeListener { buttonView, isChecked ->
            follow = isChecked
        }

        viewModel.treashold.observe(this) {
            binding.tvInfoTreashold.text = it
        }

        viewModel.enrollmentItems.distinctUntilChanged().observe(this) {
            adapter.updateItems(it)
        }

        viewModel.scrollTo.observe(this) {
            if (follow) {
                binding.rvEnrollList.smoothScrollToPosition(it)
            }
        }

        viewModel.progressInformation.observe(this) {
            binding.infoText.text = it
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    companion object {
        const val TAG = "AddFolderDialog"
    }

    interface Listener {
        fun onDone()
    }

}