package com.dicoding.asclepius.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.asclepius.data.remote.response.ArticlesItem
import com.dicoding.asclepius.data.remote.response.NewsResponse
import com.dicoding.asclepius.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewViewModel : ViewModel() {

    private var _listNews = MutableLiveData<List<ArticlesItem>?>()
    val listNews: LiveData<List<ArticlesItem>?> = _listNews

    private var _loader = MutableLiveData<Boolean>()
    val loader: LiveData<Boolean> = _loader

    init {
        getAllNews()
    }

    private fun getAllNews() {
        _loader.value = true
        val client = ApiConfig.getServiceApi().getNews()
        client.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                _loader.value = false
                if (response.isSuccessful) {
                    _listNews.value = response.body()?.articles
                } else {
                    Log.e("NewViewModel", response.message())
                }
            }


            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                _loader.value = false
                Log.e("NewViewModel", t.message.toString())
            }

        })
    }

}