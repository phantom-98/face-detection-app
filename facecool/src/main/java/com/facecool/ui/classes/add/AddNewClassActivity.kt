package com.facecool.ui.classes.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.facecool.databinding.ActivityAddNewClassBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.navigation.addFragment
import com.facecool.ui.BaseActivity
import com.facecool.ui.classes.add.general.AddClassGeneralFragment
import com.facecool.ui.classes.add.students.AddClassSelectStudentsFragment
import com.facecool.ui.classes.add.time.AddClassAddTimeFragment
import com.facecool.ui.classes.common.SharedAddNewClassData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddNewClassActivity : BaseActivity(), AddClassNavigator {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, AddNewClassActivity::class.java)
            context.startActivity(starter)
        }
        @JvmStatic
        fun start(context: Context, classId: Long){
            val starter = Intent(context, AddNewClassActivity::class.java)
            starter.putExtra("UUID", classId)
            context.startActivity(starter)
        }
    }

    private val viewModel: AddNewClassViewModel by viewModels()
    private lateinit var binding: ActivityAddNewClassBinding

    @Inject
    lateinit var navigator: NavigatorContract
    var uuid: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewClassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.genericToolbarContainer.toolbar)

        uuid = intent?.extras?.getLong("UUID") ?: -1

        SharedAddNewClassData.clear()
        addFragment(AddClassGeneralFragment.newInstance())

    }

    override fun onGeneralInfoAdded() {
        addFragment(AddClassSelectStudentsFragment.newInstance(), true)
    }

    override fun onStudentsAdded() {
        addFragment(AddClassAddTimeFragment.newInstance(), true)
    }

    override fun onOccurrenceAdded(classId: Long) {
        navigator.openClassDetailsActivity(this, classId)
        finish()
    }

    override fun updateTitle(title: String) {
        supportActionBar?.title = title
    }

}
