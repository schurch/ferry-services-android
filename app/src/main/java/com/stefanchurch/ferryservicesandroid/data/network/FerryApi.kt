package com.stefanchurch.ferryservicesandroid.data.network

import com.stefanchurch.ferryservicesandroid.data.model.PushStatus
import com.stefanchurch.ferryservicesandroid.data.model.Service
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID
import kotlinx.serialization.Serializable

interface FerryApi {
    @GET("api/services/")
    suspend fun fetchServices(): List<Service>

    @GET("api/services/{serviceId}")
    suspend fun fetchService(
        @Path("serviceId") serviceId: Int,
        @Query("departuresDate") departuresDate: String,
    ): Service

    @GET("api/services/{serviceId}")
    suspend fun fetchService(@Path("serviceId") serviceId: Int): Service

    @POST("api/installations/{installationId}")
    suspend fun createInstallation(
        @Path("installationId") installationId: UUID,
        @Body body: CreateInstallationBody,
    ): List<Service>

    @POST("api/installations/{installationId}/services")
    suspend fun addService(
        @Path("installationId") installationId: UUID,
        @Body body: CreateInstallationServiceBody,
    ): List<Service>

    @DELETE("api/installations/{installationId}/services/{serviceId}")
    suspend fun removeService(
        @Path("installationId") installationId: UUID,
        @Path("serviceId") serviceId: Int,
    ): List<Service>

    @GET("api/installations/{installationId}/push-status")
    suspend fun getPushStatus(@Path("installationId") installationId: UUID): PushStatus

    @POST("api/installations/{installationId}/push-status")
    suspend fun updatePushStatus(
        @Path("installationId") installationId: UUID,
        @Body body: PushStatus,
    ): PushStatus
}

@Serializable
data class CreateInstallationBody(
    val deviceToken: String,
    val deviceType: String = "ANDROID",
)

@Serializable
data class CreateInstallationServiceBody(
    val serviceID: Int,
)
