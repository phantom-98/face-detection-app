package com.facecool.ui.classes.details.ocurrences

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentClassesOcurencesBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.common.FaceCoolDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClassOccurrencesFragment :
    BaseFragment<FragmentClassesOcurencesBinding>(FragmentClassesOcurencesBinding::inflate),
    ClassLessonAdapter.Listener {

    companion object {

        private const val CLASS_ID = "CLASS_ID"

        fun newInstance(classId: Long): ClassOccurrencesFragment {
            val args = Bundle()
            args.putLong(CLASS_ID, classId)
            val fragment = ClassOccurrencesFragment()
            fragment.arguments = args
            return fragment
        }

    }

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.lessons_title)

    private val viewModel: ClassesOccurrencesViewModel by viewModels()

    private val classLessonAdapter = ClassLessonAdapter(this)

    private var classID: Long? = null

    @Inject
    lateinit var navigator: NavigatorContract

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
    override fun onDestroyView() {
        super.onDestroyView()
//        activity?.let {
//            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.classesListOccurrences.layoutManager = LinearLayoutManager(this.requireContext())
        binding.classesListOccurrences.adapter = classLessonAdapter

        classID = arguments?.getLong(CLASS_ID) ?: -1

        viewModel.lessons.observe(this.viewLifecycleOwner) {
            classLessonAdapter.updateData(it)
        }

        binding.buttonAddNewOccurrence.setOnClickListener {
            classID?.let {
                navigator.openAddNewOccurrenceActivity(this.requireActivity(), it)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        classID?.let {
            viewModel.getClassData(it)
        }
    }

    override fun onItemSelected(occurrence: LessonModel) {
//        viewModel.onClassOccurrenceSelected(occurrence)
        navigator.openClassOccurrenceDetailsActivity(this.requireActivity(), occurrence)
    }

    override fun deleteOccurrence(occurrence: LessonModel) {
        FaceCoolDialog.newInstance(
            getString(R.string.lesson_delete),
            object : FaceCoolDialog.Listener {
                override fun onPositiveClicked() {
                    viewModel.deleteOccurrence(occurrence)
                }
            }
        ).show(parentFragmentManager, FaceCoolDialog.TAG)
    }

    override fun editOccurrence(occurrence: LessonModel) {
        navigator.openAddNewOccurrenceActivity(requireActivity(), occurrence.lessonClassId, occurrence.lessonDdId?:return)
    }

}