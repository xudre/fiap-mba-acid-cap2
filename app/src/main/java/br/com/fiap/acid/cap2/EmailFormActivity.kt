package br.com.fiap.acid.cap2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EmailFormActivity : AppCompatActivity() {

    private val etEmail: EditText by lazy { findViewById(R.id.et_email) }
    private val etPass: EditText by lazy { findViewById(R.id.et_pass) }
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
        btnEnter.setOnClickListener { onEnterAction() }
    }

    private fun onEnterAction() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()

    }
}