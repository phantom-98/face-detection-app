package com.facecool.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemLessonBinding
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.utils.getReadableDate

class FaceCoolLessonSelectAdapter (
    private val dialog: FaceCoolLessonSelectDialog,
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<LessonModel>()

    fun setItems(items: List<LessonModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class SmallViewHolder(
        private val binding: ItemLessonBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LessonModel) {
            binding.tvLessonName.text = item.lessonName
            binding.tvLessonDate.text = item.date?.getReadableDate(pattern = "MMM dd")
            binding.tvLessonStart.text = item.startLesson?.getReadableDate(pattern = "HH:mm")
            binding.tvLessonEnd.text = item.endLesson?.getReadableDate(pattern = "HH:mm")
            binding.btnStart.setOnClickListener {
                listener.onStartButtonClicked(item)
                dialog.dismiss()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SmallViewHolder(
            ItemLessonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? SmallViewHolder)?.bind(items[position])
    }

    override fun getItemCount() = items.size

    interface Listener {
        fun onStartButtonClicked(item: LessonModel)
        fun onDismissDialog(){}
    }

}
