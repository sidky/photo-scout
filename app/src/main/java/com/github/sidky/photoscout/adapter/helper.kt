package com.github.sidky.photoscout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): T =
    DataBindingUtil.inflate(LayoutInflater.from(this.context), resId, this, attachToRoot)

