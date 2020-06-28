package com.facecool.ui.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import com.facecool.databinding.DialogSettingsSelectAlertBinding

class ExportDataSelectionDialog(private val body: String, private val listener: Listener) :
    DialogFragment() {

    private lateinit var binding: DialogSettingsSelectAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSettingsSelectAlertBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertDialogTitle.text = body
        binding.acceptBtn.setOnClickListener {
            listener.onPositiveClicked(
                Selection(
                    binding.students.isChecked,
                    binding.detections.isChecked,
                    binding.classes.isChecked,
                    binding.classes.isChecked,
                )
            )
            dismiss()
        }

        binding.all.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.students.isChecked = isChecked
            binding.detections.isChecked = isChecked
            binding.classes.isChecked = isChecked
        }

        binding.students.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.all.isChecked = binding.students.isChecked && binding.detections.isChecked && binding.classes.isChecked
        }
        binding.detections.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.all.isChecked = binding.students.isChecked && binding.detections.isChecked && binding.classes.isChecked
        }
        binding.classes.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.all.isChecked = binding.students.isChecked && binding.detections.isChecked && binding.classes.isChecked
        }

        binding.cancelBtn.setOnClickListener {
            listener.onNegativeClicked()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    companion object {
        const val TAG = "ExportDataSelectionDialog"
    }

    interface Listener {
        fun onPositiveClicked(data: Selection)
        fun onNegativeClicked() {
            // do nothing
        }
    }

    data class Selection(
        val saveUsers: Boolean = false,
        val saveEvents: Boolean = false,
        val saveClasses: Boolean = false,
        val saveLessons: Boolean = false,
    )


}