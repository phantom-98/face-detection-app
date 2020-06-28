package com.facecool.ui.students.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.databinding.ItemErrorBinding

class ErrorAdapter : RecyclerView.Adapter<ErrorAdapter.ErrorHolder>() {

    private val errorList = mutableListOf<String>()

    fun updateData(newData: List<String>){
        errorList.clear()
        errorList.addAll(newData)
        notifyDataSetChanged()
    }

    class ErrorHolder constructor(
        private val binding: ItemErrorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(error: String) {
            binding.tvErrorText.text = error
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorHolder {
        val b = ItemErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ErrorHolder(b)
    }

    override fun getItemCount() = errorList.size

    override fun onBindViewHolder(holder: ErrorHolder, position: Int) {
        holder.bind(errorList[position])
    }

}
