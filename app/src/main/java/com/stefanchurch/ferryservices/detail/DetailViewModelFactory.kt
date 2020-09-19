package com.stefanchurch.ferryservices.detail

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.stefanchurch.ferryservices.API
import com.stefanchurch.ferryservices.Preferences
import com.stefanchurch.ferryservices.models.Service

class DetailViewModelFactory(
    val service: Service,
    val api: API,
    val preferences: Preferences,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return DetailViewModel(service, api, preferences) as T
    }

}