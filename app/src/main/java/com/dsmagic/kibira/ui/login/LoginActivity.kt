package com.dsmagic.kibira.ui.login

/*
 *  This file is part of Msitu.
 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions
 *
 *  Msitu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Msitu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Msitu. If not, see <http://www.gnu.org/licenses/>
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dsmagic.kibira.R
import com.dsmagic.kibira.activities.MainActivity
import com.dsmagic.kibira.activities.RegisterActivity
import com.dsmagic.kibira.roomDatabase.AppDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    val sharedPrefFile = "LoginShareFile"
    lateinit var sharedPreferences: SharedPreferences
    var loginMode = true

    companion object {
        lateinit var authbd: AppDatabase

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       sharedPreferences =
            this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)!!
        if (loginMode) {
              checkIfLoggedIn()
        }
        authbd = AppDatabase.dbInstance(this)
        setContentView(R.layout.activity_login)
        thelogin.setOnClickListener {
            thelogin.background = resources.getDrawable(R.drawable.switch_tucks, null)
            signUp.background = null
            thelogin.setTextColor(resources.getColor(R.color.white))
            loginLayout.visibility = View.VISIBLE
            signupLayout.visibility = View.GONE
            signUp.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_grey))

        }


        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.loginbutton)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val register_page = findViewById<TextView>(R.id.signUp)

        register_page?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)


        }
        lateinit var usernameVal: String
        lateinit var passwordVal: String

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                //updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            // finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                offlineLogin(username.text.toString(), password.text.toString())
            }


        }
    }

    fun checkIfLoggedIn() {
        val userEmail: String? =
            sharedPreferences.getString("loggedUserEmail", "defaultValue")
        val userId: Int = sharedPreferences.getInt("userID", 0)
        val token = sharedPreferences.getString("token", "defaultValue")

        if (userId == 0) {
            loginMode = false
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        if (userEmail != "defaultValue") {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userID", "$userId")
            intent.putExtra("email", userEmail)
            intent.putExtra("token", token)
            startActivity(intent)
        } else {
            loginMode = false
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    fun offlineLogin(email: String, password: String) {

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val d = authbd.kibiraDao().getUser(email, password)
                var s = 70
                val userPassword = d.password
                val userEmail = d.email
                val userid = d.id
                runOnUiThread {
                    if (userid == null) {


                        Toast.makeText(
                            applicationContext, "NO ID FOUND in " +
                                    "", Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        updateUiWithUserOffline(userEmail, userid)
                    }

                }


            } catch (e: NullPointerException) {
                runOnUiThread {
                    loading.visibility = View.INVISIBLE
                    alertfail("Invalid Credentials!")
                }

            }


        }
    }

    fun alertfail(S: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setIcon(R.drawable.cross)
            .setMessage(S)
            .show()
    }


    private fun updateUiWithUserOffline(email: String, user_id: Int) {

        val displayEmail = email
        val user_id = user_id

        val editor = sharedPreferences.edit()
        editor.putString("loggedUserEmail", email)
        editor.putInt("userID", user_id)
        editor.apply()
        editor.commit()

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userID", "$user_id")
        intent.putExtra("email", "$displayEmail")
        startActivity(intent)

    }

    private fun updateUiWithUser(email: String, user_id: Int, token: String) {

        val displayEmail = email
        val user_id = user_id
        val apiToken = token

        val editor = sharedPreferences.edit()
        editor.putString("loggedUserEmail", email)
        editor.putInt("userID", user_id)
        editor.putString("token", apiToken)
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userID", "$user_id")
        intent.putExtra("email", "$displayEmail")
        intent.putExtra("token", "$apiToken")
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })


}