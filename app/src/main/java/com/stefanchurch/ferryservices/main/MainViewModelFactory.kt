package com.stefanchurch.ferryservices.main

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.stefanchurch.ferryservices.ServicesRepository
import com.stefanchurch.ferryservices.Preferences
import com.stefanchurch.ferryservices.models.Service

class MainViewModelFactory(
    private val defaultServices: Array<Service>,
    private val servicesRepository: ServicesRepository,
    private val preferences: Preferences,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return MainViewModel(defaultServices, servicesRepository, preferences) as T
    }

}