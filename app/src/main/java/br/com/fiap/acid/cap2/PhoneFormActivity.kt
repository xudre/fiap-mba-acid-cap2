package br.com.fiap.acid.cap2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.acid.cap2.utils.showSnackBar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class PhoneFormActivity : AppCompatActivity() {

    private val etPhone: EditText by lazy { findViewById(R.id.et_phone) }
    private val etCode: EditText by lazy { findViewById(R.id.et_code) }
    private val btnSend: Button by lazy { findViewById(R.id.btn_send) }
    private val btnEnter: Button by lazy { findViewById(R.id.btn_enter) }

    private var storedVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_form)
    }

    override fun onStart() {
        super.onStart()

        FirebaseAuth.getInstance()
            .firebaseAuthSettings
            .forceRecaptchaFlowForTesting(true) // Forçando o reCaptcha para a validação do device

        configListeners()
    }

    private fun configListeners() {
        btnSend.setOnClickListener { onSendAction() }
        btnEnter.setOnClickListener { onEnterAction() }
    }

    private fun onSendAction() {
        val auth = Firebase.auth
        val phone = etPhone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout para a auto verificação
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.i(TAG, "Token solicitação: $verificationId")

                    storedVerificationId = verificationId

                    etCode.visibility = View.VISIBLE
                    etCode.isEnabled = true
                    btnEnter.isEnabled = true
                }

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.i(TAG, "Código de ativação recuperado: ${credential.smsCode}")

                    signInWithCredentials(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.d(TAG, "Erro: ${e.localizedMessage}")

                    showSnackBar("Não foi possível notificar o número de telefone")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun onEnterAction() {
        storedVerificationId?.let { verificationId ->
            val code = etCode.text.toString()

            etCode.isEnabled = false
            btnSend.isEnabled = false
            btnEnter.isEnabled = false

            val credential = PhoneAuthProvider.getCredential(verificationId, code)

            signInWithCredentials(credential)
        } ?: showSnackBar("Id de verificação não encontrado.")
    }

    private fun signInWithCredentials(credential: PhoneAuthCredential) {
        val auth = Firebase.auth

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    task.result?.user?.let { user ->
                        val intent = Intent(this, ProfileActivity::class.java)

                        intent.putExtra(ProfileActivity.USER_PARAM, user)

                        startActivity(intent)
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    showSnackBar("Não foi possível autenticar com as credenciais")
                }
            }
    }

    companion object {
        private const val TAG = "PhoneForm"
    }
}