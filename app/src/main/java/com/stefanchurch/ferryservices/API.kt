package com.stefanchurch.ferryservices

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stefanchurch.ferryservices.models.Service
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class API(private val context: Context) {

    companion object {
        @Volatile private var instance: API? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: API(context).also { instance = it }
            }
    }

    suspend fun getServices() = suspendCoroutine<Array<Service>> { cont ->
        val url = "http://scottishferryapp.com/api/services"
        val request = StringRequest(Request.Method.GET, url, { response ->
            val services = Json.decodeFromString<Array<Service>>(response)
            services.sortBy { it.sortOrder }
            cont.resume(services)
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