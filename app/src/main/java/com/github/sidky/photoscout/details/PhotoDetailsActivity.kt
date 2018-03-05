package com.github.sidky.photoscout.details

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.transition.Transition
import android.util.Log
import android.view.Gravity
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.sidky.photoscout.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import org.koin.android.ext.android.inject

class PhotoDetailsActivity : AppCompatActivity() {

    private val presenter: PhotoDetailsPresenter by inject()

    private val photo: PhotoDetails by lazy {
        intent.extras.getParcelable<PhotoDetails>(PHOTO)
    }

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val adapter: PhotoInfoAdapter = PhotoInfoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        supportPostponeEnterTransition()

        Glide.with(this)
                .load(photo.url).transition(GenericTransitionOptions.withNoTransition())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        supportStartPostponedEnterTransition()
                        return false
                    }

                }).into(photo_view)

        window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                drawer.openDrawer(Gravity.END)
            }

            override fun onTransitionResume(transition: Transition?) {
                // Stub
            }

            override fun onTransitionPause(transition: Transition?) {
                // Stub
            }

            override fun onTransitionCancel(transition: Transition?) {
                // Stub
            }

            override fun onTransitionStart(transition: Transition?) {
                // Stub
            }

        })

        val llm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        info_items.layoutManager = llm
        info_items.adapter = adapter

        val disposable = presenter.getInfo(photo)
                .subscribeOn(Schedulers.io())
                .map { PhotoDetailItem.generateItems(it)}
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    Log.i("INFO", "items: ${list}")
                    adapter.items = list
                }

        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.dispose()
    }

    companion object {
        val PHOTO = "details.photo"
    }
}