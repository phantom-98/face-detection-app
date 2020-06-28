package com.facecool.di.module

import com.face.cool.common.transfer.DataTransferSender
import com.facecool.navigation.Navigator
import com.facecool.navigation.NavigatorContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent

@InstallIn(ActivityComponent::class, FragmentComponent::class)
@Module
object ViewModule {

    @Provides
    fun provideNavigator(sender: DataTransferSender): NavigatorContract = Navigator(sender)

}
