package br.com.fiap.acid.cap2

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.acid.cap2.utils.showSnackBar
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val btnLoginGoogle: Button by lazy { findViewById(R.id.btn_login_google) }
    private val btnLoginEmail: Button by lazy { findViewById(R.id.btn_login_email) }
    private val btnLoginPhone: Button by lazy { findViewById(R.id.btn_login_phone) }
    private val btnLoginAnon: Button by lazy { findViewById(R.id.btn_login_anon) }
    private val btnLoginCustom: Button by lazy { findViewById(R.id.btn_login_custom) }

    private lateinit var oneTapClient: SignInClient
    private var signInRequest: BeginSignInRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        oneTapClient = Identity.getSignInClient(this)
    }

    override fun onStart() {
        super.onStart()

        Firebase.auth.signOut()

        configListeners()
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ONE_TAP -> {
                getGoogleCredentials(data)
            }
        }
    }

    private fun configListeners() {
        btnLoginGoogle.setOnClickListener { doLoginGoogle() }
        btnLoginEmail.setOnClickListener { doLoginEmail() }
        btnLoginPhone.setOnClickListener { doLoginPhone() }
        btnLoginAnon.setOnClickListener { doLoginAnon() }
        btnLoginCustom.setOnClickListener { doLoginCustom() }
    }

    /**
     * Google Login - Primeira etapa
     */
    @Suppress("DEPRECATION")
    private fun doLoginGoogle() {
        if (signInRequest == null) {
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // ID do cliente web, coletado no Console ou do google-services.json:
                        .setServerClientId(getString(R.string.google_client_id))
                        // Habilita o v??nculo com novas contas:
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()
        }

        signInRequest?.let { request ->
            oneTapClient.beginSignIn(request)
                .addOnSuccessListener { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender,
                            REQUEST_ONE_TAP,
                            null, 0, 0, 0
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Erro ao iniciar tela de One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "A requisi????o de login falhou: ${e.localizedMessage}")
                }
        }
    }

    /**
     * Google Login - Segunda etapa
     */
    private fun getGoogleCredentials(data: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken

            when {
                idToken != null -> {
                    // Obteve o ID token da Google e agora ?? poss??vel autenticar no backend
                    finishGoogleSignIn(idToken)
                }
                else -> {
                    // N??o conseguiu obter as credenciais
                    showSnackBar("N??o foi poss??vel obter as credenciais")
                }
            }
        } catch (e: ApiException) {
            Log.d(TAG, "N??o foi poss??vel obter as credenciais: ${e.localizedMessage}")
        }
    }

    /**
     * Google Login - Terceira etapa, final
     */
    private fun finishGoogleSignIn(idToken: String) {
        val auth = Firebase.auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    task.result?.user?.let { user ->
                        navigateUserProfile(user)
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    showSnackBar("N??o foi poss??vel autenticar com as credenciais")
                }
            }
    }

    private fun doLoginEmail() {
        startActivity(Intent(this, EmailFormActivity::class.java))
    }

    private fun doLoginPhone() {
        startActivity(Intent(this, PhoneFormActivity::class.java))
    }

    private fun doLoginAnon() {
        val auth = Firebase.auth

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        auth.currentUser?.let { user ->
                            navigateUserProfile(user)
                        }
                    }
                    false -> {
                        showSnackBar("Autentica????o an??nima falhou.")
                    }
                }
            }
    }

    private fun doLoginCustom() {
        startActivity(Intent(this, CustomFormActivity::class.java))
    }

    private fun navigateUserProfile(user: FirebaseUser) {
        val intent = Intent(this, ProfileActivity::class.java)

        intent.putExtra(ProfileActivity.USER_PARAM, user)

        startActivity(intent)
    }

    companion object {
        private const val TAG = "Home"
        private const val REQUEST_ONE_TAP = 90
    }
}