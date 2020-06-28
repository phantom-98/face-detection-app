package com.facecool.ui.classes.details.ocurrences

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemClassOccurrenceBinding
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.utils.getReadableDate

class ClassLessonAdapter(private val listener: Listener) :
    RecyclerView.Adapter<ClassLessonAdapter.ViewHolder>() {

    private val itemList = mutableListOf<LessonModel>()

    inner class ViewHolder(
        private val binding: ItemClassOccurrenceBinding,
        private val listener: Listener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LessonModel) {
            binding.tvLessonClassTitle.text = item.lessonName
            binding.tvLessonStatus.text = item.status
            binding.tvLessonDate.text = item.startLesson?.getReadableDate(pattern = "MMM  dd \nHH:mm-") + item.endLesson?.getReadableDate(pattern = "HH:mm")
            binding.root.setOnClickListener { listener.onItemSelected(item) }
            binding.btnEdit.setOnClickListener { listener.editOccurrence(item) }
            binding.btnRemove.setOnClickListener { listener.deleteOccurrence(item) }
        }
    }

    interface Listener {
        fun onItemSelected(occurrence: LessonModel)
        fun deleteOccurrence(occurrence: LessonModel)
        fun editOccurrence(occurrence: LessonModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemClassOccurrenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    fun updateData(newData: List<LessonModel>) {
        itemList.clear()
        itemList.addAll(newData)
        notifyDataSetChanged()
    }

}