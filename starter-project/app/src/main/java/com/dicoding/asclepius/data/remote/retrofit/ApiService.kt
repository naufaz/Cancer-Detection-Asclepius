package com.dicoding.asclepius.data.remote.retrofit

import com.dicoding.asclepius.data.remote.response.NewsResponse
import com.dicoding.asclepius.utils.NEWS_CATEGORY
import com.dicoding.asclepius.utils.NEWS_LANGUAGE
import com.dicoding.asclepius.utils.NEWS_QUERY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines")
    fun getNews(
        @Query("q") query: String = NEWS_QUERY,
        @Query("category") category: String = NEWS_CATEGORY,
        @Query("language") language: String = NEWS_LANGUAGE,
        @Query("apiKey") apiKey: String = BuildConfig.API_KEY
    ): Call<NewsResponse>
}