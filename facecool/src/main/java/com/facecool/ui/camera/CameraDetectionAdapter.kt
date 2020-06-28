package com.facecool.ui.camera

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ItemSmallCameraDetectionBinding
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.loadImageFromLocal

class CameraDetectionAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<EventModel>()

    fun setItems(items: List<EventModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class SmallViewHolder(
        private val binding: ItemSmallCameraDetectionBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EventModel) {
            when (item.enrollmentStatus) {
                EnrollmentStatus.ENROLLED -> {
                    binding.card.setCardBackgroundColor(Color.GREEN)
                    binding.tvUserName.setTextColor(Color.GREEN)
                    binding.tvUserName.text = item.userName
                }
                EnrollmentStatus.REJECTED,
                EnrollmentStatus.UNKNOWN -> {
                    binding.card.setCardBackgroundColor(Color.WHITE)
                    binding.tvUserName.setTextColor(Color.BLACK)
                    binding.tvUserName.text = itemView.context.getString(R.string.camera_detect_unknown_user_name)
                }
            }
            binding.root.setOnClickListener {
                listener.onEventItemClicked(item)
            }
            binding.imageCameraDetection.loadImageFromLocal(item.imagePathName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SmallViewHolder(
            ItemSmallCameraDetectionBinding.inflate(
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
        fun onEventItemClicked(item: EventModel)
    }

}
