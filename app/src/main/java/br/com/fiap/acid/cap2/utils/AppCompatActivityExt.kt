package br.com.fiap.acid.cap2.utils

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

fun AppCompatActivity.showSnackBar(message: String) {
    Snackbar
        .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        .show()
}