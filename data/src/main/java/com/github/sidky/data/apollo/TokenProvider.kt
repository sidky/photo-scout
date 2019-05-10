package com.github.sidky.data.apollo

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TokenProvider {
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private var lastToken: String = ""

    fun token(): String = lastToken

    suspend fun initialize() {
        val user = auth.currentUser
        if (user != null) {
            val result = user.getIdToken(false).await()
            lastToken = result?.token ?: ""
        }
    }

    suspend fun refresh() {
        val user = auth.currentUser
        if (user != null) {
            val result = user.getIdToken(true).await()
            lastToken = result?.token ?: ""
        }
    }
}

suspend fun <T> Task<T>.await() = suspendCoroutine<T?> { cont ->
    this.addOnCompleteListener {
        if (it.isSuccessful) {
            cont.resume(it.result)
        } else {
            val ex = it.exception
            if (ex != null) {
                cont.resumeWithException(ex)
            } else {
                cont.resume(null)
            }
        }
    }
}