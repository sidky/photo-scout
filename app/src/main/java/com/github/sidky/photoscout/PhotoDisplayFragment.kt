package com.github.sidky.photoscout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.sidky.data.dao.PhotoDAO
import com.github.sidky.photoscout.databinding.FragmentPhotoDisplayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PhotoDisplayFragment : Fragment() {
    lateinit var binding: FragmentPhotoDisplayBinding
    lateinit var safeArgs: PhotoDisplayFragmentArgs

    private val dao: PhotoDAO by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo_display, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        safeArgs = PhotoDisplayFragmentArgs.fromBundle(arguments!!)

        GlobalScope.launch(Dispatchers.Main) {
            val result = async {
                dao.getPhoto(safeArgs.photoId)
            }.await()

            val url = result.urls.maxBy { it.width * it.height }?.url

            Glide.with(this@PhotoDisplayFragment).load(url!!).into(binding.photoDisplay)
        }
    }
}