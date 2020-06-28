package com.facecool.ui.reports.history

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentHistoryLogBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.gone
import com.facecool.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryLogFragment :
    BaseFragment<FragmentHistoryLogBinding>(FragmentHistoryLogBinding::inflate),
    HistoryLogAdapter.Listener {

    override fun getTitle(): String = getTitle(requireContext())

    override fun getTitle(context: Context) = context.getString(R.string.report_history_tab_title)

    @Inject
    lateinit var navigator: NavigatorContract

    private val viewModel: HistoryLogViewModel by viewModels()

    private val adapter = HistoryLogAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.historyLogRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        binding.historyLogRecyclerView.adapter = adapter

        viewModel.onUnEnrolledUserSelectedLiveData.observe(this.viewLifecycleOwner) {
            navigator.openAddNewStudentActivity(requireActivity(), it)
        }

        viewModel.onUserSelectedLiveData.observe(this.viewLifecycleOwner) {
            navigator.openStudentDetailsActivity(requireActivity(), it)
        }

        viewModel.loadingStatus.observe(this.viewLifecycleOwner) {
            when (it) {
                ProgressStatus.LOADING -> {
                    binding.historyProgress.visible()
                    binding.historyLogRecyclerView.gone()
                }
                else -> {
                    binding.historyProgress.gone()
                    binding.historyLogRecyclerView.visible()
                }
            }
        }

        viewModel.historyLogItems.observe(this.viewLifecycleOwner) {
            adapter.updateItems(it)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.latestEvents.observe(viewLifecycleOwner) {
            viewModel.processEvents(it)
        }
    }
    override fun onItemClick(item: HistoryItemModel) {
        viewModel.onItemClick(item)
    }

    override fun onLongClick(item: HistoryItemModel): Boolean {
        FaceCoolDialog.newInstance(
            getString(R.string.report_history_dialog_warning_delete_item),
            object : FaceCoolDialog.Listener {
                override fun onPositiveClicked() {
                    viewModel.delete(item)
                }
            }
        ).show(requireActivity().supportFragmentManager, FaceCoolDialog.TAG)
        return true
    }

}
