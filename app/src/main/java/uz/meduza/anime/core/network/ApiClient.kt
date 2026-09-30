package uz.meduza.anime.core.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.meduza.anime.core.constants.AppConstants
import uz.meduza.anime.core.session.SessionManager
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var apiService: MeduzaApiService? = null

    fun getService(context: Context): MeduzaApiService {
        return apiService ?: synchronized(this) {
            apiService ?: buildRetrofit(context.applicationContext).create(MeduzaApiService::class.java).also {
                apiService = it
            }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val sessionManager = SessionManager.getInstance(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val gson = com.google.gson.GsonBuilder()
            .setStrictness(com.google.gson.Strictness.LENIENT)
            .serializeNulls()
            .create()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(SafeNetworkInterceptor())
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(TokenAuthenticator(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
