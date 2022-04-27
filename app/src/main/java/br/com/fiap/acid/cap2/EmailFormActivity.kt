package br.com.fiap.acid.cap2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.acid.cap2.utils.showSnackBar
import com.google.android.gms.tasks.Task
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailFormActivity : AppCompatActivity() {

    private val etEmail: EditText by lazy { findViewById(R.id.et_email) }
    private val etPass: EditText by lazy { findViewById(R.id.et_pass) }
    private val swtCreate: SwitchMaterial by lazy { findViewById(R.id.swt_create) }
    private val btnEnter: Button by lazy { findViewById(R.id.btn_enter) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_form)
    }

    override fun onStart() {
        super.onStart()

        configListeners()
    }

    private fun configListeners() {
        swtCreate.setOnCheckedChangeListener { _, status ->
            btnEnter.text = getString(if (status) R.string.criar else R.string.entrar)
        }
        btnEnter.setOnClickListener { onEnterAction() }
    }

    private fun onEnterAction() {
        val email = etEmail.text.toString()
        val password = etPass.text.toString()
        val auth = Firebase.auth

        if (swtCreate.isChecked) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    onUserOnboardComplete(task)
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    onUserOnboardComplete(task)
                }
        }
    }

    private fun onUserOnboardComplete(task: Task<AuthResult>) {
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

    companion object {
        private const val TAG = "EmailForm"
    }
}