package com.github.sidky.photoscout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.sidky.photoscout.api.flickr.FlickrService
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject

class PhotoActivity : AppCompatActivity() {

    val flickr: FlickrService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        flickr.interesting().subscribeOn(Schedulers.io()).subscribe { v ->
            Log.e("Flickr", v.toString())
        }
    }
}