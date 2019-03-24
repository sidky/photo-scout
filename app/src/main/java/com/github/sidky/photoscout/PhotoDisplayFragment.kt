package com.github.sidky.photoscout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.sidky.data.dao.PhotoDAO
import com.github.sidky.photoscout.adapter.InfoAdapter
import com.github.sidky.photoscout.databinding.FragmentPhotoDisplayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class PhotoDisplayFragment : Fragment() {
    lateinit var binding: FragmentPhotoDisplayBinding
    lateinit var safeArgs: PhotoDisplayFragmentArgs

    lateinit var infoAdapter: InfoAdapter

    val photoDetailViewModel: PhotoDetailViewModel by sharedViewModel()

    private val dao: PhotoDAO by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_display, container, false)

        infoAdapter = InfoAdapter()
        binding.info.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.info.adapter = infoAdapter
        val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        decoration.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.spacer)!!)
        binding.info.addItemDecoration(decoration)

        photoDetailViewModel.detail.observe(this, Observer {
            GlobalScope.launch(Dispatchers.Default) {
                infoAdapter.update(it)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        safeArgs = PhotoDisplayFragmentArgs.fromBundle(arguments!!)

        GlobalScope.launch(Dispatchers.Main) {
            Timber.d("ID: ${safeArgs.photoId}")
            val result = async {
                dao.getPhoto(safeArgs.photoId)
            }.await()

            val url = result?.urls?.maxBy { it.width * it.height }?.url

            Glide.with(this@PhotoDisplayFragment).load(url!!).into(binding.photoDisplay)

            photoDetailViewModel.load(result.photo.photoId)
        }
    }
}