package com.github.sidky.data.paging

import androidx.paging.PagedList
import androidx.work.*
import com.github.sidky.data.converter.WorkResultUtil
import com.github.sidky.data.dao.LoadingState
import com.github.sidky.data.dao.PhotoThumbnail

abstract class AbstractPhotoBoundaryCallback<T>(private val loadingState: LoadingState) : PagedList.BoundaryCallback<T>() {

    protected val constraint: Constraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresDeviceIdle(false)
            .build()
    }

    private val workmanager: WorkManager by lazy {
        WorkManager.getInstance()
    }

    abstract fun firstPageLoader(): OneTimeWorkRequest
    abstract fun nextPageLoader(page: Int): OneTimeWorkRequest

    override fun onZeroItemsLoaded() = firstPage()

    fun firstPage() {
        val request = firstPageLoader() //buildRequest<InterestingFirstPageLoader>("page:1")
        workmanager.cancelAllWorkByTag(TAG_LOADER)
        workmanager.beginUniqueWork("loader", ExistingWorkPolicy.KEEP, request).enqueue()
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) = loadMore()

    fun loadMore() {

        if (!loadingState.hasNext) {
            return
        }

        val page = loadingState.next

        val request = nextPageLoader(page) //buildRequest<InterestingNextPageLoader>("photo:$page", input)
        WorkManager.getInstance().beginUniqueWork("loader", ExistingWorkPolicy.KEEP, request).enqueue()
    }

    protected inline fun <reified T : ListenableWorker> buildRequest(name: String, input: Data? = null): OneTimeWorkRequest {
        var builder = OneTimeWorkRequestBuilder<T>()
            .setConstraints(constraint)

        if (input != null) {
            builder = builder.setInputData(input)
        }

        return builder.addTag("name:$name")
            .addTag(TAG_LOADER)
            .build()
    }

    companion object {
        val TAG_LOADER = "loader"
    }
}

class InterestingPhotoBoundaryCallback(loadingState: LoadingState) : AbstractPhotoBoundaryCallback<PhotoThumbnail>(loadingState) {
    override fun firstPageLoader(): OneTimeWorkRequest = buildRequest<InterestingFirstPageLoader>("page:1")

    override fun nextPageLoader(page: Int): OneTimeWorkRequest =
        buildRequest<InterestingNextPageLoader>("photo:$page", WorkResultUtil.inputPage(page))
}

class SearchPhotoBoundaryCallback(private val query: String, loadingState: LoadingState) : AbstractPhotoBoundaryCallback<PhotoThumbnail>(loadingState) {
    override fun firstPageLoader(): OneTimeWorkRequest =
        buildRequest<SearchFirstPageLoader>("page:1", WorkResultUtil.query(query))

    override fun nextPageLoader(page: Int): OneTimeWorkRequest = buildRequest<InterestingNextPageLoader>("photo:$page", WorkResultUtil.query(query, page))
}