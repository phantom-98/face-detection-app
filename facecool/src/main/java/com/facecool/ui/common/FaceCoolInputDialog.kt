package com.facecool.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import com.facecool.databinding.DialogEditBinding

class FaceCoolInputDialog(
    private val title: String,
    private val existingData: String?,
    private val pinMode: Boolean,
    private val listener: Listener,
) : DialogFragment() {

    private lateinit var binding: DialogEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogEditBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertDialogTitle.text = title
        binding.etField.setText(existingData ?: "")
        if(pinMode) binding.etField.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        binding.etField.requestFocus()
        binding.acceptBtn.setOnClickListener {
            listener.onPositiveClicked(binding.etField.text?.toString())
            dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        listener.onDismissed()
    }

    companion object {
        const val TAG = "FaceCoolInputDialog"
    }

    interface Listener {
        fun onPositiveClicked(newData: String?)
        fun onNegativeClicked(){}
        fun onDismissed(){}
    }

}