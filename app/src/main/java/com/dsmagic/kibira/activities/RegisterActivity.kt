package com.dsmagic.kibira.activities

/*

 *  This file is part of Msitu.

 *  <https://github.com/kitandara/kibira>
 *
 *  Copyright (C) 2022 Digital Solutions

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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dsmagic.kibira.R
import com.dsmagic.kibira.databinding.ActivityLoginBinding
import com.dsmagic.kibira.roomDatabase.AppDatabase
import com.dsmagic.kibira.roomDatabase.Entities.User
import com.dsmagic.kibira.ui.login.LoginActivity
import com.dsmagic.kibira.ui.login.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    companion object {
        lateinit var authDB: AppDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        signUp.setOnClickListener {
            signUp.background = resources.getDrawable(R.drawable.switch_tucks, null)
            thelogin.background = null
            loginLayout.visibility = View.GONE
            signupLayout.visibility = View.VISIBLE
            thelogin.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_grey))
            signUp.setTextColor(resources.getColor(R.color.white))

        }
        authDB = AppDatabase.dbInstance(this)

        val login = findViewById<TextView>(R.id.thelogin)
        val registerName = findViewById<EditText>(R.id.register_name)
        val registerEmail = findViewById<EditText>(R.id.register_email)
        val registerPassword = findViewById<EditText>(R.id.register_password)
        val registerConfirmPassword = findViewById<EditText>(R.id.register_confirm_password)
        val registerButton = findViewById<Button>(R.id.register_button)
        val register_loading = findViewById<ProgressBar>(R.id.register_loading)

        lateinit var name: String
        lateinit var email: String
        lateinit var password: String
        lateinit var password_confirm: String

        registerButton.setOnClickListener {
            register_loading.isVisible = true
            name = registerName.text.toString()
            email = registerEmail.text.toString()
            password = registerPassword.text.toString()
            password_confirm = registerConfirmPassword.text.toString()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                register_loading.isVisible = false
                alertfail("Fields can not be empty")

            }
            if (password.length < 4) {
                register_loading.isVisible = false
                alertfail("Password should be >4")
            } else if (!password.equals(password_confirm)) {
                register_loading.isVisible = false
                alertfail("Passwords don't match")
            } else {

                saveUser(name, email, password)

            }


        }

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    var UID: Long = 0
    fun saveUser(name: String, email: String, password: String) {

        val user = User(
            null,
            name,
            password,
            email
        )  //maintain order the fields are defined in the Entity class.


        GlobalScope.launch(Dispatchers.IO) {
            var d = authDB.kibiraDao().insertUser(user)
            if (d > 0) {
                UID = d
                registeredUser()
            }

        }

    }

    fun registeredUser() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun alertfail(S: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(S)
            .show()
    }

}