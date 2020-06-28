package com.facecool.ui.settings.administrators

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.face.cool.databasa.administrators.AdministratorEntity
import com.face.cool.manualsync.stringToBitMap
import com.facecool.databinding.ItemStudentBinding

class AdministratorListAdapter(private val listener: Listener) :
    RecyclerView.Adapter<AdministratorListAdapter.AdminViewHolder>() {

    private val admins = mutableListOf<AdministratorEntity>()

    fun setAdmins(admins: List<AdministratorEntity>) {
        this.admins.clear()
        this.admins.addAll(admins)
        notifyDataSetChanged()
    }

    inner class AdminViewHolder(
        private val binding: ItemStudentBinding,
        private val listener: Listener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(admin: AdministratorEntity) {

            binding.studentImage.setImageBitmap(admin.base64Image?.stringToBitMap())

            binding.studentName.text = "${admin.name} ${admin.lastName}"
            binding.root.setOnClickListener {
                listener.onAdminClicked(admin)
            }
            binding.btnRemoveStudent.setOnClickListener {
                listener.removeAdmin(admin)
            }
        }
    }

    interface Listener {
        fun onAdminClicked(student: AdministratorEntity)
        fun removeAdmin(student: AdministratorEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminViewHolder(binding, listener)
    }

    override fun getItemCount() = admins.size

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        holder.bind(admins[position])
    }

}
