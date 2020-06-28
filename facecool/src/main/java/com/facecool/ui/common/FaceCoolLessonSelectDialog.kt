package com.facecool.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.databinding.DialogLessonSelectBinding
import com.facecool.ui.classes.add.time.LessonModel

class FaceCoolLessonSelectDialog (
    private val lessons: List<LessonModel>,
    private val listener: FaceCoolLessonSelectAdapter.Listener
) : DialogFragment() {

    private lateinit var binding: DialogLessonSelectBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogLessonSelectBinding.inflate(inflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLessonList.layoutManager = LinearLayoutManager(view.context)
        val adapter = FaceCoolLessonSelectAdapter(this, listener)
        adapter.setItems(lessons)
        binding.rvLessonList.adapter = adapter

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener.onDismissDialog()
    }

    companion object {
        const val TAG = "FaceCoolLessonSelectDialog"
    }


}