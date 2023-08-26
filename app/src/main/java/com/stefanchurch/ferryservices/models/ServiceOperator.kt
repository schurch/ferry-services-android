package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ServiceOperator(
    val id: Int,
    val name: String,
    val website: String? = null,
    val localNumber: String? = null,
    val overseasNumber: String? = null,
    val email: String? = null,
    val x: String? = null,
    val facebook: String? = null
) : Parcelable
