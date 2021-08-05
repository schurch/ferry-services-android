package com.stefanchurch.ferryservices

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stefanchurch.ferryservices.models.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ServicesRepository(private val context: Context) {

    companion object {
        @Volatile private var instance: ServicesRepository? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: ServicesRepository(context).also { instance = it }
            }

//        private val baseURL = URL("http://10.0.2.2:3001")
        private val baseURL = URL("https://scottishferryapp.com")
//        private val baseURL = URL("http://192.168.86.25:3001")
    }

    suspend fun getService(serviceID: Int) = suspendCoroutine<Service> { cont ->
        val url = URL(baseURL, "/api/services/$serviceID")
        val request = StringRequest(Request.Method.GET, url.toString(), { response ->
            val format = Json { ignoreUnknownKeys = true }
            val service = format.decodeFromString<Service>(response)
            cont.resume(service)
        }, { error ->
            cont.resumeWithException(error)
        })
        addToRequestQueue(request)
    }

    suspend fun getServices() = suspendCoroutine<Array<Service>> { cont ->
        val url = URL(baseURL, "/api/services")
        val request = StringRequest(Request.Method.GET, url.toString(), { response ->
            cont.resume(responseToServices(response))
        }, { error ->
            cont.resumeWithException(error)
        })
        addToRequestQueue(request)
    }

    suspend fun updateInstallation(installationID: UUID, deviceToken: String) = suspendCoroutine<Array<Service>> { cont ->
        val url = URL(baseURL, "/api/installations/$installationID")
        val body = CreateInstallationBody(deviceToken)

        val request = object : StringRequest(Request.Method.POST, url.toString(), { response ->
            cont.resume(responseToServices(response))
        }, { error ->
            cont.resumeWithException(error)
        }) {
            override fun getBody(): ByteArray {
                return Json.encodeToString(body).toByteArray()
            }
        }

        addToRequestQueue(request)
    }

    suspend fun getSubscribedServices(installationID: UUID) = suspendCoroutine<Array<Service>> { cont ->
        val url = URL(baseURL, "/api/installations/$installationID/services")

        val request = StringRequest(Request.Method.GET, url.toString(), { response ->
            cont.resume(responseToServices(response))
        }, { error ->
            cont.resumeWithException(error)
        })
        addToRequestQueue(request)
    }

    suspend fun addService(installationID: UUID, serviceID: Int) = suspendCoroutine<Array<Service>> { cont ->
        val url = URL(baseURL, "/api/installations/$installationID/services")
        val body = CreateInstallationServiceBody(serviceID)

        val request = object : StringRequest(Request.Method.POST, url.toString(), { response ->
            cont.resume(responseToServices(response))
        }, { error ->
            cont.resumeWithException(error)
        }) {
            override fun getBody(): ByteArray {
                return Json.encodeToString(body).toByteArray()
            }
        }

        addToRequestQueue(request)
    }

    suspend fun removeService(installationID: UUID, serviceID: Int) = suspendCoroutine<Array<Service>> { cont ->
        val url = URL(baseURL, "/api/installations/$installationID/services/$serviceID")

        val request = StringRequest(Request.Method.DELETE, url.toString(), { response ->
            cont.resume(responseToServices(response))
        }, { error ->
            cont.resumeWithException(error)
        })

        addToRequestQueue(request)
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    private fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}

private fun responseToServices(response: String) : Array<Service> {
    val format = Json { ignoreUnknownKeys = true }
    val services = format
        .decodeFromString<Array<Service>>(response)
        .sortedBy { it.sortOrder }

    return services.toTypedArray()
}

@Serializable
private data class CreateInstallationBody(
    @SerialName("device_token") val deviceToken: String,
    @SerialName("device_type") val deviceType: String = "Android",
)

@Serializable
private data class CreateInstallationServiceBody(
    @SerialName("service_id") val serviceID: Int,
)
