package com.stefanchurch.ferryservicesandroid.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stefanchurch.ferryservicesandroid.BuildConfig
import com.stefanchurch.ferryservicesandroid.data.local.ServiceCache
import com.stefanchurch.ferryservicesandroid.data.network.FerryApi
import com.stefanchurch.ferryservicesandroid.data.preferences.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideFerryApi(okHttpClient: OkHttpClient, json: Json): FerryApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FerryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.filesDir.resolve("app-preferences.preferences_pb") },
        )
    }

    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>,
    ): AppPreferences = AppPreferences(context, dataStore)

    @Provides
    @Singleton
    fun provideServiceCache(@ApplicationContext context: Context, json: Json): ServiceCache {
        return ServiceCache(context = context, json = json)
    }
}
