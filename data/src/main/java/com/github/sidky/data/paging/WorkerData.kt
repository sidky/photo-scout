package com.github.sidky.data.paging

import com.github.sidky.photoscout.graphql.fragment.ClientPhoto
import com.github.sidky.photoscout.graphql.fragment.NextPage

data class PhotoLoaderResponse(val photos: List<ClientPhoto>?, val next: NextPage?)
