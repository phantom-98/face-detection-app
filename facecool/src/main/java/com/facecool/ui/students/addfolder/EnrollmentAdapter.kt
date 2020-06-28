package com.facecool.ui.students.addfolder

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.facecool.databinding.ItemEnrollmentViewBinding
import com.facecool.ui.common.unknownUserImage
import com.facecool.utils.gone
import com.facecool.utils.loadImageFromLocal
import com.facecool.utils.loadUriImage
import com.facecool.utils.visible

class EnrollmentAdapter : RecyclerView.Adapter<EnrollmentAdapter.EnrollmentViewHolder>() {

    private val items = mutableListOf<EnrollmentModel>()

    class EnrollmentViewHolder(private val binding: ItemEnrollmentViewBinding) :
        ViewHolder(binding.root) {

        fun bind(item: EnrollmentModel) {
            binding.progress.gone()
            when (item) {
                is EnrollmentModel.Enrolled -> {
//                    Log.d("TEST_LOG", " bind item  Enrolled ")
                    binding.ivUserImage.setImageBitmap(null)
                    binding.ivUserImage.destroyDrawingCache()
                    binding.ivUserImage.loadImageFromLocal(item.imagePath)
                    binding.tvName.text = item.name + " (Q:" + item.imageQuality + ")"
                    binding.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context, com.design.R.color.enr_item_enrolled
                        )
                    )
                }

                is EnrollmentModel.Waiting -> {
                    binding.ivUserImage.loadUriImage(item.imageUri)
                    binding.tvName.text = item.detectedName
                    binding.tvName.setTextColor(Color.DKGRAY)
                    binding.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context, com.design.R.color.enr_item_queue
                        )
                    )
                    if (item.isProgress) {
                        binding.progress.visible()
                    }
                }

                is EnrollmentModel.Error -> {
                    binding.tvName.text = item.reason + " (Q:" + item.imageQuality + ")"
                    binding.tvName.setTextColor(Color.WHITE)
                    binding.cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context, com.design.R.color.enr_item_error
                        )
                    )

                    binding.ivUserImage.setImageBitmap(null)
                    binding.ivUserImage.destroyDrawingCache()
                    if (item.imageUri == null) {
                        binding.ivUserImage.setImageBitmap(unknownUserImage(binding.root.context))
                    } else {
                        item.imageUri?.let {
                            binding.ivUserImage.loadUriImage(it)
                        }
                    }
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        return EnrollmentViewHolder(
            ItemEnrollmentViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateItems(newItems: List<EnrollmentModel>?) {
        items.clear()
        if (newItems != null) {
            items.addAll(newItems)
        }
        notifyDataSetChanged()
    }
}