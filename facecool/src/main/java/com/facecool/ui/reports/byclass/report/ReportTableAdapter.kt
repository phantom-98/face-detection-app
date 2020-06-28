package com.facecool.ui.reports.byclass.report

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.facecool.R
import com.facecool.databinding.ItemClassReportBinding
import com.facecool.databinding.ItemPhotoReportBinding
import com.facecool.utils.loadImageFromLocal
import thiha.aung.fancytable.BaseFancyTableAdapter
import kotlin.math.roundToInt

class ReportTableAdapter constructor(
    private val context: Context,
    private val data: List<List<ReportData>>,
    private val listener: Listener
) : BaseFancyTableAdapter() {

    private val widthNameColumn: Int
    private val width: Int
    private val height: Int

    init {
        width = dpToPx(75)
        height = dpToPx(45)
        widthNameColumn = dpToPx(90)
    }

    private fun dpToPx(dps: Int) = (context.resources.displayMetrics.density * dps).roundToInt()

    override fun isRowOneColumn(row: Int) = false

    private fun getEmptyView(parent: ViewGroup, className: String): TextView {
        val binding =
            ItemClassReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.gravity = Gravity.CENTER
        binding.itemData.text = className
        binding.itemData.setBackgroundColor(Color.WHITE)
        return binding.root
    }

    private fun getPhotoView(parent: ViewGroup, photo: String, id: Long): ImageView {
        val binding = ItemPhotoReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.itemPhoto.loadImageFromLocal(photo)
        binding.itemPhoto.setOnClickListener {
            listener.onPhotoViewClicked(id)
        }
        return binding.root
    }

    private fun getNameView(parent: ViewGroup, name: String): TextView {
        val binding =
            ItemClassReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.gravity = Gravity.CENTER
        binding.itemData.text = name
        binding.itemData.setBackgroundColor(Color.WHITE)
        return binding.root
    }

    private fun getDateView(parent: ViewGroup, date: String): TextView {
        val binding =
            ItemClassReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.gravity = Gravity.CENTER
        binding.itemData.text = date
        binding.itemData.setBackgroundColor(Color.WHITE)
        return binding.root
    }

    private fun getAttendanceView(parent: ViewGroup, attendance: String): TextView {
        val binding =
            ItemClassReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.gravity = Gravity.CENTER
        binding.itemData.text = attendance
        binding.itemData.setBackgroundColor(Color.WHITE)
        return binding.root
    }

    override fun getDockedColumnCount(): Int {
        return 2
    }

    override fun getRowCount(): Int {
        return data.size
    }

    override fun getColumnCount(): Int {
        return data.getOrNull(0)?.size ?: 0
    }

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup?): View? {
        parent ?: return null
        val d = data.getOrNull(row)?.getOrNull(column) ?: return null
        return when (d) {
            is ReportData.Attendance -> getAttendanceView(parent, d.value)
            is ReportData.ClassName -> getEmptyView(parent, "${d.classID} - ${d.name}")
            is ReportData.Date -> getDateView(parent, d.readableDate)
            is ReportData.StudentName -> getNameView(parent, "${d.displayStudentId} - ${d.firstName} ${d.lastName}")
            is ReportData.StudentPhoto -> getPhotoView(parent, d.localPath, d.studentId)
        }
    }

    override fun getWidth(column: Int): Int {
        return if (column == 0) height else if (column == 1) widthNameColumn else width
    }

    override fun getHeight(row: Int): Int {
        return height
    }

    override fun getItemViewType(row: Int, column: Int): Int {
        if (row == 0 && (column == 0 || column==1)) return com.facecool.ui.reports.yxl.report.ReportTableAdapter.TYPE_EMPTY
        if (row == 0) return com.facecool.ui.reports.yxl.report.ReportTableAdapter.TYPE_DATE
        if (column == 0) return com.facecool.ui.reports.yxl.report.ReportTableAdapter.TYPE_PHOTO
        if (column == 1) return com.facecool.ui.reports.yxl.report.ReportTableAdapter.TYPE_NAME
        return com.facecool.ui.reports.yxl.report.ReportTableAdapter.TYPE_ATTENDANCE
    }

    override fun getViewTypeCount(): Int {
        return 5
    }

    companion object {
        const val TYPE_EMPTY = 0
        const val TYPE_NAME = 1
        const val TYPE_DATE = 2
        const val TYPE_ATTENDANCE = 3
        const val TYPE_PHOTO = 4
    }

    interface Listener {
        fun onPhotoViewClicked(id: Long)
    }
}
