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
import com.facecool.databinding.DialogPhotoChooserAlertBinding

class FaceCoolPhotoChooserDialog(private val body: String, private val listener: Listener) : DialogFragment() {

    private lateinit var binding: DialogPhotoChooserAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPhotoChooserAlertBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertDialogTitle.text = body
        binding.btnCamera.setOnClickListener {
            listener.onCamera()
            dismiss()
        }
        binding.btnFile.setOnClickListener {
            listener.onFile()
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
        const val TAG = "FaceCoolPhotoChooserDialog"
    }

    interface Listener {
        fun onCamera()
        fun onFile()
        fun onNegativeClicked() {
            // do nothing
        }
    }

}