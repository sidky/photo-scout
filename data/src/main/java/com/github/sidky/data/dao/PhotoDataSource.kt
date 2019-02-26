package com.github.sidky.data.dao

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource

class PhotoDataSource : PositionalDataSource<PhotoThumbnail>() {
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<PhotoThumbnail>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<PhotoThumbnail>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class PhotoDataSourceFactory() : DataSource.Factory<Int, PhotoThumbnail>() {
    override fun create(): DataSource<Int, PhotoThumbnail> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}