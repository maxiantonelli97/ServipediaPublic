package com.antonelli.servipedia

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.antonelli.servipedia.databinding.ActivityMainBinding
import com.antonelli.servipedia.entity.UserModel
import com.antonelli.servipedia.utils.MainInterface
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.securepreferences.SecurePreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainInterface {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var toolbar: Toolbar? = null
    private val securePreferences by lazy { SecurePreferences(applicationContext) }
    private lateinit var resultManager: ActivityResultLauncher<IntentSenderRequest>
    private val tag = "LOGIN INFO HELP"
    private lateinit var auth: FirebaseAuth
    private var logueado: Boolean = false
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        logueado = securePreferences.contains("userMail") &&
            securePreferences.contains("userId") &&
            securePreferences.contains("userName")

        toolbar = binding.mainToolbar
        setSupportActionBar(toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        toolbar?.inflateMenu(R.menu.menu)
        supportActionBar?.title = "Servipedia"
        resultManager = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            mostrarProgressBar()
            toolbar?.menu?.getItem(0)?.isVisible = false
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential =
                        Identity.getSignInClient(this).getSignInCredentialFromIntent(result.data)

                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(credential.googleIdToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.e("firebase", "signInWithCredential:success")
                                saveUser(
                                    auth.currentUser?.uid,
                                    auth.currentUser?.email,
                                    auth.currentUser?.displayName
                                )
                                usuarioLogueado()
                            } else {
                                toastError()
                                usuarioDeslogueado()
                            }
                        }
                } catch (e: ApiException) {
                    toastError()
                    usuarioDeslogueado()
                }
            } else {
                toastError()
                usuarioDeslogueado()
            }
        }

        if (logueado) {
            usuarioLogueado()
        } else {
            usuarioDeslogueado()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        if (menu != null) {
            if (logueado) {
                menu.getItem(0).isVisible = false
                menu.getItem(1).isVisible = true
                menu.getItem(2).isVisible = true
                menu.getItem(3).isVisible = true
            } else {
                menu.getItem(0).isVisible = true
                menu.getItem(1).isVisible = false
                menu.getItem(2).isVisible = false
                menu.getItem(3).isVisible = false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_login -> {
            signIn()
            true
        }
        R.id.action_logout -> {
            dialogCerrarSesion()
            true
        }
        R.id.action_delete_user -> {
            dialogEliminarCuenta()
            true
        }
        R.id.action_favorites -> {
            navController.navigate(R.id.favoritesFragment)
            true
        }
        16908332 -> {
            navController.navigateUp()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun usuarioLogueado() {
        mostrarProgressBar()
        navController.navigateUp()
        logueado = true
        toolbar?.menu?.getItem(0)?.isVisible = false
        toolbar?.menu?.getItem(1)?.isVisible = true
        toolbar?.menu?.getItem(2)?.isVisible = true
        toolbar?.menu?.getItem(3)?.isVisible = true
        ocultarProgressBar()
    }

    private fun usuarioDeslogueado() {
        deleteUser()
        mostrarProgressBar()
        logueado = false
        Firebase.auth.signOut()
        toolbar?.menu?.getItem(0)?.isVisible = true
        toolbar?.menu?.getItem(1)?.isVisible = false
        toolbar?.menu?.getItem(2)?.isVisible = false
        toolbar?.menu?.getItem(3)?.isVisible = false
        navController.navigateUp()
        ocultarProgressBar()
    }
    private fun saveUser(id: String?, mail: String?, name: String?) {
        securePreferences.edit().putString("userId", id).apply()
        securePreferences.edit().putString("userMail", mail).apply()
        securePreferences.edit().putString("userName", name).apply()
    }

    private fun deleteUser() {
        securePreferences.edit().remove("userId").apply()
        securePreferences.edit().remove("userMail").apply()
        securePreferences.edit().remove("userName").apply()
    }

    fun getuser(): UserModel? {
        val user = UserModel(
            securePreferences.getString("userId", null),
            securePreferences.getString("userMail", null),
            securePreferences.getString("userName", null)
        )
        return if (user.id == null || user.name == null || user.email == null) {
            null
        } else {
            user
        }
    }

    fun hideKeyboard(view: View) {
        val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun signIn() {
        if (isOnline()) {
            val request = GetSignInIntentRequest.builder()
                .setServerClientId(getString(R.string.ID_cliente_google))
                .build()

            Identity.getSignInClient(this)
                .getSignInIntent(request)
                .addOnSuccessListener { result: PendingIntent ->
                    try {
                        resultManager.launch(
                            IntentSenderRequest.Builder(result).build()
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(tag, "Google Sign-in failed")
                    }
                }
                .addOnFailureListener { e: Exception? ->
                    Log.e(
                        tag,
                        "Google Sign-in failed",
                        e
                    )
                }
        }
    }

    fun isOnline(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
        return false
    }

    private fun dialogCerrarSesion() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.confirmar_cerrar_Sesion))
        builder.setPositiveButton(R.string.si) { _, _ ->
            if (isOnline()) {
                usuarioDeslogueado()
            }
        }
        builder.setNegativeButton(R.string.no) { dialogb, _ ->
            dialogb.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.darkness))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.darkness))
    }

    private fun dialogEliminarCuenta() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.confirmar_eliminar_usuario))
        builder.setPositiveButton(R.string.si) { _, _ ->
            if (isOnline()) {
                eliminarCuenta()
            }
        }
        builder.setNegativeButton(R.string.no) { dialogb, _ ->
            dialogb.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.darkness))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.darkness))
    }

    private fun eliminarCuenta() {
        val user = Firebase.auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    usuarioDeslogueado()
                } else {
                    toastError()
                }
            }
    }

    override fun mostrarProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun ocultarProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    fun toastError() {
        Toast.makeText(applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
    }
}
