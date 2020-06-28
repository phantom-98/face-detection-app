package com.facecool.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import com.facecool.databinding.DialogEditBinding

class FaceCoolEditFieldDialog<in T>(
    private val body: String,
    private val existingData: String?,
    private val code: T,
    private val listener: Listener<T>,
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
        binding.alertDialogTitle.text = body
        binding.etField.setText(existingData)
        binding.etField.requestFocus()
        binding.acceptBtn.setOnClickListener {
            listener.onPositiveClicked(binding.etField.text?.toString(), code)
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

    companion object {
        const val TAG = "PurchaseConfirmationDialog"
    }

    interface Listener<R> {
        fun onPositiveClicked(newData: String?, code: R)
        fun onNegativeClicked() {
            // do nothing
        }
    }

}