package com.facecool.ui

import androidx.appcompat.app.AppCompatActivity
import com.face.cool.common.transfer.DataTransferReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity(){

    @Inject
    lateinit var receiver: DataTransferReceiver

}
