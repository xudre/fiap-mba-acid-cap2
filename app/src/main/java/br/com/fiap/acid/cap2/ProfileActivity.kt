package br.com.fiap.acid.cap2

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.fiap.acid.cap2.utils.showSnackBar
import com.google.firebase.auth.FirebaseUser

class ProfileActivity : AppCompatActivity() {

    private val tvUserId by lazy { findViewById<TextView>(R.id.tv_user_id) }
    private val tvUserName by lazy { findViewById<TextView>(R.id.tv_user_name) }
    private val tvUserEmail by lazy { findViewById<TextView>(R.id.tv_user_email) }
    private val tvUserPhone by lazy { findViewById<TextView>(R.id.tv_user_phone) }

    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    override fun onStart() {
        super.onStart()

        user = intent.getParcelableExtra(USER_PARAM)

        user?.let { user ->
            setupContent(user)
        } ?: showSnackBar("Usuário não encontrado")
    }

    private fun setupContent(user: FirebaseUser) {
        tvUserId.text = user.uid
        tvUserName.text = user.displayName
        tvUserEmail.text = user.email
        tvUserPhone.text = user.phoneNumber
    }

    companion object {
        const val USER_PARAM = "user"
    }
}