package com.github.sidky.photoscout.api

import com.github.sidky.photoscout.api.flickr.FlickrService
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object FlickrApi {

    fun service(apiKey: String, moshi: Moshi): FlickrService {
        val client = OkHttpClient.Builder()
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain?): Response {
                        val request = chain?.request()
                        val updatedUrl = request?.url()
                                ?.newBuilder()
                                ?.addQueryParameter("api_key", apiKey)
                                ?.addQueryParameter("format", "json")
                                ?.addQueryParameter("nojsoncallback", "1")
                                ?.build()

                        val updatedRequest = updatedUrl?.let {
                            request.newBuilder().url(it).build()
                        }
                        return chain!!.proceed(updatedRequest)
                    }
                }).build()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.flickr.com/services/rest/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()

        return retrofit.create(FlickrService::class.java)
    }

}