package com.facecool.ui.classes.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.facecool.databinding.ActivityClassDetailsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import com.facecool.ui.students.common.ViewWithTitle
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClassDetailsActivity : BaseActivity() {

    companion object {
        private const val CLASS_ID = "CLASS_ID"

        @JvmStatic
        fun start(context: Context, classID: Long) {
            val starter = Intent(context, ClassDetailsActivity::class.java)
                .putExtra(CLASS_ID, classID)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    private lateinit var binding: ActivityClassDetailsBinding

    private val viewModel: ClassDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val classId = intent.getLongExtra(CLASS_ID, -1)
        val classDetailsAdapter = ClassDetailsPageAdapter(this, classId)

        setSupportActionBar(binding.toolbarContainer.toolbar)

        viewModel.getTitle(classId)

        viewModel.title.observe(this){
            supportActionBar?.title = it
        }

        binding.toolbarContainer.ivCloseJourney?.visibility = View.VISIBLE
        binding.toolbarContainer.ivCloseJourney?.setOnClickListener {
            navigator.closeJourney(this)
        }

        binding.classDetailsViewPager.adapter = classDetailsAdapter
        TabLayoutMediator(
            binding.classDetailsTabLayout,
            binding.classDetailsViewPager
        ) { tab, position ->
            tab.text =
                (classDetailsAdapter.fragments.get(position) as? ViewWithTitle?)?.getTitle(this)
        }.attach()

    }

}
