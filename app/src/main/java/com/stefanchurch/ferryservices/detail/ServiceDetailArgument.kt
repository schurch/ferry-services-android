package com.stefanchurch.ferryservices.detail

import android.os.Parcel
import android.os.Parcelable
import com.stefanchurch.ferryservices.models.Service
import kotlinx.serialization.*

@Serializable
data class ServiceDetailArgument(
    val serviceID: Int,
    val service: Service?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Service::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(serviceID)
        parcel.writeParcelable(service, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceDetailArgument> {
        override fun createFromParcel(parcel: Parcel): ServiceDetailArgument {
            return ServiceDetailArgument(parcel)
        }

        override fun newArray(size: Int): Array<ServiceDetailArgument?> {
            return arrayOfNulls(size)
        }
    }
}
