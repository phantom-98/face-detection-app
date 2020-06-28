package com.facecool.ui.classes.add.time

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemWeekDayBinding

class WeekDaysAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<WeekDaysAdapter.ViewHolder>() {

    private val days = mutableListOf<DayModel>()

    fun setDays(days: List<DayModel>) {
        this.days.clear()
        this.days.addAll(days)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemWeekDayBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: DayModel) {
            binding.tvDay.text = day.day
            if (day.isSelected) {
                binding.rootCard.setCardBackgroundColor(Color.parseColor("#cf203c"))
            } else {
                binding.rootCard.setCardBackgroundColor(Color.parseColor("#d1d9ed"))
            }
            binding.root.setOnClickListener {
                listener.onDaySelected(day)
            }
        }
    }

    interface Listener {
        fun onDaySelected(day: DayModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWeekDayBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener
        )
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position])
    }

}