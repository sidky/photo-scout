package com.github.sidky.data.paging

sealed class GraphQLResponse {
    data class Success(val response: PhotoLoaderResponse) : GraphQLResponse()
    class Failure(): GraphQLResponse()
}