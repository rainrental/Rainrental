package org.rainrental.rainrentalrfid.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.apis.data.Configuration
import org.rainrental.rainrentalrfid.apis.interceptors.SafeLoggingInterceptor
import org.rainrental.rainrentalrfid.commission.data.CommissionApi
import org.rainrental.rainrentalrfid.rainrental.data.RainRentalApi
import org.rainrental.rainrentalrfid.auth.InvitationApiService
import com.google.firebase.auth.FirebaseAuth
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_store")

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    @Named("UnifiedConfig")
    fun provideConfiguration(@ApplicationContext context: Context): Configuration {
        val res = context.resources
        val baseUrl = res.getString(R.string.base_url)
        val apiKey = res.getString(R.string.api_key)
        return Configuration(baseUrl, apiKey)
    }


    @Provides
    @Singleton
    @Named("RainRentalConfig")
    fun provideRainRentalConfiguration(@ApplicationContext context: Context): Configuration {
        val res = context.resources
        val baseUrl = res.getString(R.string.base_url_rain_rental)
        val apiKey = res.getString(R.string.api_key_rain_rental)
        return Configuration(baseUrl, apiKey)
    }



    @Provides
    @Singleton
    @Named("UnifiedHttpClient")
    fun provideAutoUpdateOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    @Named("RainRentalHttpClient")
    fun provideRainRentalOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor{chain ->
                val timeZone: String = TimeZone.getDefault().id
                val request: Request = chain
                    .request()
                    .newBuilder()
                    .addHeader("TimeZone", timeZone)
                    .build()
                chain.withConnectTimeout(20000,TimeUnit.SECONDS).withReadTimeout(20000,TimeUnit.SECONDS).proceed(request)
            }
            .build()
    }



    @Singleton
    @Provides
    @Named("UnifiedRetrofit")
    fun provideRetrofit(@Named("UnifiedHttpClient") okHttpClient: OkHttpClient, @Named("UnifiedConfig") configuration: Configuration, gson:Gson) : Retrofit {
        Log.i("Building","Building with Url: ${configuration.baseUrl}")
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(configuration.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    @Named("RainRentalRetrofit")
    fun provideAuthRetrofit(@Named("RainRentalHttpClient") okHttpClient: OkHttpClient, @Named("RainRentalConfig") configuration: Configuration, gson:Gson) : Retrofit {
        Log.i("Building","Building with Url: ${configuration.baseUrl}")
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(configuration.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }



    @Provides
    @Singleton
    fun providesCommissionApi(@Named("UnifiedRetrofit") retrofit: Retrofit) : CommissionApi {
        return retrofit.create(CommissionApi::class.java)
    }

    @Provides
    @Singleton
    fun providesRainRentalApi(@Named("RainRentalRetrofit") retrofit: Retrofit) : RainRentalApi {
        return retrofit.create(RainRentalApi::class.java)
    }

    @Provides
    @Singleton
    fun providesInvitationApiService(@ApplicationContext context: Context, gson: Gson) : InvitationApiService {
        val baseUrl = context.getString(R.string.base_url_firebase_functions)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val timeZone: String = TimeZone.getDefault().id
                val request: Request = chain
                    .request()
                    .newBuilder()
                    .addHeader("TimeZone", timeZone)
                    .build()
                chain.withConnectTimeout(20000, TimeUnit.SECONDS).withReadTimeout(20000, TimeUnit.SECONDS).proceed(request)
            }
            .build()
        
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            
        return retrofit.create(InvitationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun providesGson() : Gson{
        return GsonBuilder()
//            .registerTypeAdapter(CheckoutResponseTypeDto::class.java, checkoutResponseTypeDtoDeserializer)
//            .registerTypeAdapter(AddToInventoryTypeDto::class.java,addToInventoryTypeDtoDeserializer)
//            .registerTypeAdapter(CheckInResultTypeDto::class.java,checkInResponseTypeDtoDeserializer)
//            .registerTypeAdapter(LocalDateTime::class.java, localDateTimeDeserializer)
//            .registerTypeAdapter(LocalDateTime::class.java, localDateTimeSerializer)
            .create()
    }

    @Provides
    @Singleton
    fun provideSafeLoggingInterceptor() : SafeLoggingInterceptor {
        return SafeLoggingInterceptor()
    }

//    @Provides
//    @Singleton
//    fun provideTimeoutInterceptor() : TimeoutInterceptor{
//        return TimeoutInterceptor()
//    }


}
