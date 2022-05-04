package br.com.fiap.acid.cap2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.acid.cap2.utils.showSnackBar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CustomFormActivity : AppCompatActivity() {

    private val etToken: EditText by lazy { findViewById(R.id.et_token) }
    private val btnEnter: Button by lazy { findViewById(R.id.btn_enter) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_form)
    }

    override fun onStart() {
        super.onStart()
        configListeners()
    }

    private fun configListeners() {
        btnEnter.setOnClickListener { onEnterAction() }
    }

    private fun onEnterAction() {
        val token = etToken.text.toString()
        val auth = Firebase.auth

        auth.signInWithCustomToken(token)
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
        private const val TAG = "CustomForm"
    }
}