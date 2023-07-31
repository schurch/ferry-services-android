package com.stefanchurch.ferryservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.util.Base64

@Parcelize
@Serializable
data class ScheduledDeparture(
    @Serializable(InstantSerializer::class)
    val departure: Instant,

    @Serializable(InstantSerializer::class)
    val arrival: Instant,

    val destination: Location
) : Parcelable
