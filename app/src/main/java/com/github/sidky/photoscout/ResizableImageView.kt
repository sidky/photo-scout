package com.github.sidky.photoscout

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.sidky.photoscout.data.PhotoWithURL
import com.github.sidky.photoscout.data.SizedURL
import timber.log.Timber

class ResizableImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ImageView(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context): this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    init {
        adjustViewBounds = true
        scaleType = ScaleType.CENTER_CROP
    }

    var image: PhotoWithURL? = null
        set(value) {
            field = value
            updatePhoto()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePhoto()
    }

    private fun updatePhoto() {
        Timber.e("Size: $width x $height")
        val url = image?.urls?.fold<SizedURL, SizedURL?>(null as SizedURL?) { s, p ->
            if (s == null) {
                p
            } else if (p.width >= width && p.height >= height) {
                if (compare(s, p) > 0) p else s
            } else {
                if (compare(s, p) > 0) s else p
            }
        }
        if (url != null) {
            Glide.with(this).load(url.url).into(this)
        }
    }

    private fun compare(u1: SizedURL, u2: SizedURL): Int {
        return if (u1.width == u2.width) {
            u1.height - u2.height
        } else {
            u1.width - u2.width
        }
    }
}