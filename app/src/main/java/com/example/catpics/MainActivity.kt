package com.example.catpics

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.readystatesoftware.chuck.ChuckInterceptor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import timber.log.Timber.DebugTree


class MainActivity : AppCompatActivity() {

    // Cat API Retrofit Service
    lateinit var catApiService: CatApiService

    // Screen display metrics
    lateinit var displayMetrics: DisplayMetrics

    // Disposable, used to stop background fetching in onDestroy()
    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Timber tree for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        // Http Client
        val httpClient = OkHttpClient().newBuilder()
            // Add Chuck interceptor for debugging
            .addInterceptor(ChuckInterceptor(this))
            .build()

        // Retrofit
        val retrofit = Retrofit.Builder()
            // Add http client
            .client(httpClient)
            // Add call adapter factory (RxJava)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            // Add converter factory (Gson)
            .addConverterFactory(GsonConverterFactory.create())
            // Base URL of API
            .baseUrl("https://api.thecatapi.com/v1/images/")
            .build()

        // Set service
        this.catApiService = retrofit.create<CatApiService>(CatApiService::class.java)

        // Get display metrics for width and height
        displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        // Init with a cat pic
        this.callCatService()
    }

    // Make a call to the Cat API using our service and set the image
    private fun callCatService() {
        var url: String = ""
        // Call service
        disposable = this.catApiService.getCatPicture()
            // Fetch data in background
            .subscribeOn(Schedulers.io())
            // Display data in main thread
            .observeOn(AndroidSchedulers.mainThread())
            // Access data
            .subscribe(
                {
                    result -> run {
                        // Get url
                        url = result[0].url
                        var logStr = "Url: $url"
                        Timber.d(logStr)
                        // Load image from url into imageView using Glide
                        Glide.with(this)
                            .load(url)
                            .apply(RequestOptions().override(this.displayMetrics.widthPixels, this.displayMetrics.heightPixels))
                            .into(catImage)
                    }
                },
                {
                    error -> Timber.e(error.toString())
                }
            )
    }

    // Stop background fetching if app is terminated
    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}

// Uses Glide, Chuck, Timber, Retrofit, RxJava