package com.facecool.ui.profile

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.databinding.FragmentProfileBinding
import com.facecool.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate),
    ProfileItemAdapter.Listener {

    private val viewModel: ProfileViewModel by viewModels()
    private val adapter = ProfileItemAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProfileInformation()

        binding.itemDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.itemDetails.adapter = adapter

        viewModel.profileInformation.observe(viewLifecycleOwner) {
            adapter.updateItems(it)
        }


    }

    override fun getTitle(): String = ""

    override fun onItemClicked(item: ProfileItemModel) {

    }

}
