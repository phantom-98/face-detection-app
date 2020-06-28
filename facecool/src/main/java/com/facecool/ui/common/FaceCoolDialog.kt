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
import com.facecool.databinding.DialogAlertBinding
import java.lang.Exception

class FaceCoolDialog() : DialogFragment() {

    private lateinit var binding: DialogAlertBinding
    private lateinit var body: String
    private lateinit var listener: Listener

    init {

    }
    constructor(body: String, listener: Listener):this(){
        initialize(body, listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAlertBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            binding.alertDialogTitle.text = body
        }catch (e: Exception){
            binding.alertDialogTitle.text = ""
        }
        binding.acceptBtn.setOnClickListener {
            dismiss()
            listener.onPositiveClicked()
        }
        binding.cancelBtn.setOnClickListener {
            dismiss()
            listener.onNegativeClicked()
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

    fun initialize(body: String, listener: Listener){
        this.body = body
        this.listener = listener
    }


    companion object {
        const val TAG = "PurchaseConfirmationDialog"

        fun newInstance(body: String, listener: Listener):FaceCoolDialog{
            val fragment = FaceCoolDialog()
            fragment.initialize(body, listener)
            return fragment
        }
    }

    interface Listener {
        fun onPositiveClicked()
        fun onNegativeClicked() { }
        fun onDismissed(){}
    }

}