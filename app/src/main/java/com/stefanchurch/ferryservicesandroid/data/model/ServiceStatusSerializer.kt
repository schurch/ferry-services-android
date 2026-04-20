package com.stefanchurch.ferryservicesandroid.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ServiceStatusSerializer : KSerializer<ServiceStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ServiceStatus", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): ServiceStatus {
        return when (decoder.decodeInt()) {
            0 -> ServiceStatus.NORMAL
            1 -> ServiceStatus.DISRUPTED
            2 -> ServiceStatus.CANCELLED
            else -> ServiceStatus.UNKNOWN
        }
    }

    override fun serialize(encoder: Encoder, value: ServiceStatus) {
        val encoded = when (value) {
            ServiceStatus.NORMAL -> 0
            ServiceStatus.DISRUPTED -> 1
            ServiceStatus.CANCELLED -> 2
            ServiceStatus.UNKNOWN -> -99
        }
        encoder.encodeInt(encoded)
    }
}
