package com.facecool.ui.settings.administrators

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.face.cool.databasa.administrators.AdministratorEntity
import com.facecool.R
import com.facecool.databinding.FragmentStudentsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.utils.gone
import com.facecool.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AdministratorsFragment : BaseFragment<FragmentStudentsBinding>(FragmentStudentsBinding::inflate),
        AdministratorListAdapter.Listener{

        @Inject
        lateinit var navigator: NavigatorContract

        private val viewModel: AdministratorsViewModel by viewModels()
        private val adapter = AdministratorListAdapter(this)

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            activity?.let {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.toggleEnrolled.gone()

            binding.rejectedList.gone()

            binding.studentsList.layoutManager = LinearLayoutManager(requireContext())
            binding.studentsList.adapter = adapter

            binding.searchStudents.hint = getString(R.string.search_for_admin)
            binding.searchStudents.textChanges()
                .onEach { viewModel.searchForAdmin(it.toString()) }
                .launchIn(lifecycleScope)

            viewModel.admins.observe(viewLifecycleOwner) {
                adapter.setAdmins(it)
            }

            binding.buttonAddNewStudent.setOnClickListener {
                //add new admin
                navigator.openAddNewAdminActivity(requireActivity())
            }

        }

        override fun onResume() {
            super.onResume()
            viewModel.getAdmins()
        }

        override fun onAdminClicked(admin: AdministratorEntity) {
            viewModel.onAdminClicked(admin)
            admin.uid?.let {

                navigator.openAdminDetailsActivity(requireActivity(), it)
            }
        }

        override fun removeAdmin(admin: AdministratorEntity) {
            FaceCoolDialog.newInstance(
                getString(R.string.delete_confirm_message, admin.name),
                object : FaceCoolDialog.Listener {
                    override fun onPositiveClicked() {
                        viewModel.removeAdmin(admin)
                    }
                }).show(childFragmentManager, FaceCoolDialog.TAG)
        }

        override fun getTitle(): String = getTitle(requireActivity())

        override fun getTitle(context: Context) = context.getString(R.string.admin_title)


    }
