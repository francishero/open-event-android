package org.fossasia.openevent.general.di

import android.arch.persistence.room.Room
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.fossasia.openevent.general.OpenEventDatabase
import org.fossasia.openevent.general.auth.*
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


val commonModule = applicationContext {
    bean { Preference() }
}

val apiModule = applicationContext {
    factory {
        val retrofit: Retrofit = get()
        retrofit.create(EventApi::class.java)
    }
    factory {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApi::class.java)
    }

    factory { AuthHolder(get()) }
    bean { AuthService(get(), get()) } // TODO: Convert to factory once database is implemented

    bean { EventService(get()) } // TODO: Convert to factory once database is implemented
}

val viewModelModule = applicationContext {
    viewModel { LoginFragmentViewModel(get()) }
    viewModel { EventsViewModel(get()) }
    viewModel { ProfileFragmentViewModel(get()) }
    viewModel { SignUpFragmentViewModel(get()) }
    viewModel { EventDetailsViewModel(get()) }
}

val networkModule = applicationContext {

    bean {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper
    }

    bean { RequestAuthenticator(get()) as Authenticator }

    bean {
        val connectTimeout = 15 // 15s
        val readTimeout = 15 // 15s

        OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
                .addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .authenticator(get())
                .build()
    }

    bean {
        val baseUrl = "https://open-event-api-dev.herokuapp.com/v1/"
        val objectMapper: ObjectMapper = get()

        Retrofit.Builder()
                .client(get())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JSONAPIConverterFactory(objectMapper, Event::class.java, User::class.java, SignUp::class.java))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(baseUrl)
                .build()
    }

}

val databaseModule = applicationContext {

    bean {
        Room.databaseBuilder(androidApplication(),
                OpenEventDatabase::class.java, "open_event_database")
                .build()
    }
}