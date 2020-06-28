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
import com.facecool.databinding.DialogSelectorAlertBinding

class FaceSelectorCoolDialog(
    private val body: String,
    private val btn1: String,
    private val btn2: String,
    private val btn3: String,
    private val listener: Listener
) : DialogFragment() {

    private lateinit var binding: DialogSelectorAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSelectorAlertBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertDialogTitle.text = body
        binding.btn1.text = btn1
        binding.btn2.text = btn2
        binding.btn3.text = btn3

        binding.btn1.setOnClickListener {
            listener.onBtnOne()
            dismiss()
        }

        binding.btn2.setOnClickListener {
            listener.onBtnTwo()
            dismiss()
        }

        binding.btn3.setOnClickListener {
            listener.onBtnThree()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    companion object {
        const val TAG = "FaceSelectorCoolDialog"
    }

    interface Listener {
        fun onBtnOne()
        fun onBtnTwo()
        fun onBtnThree()
    }

}