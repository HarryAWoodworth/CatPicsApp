package com.example.catpics
import com.example.catpics.models.CatModel
import io.reactivex.Observable
import retrofit2.http.GET

interface CatApiService {

    @GET("search")
    fun getCatPicture(): Observable<List<CatModel>>

}


