package com.github.sidky.photoscout.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.sidky.data.apollo.TokenProvider
import com.github.sidky.photoscout.BuildConfig
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

class LoginFragment : Fragment() {
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
        GoogleSignIn.getClient(activity!!, gso)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (auth.currentUser != null) {
            authenticated()
        } else {
            googleLogin()
        }
    }


    private fun authenticated() {
        GlobalScope.launch(Dispatchers.Default) {
            tokenProvider.initialize()

            GlobalScope.launch(Dispatchers.Main) {
                Timber.d("Token: %s", tokenProvider.token())
                val arg = LoginFragmentDirections.actionAuthenticated()
                findNavController().navigate(arg)
            }
        }
    }

    private fun googleLogin() {
        val intent = googleClient.signInIntent
        startActivityForResult(intent, LoginActivity.RC_GOOGLE_SIGNIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LoginActivity.RC_GOOGLE_SIGNIN -> {
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
            .addOnCompleteListener(activity!!) {
                if (it.isSuccessful) {
                    Timber.d("Successfully signed in: %s", auth.currentUser?.email)

                    auth.currentUser?.getIdToken(false)?.addOnCompleteListener(activity!!) {
                        Timber.d("Token: %s", it.result?.token)

                        authenticated()
                    }
                } else {
                    Timber.e(it.exception, "Unable to sign in")
                }
            }
    }

    companion object {
        val RC_GOOGLE_SIGNIN = 1
    }
}