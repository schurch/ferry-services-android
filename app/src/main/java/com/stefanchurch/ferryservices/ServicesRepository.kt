package com.stefanchurch.ferryservices

import android.content.Context
import android.net.Uri
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stefanchurch.ferryservices.models.Service
import com.stefanchurch.ferryservices.models.Vessel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
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

        private const val host = "scottishferryapp.com"
//        private const val host = "10.0.2.2:3001"
//        private const val host = "192.168.86.31:3001"
        private val baseURL = URL("https://$host")
    }

    suspend fun getService(serviceID: Int, date: Long) = suspendCoroutine<Service> { cont ->
        val departuresDate = Instant.ofEpochMilli(date)
            .atZone(Calendar.getInstance().timeZone.toZoneId())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("uuuu-MM-dd"))
        val url = Uri.Builder()
            .scheme("https")
            .authority(host)
            .appendPath("api")
            .appendPath("services")
            .appendPath(serviceID.toString())
            .appendQueryParameter("departuresDate", departuresDate)
            .build()
            .toString()
        val request = StringRequest(Request.Method.GET, url, { response ->
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

    suspend fun getVessels() = suspendCoroutine<Array<Vessel>> { cont ->
        val url = URL(baseURL, "/api/vessels")
        val request = StringRequest(Request.Method.GET, url.toString(), { response ->
            val format = Json { ignoreUnknownKeys = true }
            val vessels = format
                .decodeFromString<Array<Vessel>>(response)
            cont.resume(vessels)
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
    return format.decodeFromString(response)
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
