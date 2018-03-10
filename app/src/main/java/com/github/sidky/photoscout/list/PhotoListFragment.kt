package com.github.sidky.photoscout.list

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.sidky.photoscout.PhotoAdapter
import com.github.sidky.photoscout.PhotoPresenter
import com.github.sidky.photoscout.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.koin.android.ext.android.inject

class PhotoListFragment: Fragment() {

    val presenter: PhotoPresenter by inject()

    private val adapter: PhotoAdapter by lazy {
        PhotoAdapter()
    }

    private val layoutManager by lazy {
        GridLayoutManager(context, 3)
    }


    lateinit var list: RecyclerView

    var compositeDisposable: CompositeDisposable? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(R.id.photo_list)

        list.adapter = adapter
        list.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()

        val disposable = presenter
                .photos
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    adapter.submitList(list)
                }

        if (!(compositeDisposable?.isDisposed ?: true)) {
            compositeDisposable?.dispose()
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable?.add(disposable)
    }

    override fun onPause() {
        super.onPause()

        compositeDisposable?.dispose()
    }
}