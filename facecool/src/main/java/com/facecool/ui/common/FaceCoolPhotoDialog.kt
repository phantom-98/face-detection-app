package com.facecool.ui.common

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import com.facecool.databinding.DialogPhotoAlertBinding
import com.facecool.utils.loadImageFromLocal
import com.facecool.utils.loadUriImage

class FaceCoolPhotoDialog(
    private val title: String,
    private val imageUri: Uri? = null,
    private val imagePath: String? = null,
    private val btm: Bitmap? = null,
) : DialogFragment() {

    private lateinit var binding: DialogPhotoAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPhotoAlertBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertDialogTitle.text = title

        binding.root.setOnClickListener {
            dismiss()
        }

        if (imageUri != null) {
            binding.photo.loadUriImage(imageUri)
            return
        }

        if (imagePath != null) {
            binding.photo.loadImageFromLocal(imagePath)
            return
        }

        if (btm != null) {
            binding.photo.setImageBitmap(btm)
            return
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    companion object {
        const val TAG = "FaceCoolPhotoDialog"
    }


}