package com.github.sidky.photoscout.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.sidky.data.apollo.TokenProvider
import com.github.sidky.photoscout.BuildConfig
import com.github.sidky.photoscout.PhotoListActivity
import com.github.sidky.photoscout.R
import com.github.sidky.photoscout.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    val tokenProvider: TokenProvider by inject()

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.AUTH_REQUEST_ID)
            .requestEmail()
            .build()
    }

    private val googleClient by lazy {
        GoogleSignIn.getClient(this, gso)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        if (auth.currentUser != null) {
            openApp()
        } else {
            googleLogin()
        }
    }

    private fun googleLogin() {
        val intent = googleClient.signInIntent
        startActivityForResult(intent, RC_GOOGLE_SIGNIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_GOOGLE_SIGNIN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        firebaseAuthWithGoogle(account)
                    }
                } catch (e: ApiException) {
                    Timber.w(e, "Google Sign-in failed")
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Timber.d("Successfully signed in: %s", auth.currentUser?.email)

                    auth.currentUser?.getIdToken(false)?.addOnCompleteListener(this) {
                        Timber.d("Token: %s", it.result?.token)

                        openApp()
                    }
                } else {
                    Timber.e(it.exception, "Unable to sign in")
                }
            }
    }

    private fun openApp() {
        GlobalScope.launch(Dispatchers.Default) {
            tokenProvider.initialize()

            GlobalScope.launch(Dispatchers.Main) {
                val intent = Intent(this@LoginActivity, PhotoListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        val RC_GOOGLE_SIGNIN = 1
    }
}