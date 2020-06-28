package com.facecool.ui.classes.add.time

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ItemCustomDayBinding
import com.facecool.ui.classes.common.SharedAddNewClassData
import com.facecool.ui.common.SpinnerSelectionListener
import com.facecool.utils.openTimeSelector

class CustomDaysAdapter(private val listener: Listener) :
    RecyclerView.Adapter<CustomDaysAdapter.ViewHolder>() {

    private val days = mutableListOf<CustomDayModel>()

    fun setDays(days: List<CustomDayModel>) {
        this.days.clear()
        this.days.addAll(days)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemCustomDayBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: CustomDayModel) {
            val days = listOf(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
            )
            binding.ibAdd.setOnClickListener {
                listener.onItemAdd(day)
            }
            binding.ibRemove.setOnClickListener {
                listener.onItemRemove(day)
            }
            binding.spinnerCustomDay.onItemSelectedListener = object : SpinnerSelectionListener() {
                override fun onItemSelected(position: Int) {
                    day.day = days[position]
                }
            }
            binding.etStartTime.setOnClickListener {
                itemView.context.openTimeSelector() {
                    day.startTime = it % (24*3600*1000)
                }
            }
            binding.etEndTime.setOnClickListener {
                itemView.context.openTimeSelector() {
                    day.endTime = it % (24*3600*1000)
                }
            }
        }
    }

    interface Listener {
        fun onDataChanged(day: CustomDayModel)
        fun onItemAdd(item: CustomDayModel)
        fun onItemRemove(item: CustomDayModel)
//        fun updateCustomStartTime(time: Long)
//        fun updateCustomEndTime(time: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCustomDayBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener
        )
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position])
    }

}