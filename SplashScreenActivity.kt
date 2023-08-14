package com.antonelli.servipedia

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.securepreferences.SecurePreferences

class SplashScreenActivity : AppCompatActivity() {
    private val securePreferences by lazy { SecurePreferences(applicationContext) }
    private val aceptados = "terminosAceptados"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_splash_screen)
        showDialog()
    }

    private fun showDialog() {
        if (securePreferences.contains(aceptados)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle(getString(R.string.tYCTitulo))
            builder.setMessage(R.string.terminosYCondiciones)
            builder.setPositiveButton(R.string.aceptar) { _, _ ->
                securePreferences.edit().putString(aceptados, aceptados).apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton(R.string.cancelar) { _, _ ->
                finish()
            }
            builder.setCancelable(false)
            val dialog = builder.create()
            dialog.show()
            dialog.findViewById<TextView>(android.R.id.message).textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.darkness))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.darkness))
            dialog.window?.setLayout(resources.displayMetrics.heightPixels / 2, resources.displayMetrics.heightPixels / 2)
        }
    }
}
