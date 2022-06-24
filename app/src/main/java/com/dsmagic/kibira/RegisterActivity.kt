package com.dsmagic.kibira

import androidx.appcompat.app.AppCompatActivity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Contacts
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.lifecycle.ViewModelProvider
import com.dsmagic.kibira.databinding.ActivityLoginBinding
import com.dsmagic.kibira.databinding.ActivityRegisterBinding
import com.dsmagic.kibira.services.RegisterDataclassX
import com.dsmagic.kibira.services.apiInterface
import com.dsmagic.kibira.ui.login.LoginActivity
import com.dsmagic.kibira.ui.login.LoginViewModel
import com.dsmagic.kibira.ui.login.LoginViewModelFactory
import com.dsmagic.kibira.ui.login.afterTextChanged
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

var login = binding.login
        var registerName = binding.registerName
        var registerEmail = binding.registerEmail
        var registerPassword = binding.registerPassword
        var registerConfirmPassword = binding.registerConfirmPassword
        var registerButton = binding.registerButton
        var register_loading = binding.registerLoading


        lateinit var name: String
        lateinit var email: String
        lateinit var password: String
        lateinit var password_confirm: String

        registerButton.setOnClickListener{

           name = registerName.text.toString()
            email = registerEmail.text.toString()
            password = registerPassword.text.toString()
           password_confirm = registerConfirmPassword.text.toString()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                alertfail("Fields can not be empty")

            }
            else if(!password.equals(password_confirm)){
                alertfail("Passwords don't match")
            }
            else{
                //MainActivity().registerUser(name,email,password,password_confirm)
                registerUser(name,email,password,password_confirm)
            }


        }

        login.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

        fun registerUser(name:String, email:String,password:String,password_confirm:String)
        {
            val retrofitBuilder = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MainActivity().BaseUrl)
                .build()
                .create(apiInterface::class.java)
            val modal = RegisterDataclassX(email,name,password,password_confirm)
            val retrofitData = retrofitBuilder.registerUser(modal)
            retrofitData.enqueue(object : Callback<RegisterDataclassX?> {
                override fun onResponse(
                    call: Call<RegisterDataclassX?>,
                    response: Response<RegisterDataclassX?>
                ) {


                    SuccessAlert("Successfully Registered")


                }

                override fun onFailure(call: Call<RegisterDataclassX?>, t: Throwable) {

                    alertfail("Email already taken")
                }
            })
        }



fun alertfail(S:String){
    AlertDialog.Builder(this)
        .setTitle("Error")
        .setMessage(S)
        .show()
}
    fun SuccessAlert(S:String){
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setIcon(R.drawable.tick)
            .setMessage(S)
            .show()
    }
//        val username = binding.username
//        val password = binding.password
//        val login = binding.login
//        val loading = binding.loading
//
//        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
//            .get(LoginViewModel::class.java)
//
//        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
//            val loginState = it ?: return@Observer
//
//            // disable login button unless both username / password is valid
//            login.isEnabled = loginState.isDataValid
//
//            if (loginState.usernameError != null) {
//                username.error = getString(loginState.usernameError)
//            }
//            if (loginState.passwordError != null) {
//                password.error = getString(loginState.passwordError)
//            }
//        })
//
//        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
//            val loginResult = it ?: return@Observer
//
//            loading.visibility = View.GONE
//            if (loginResult.error != null) {
//                showLoginFailed(loginResult.error)
//            }
//            if (loginResult.success != null) {
//                updateUiWithUser(loginResult.success)
//            }
//            setResult(Activity.RESULT_OK)
//
//            //Complete and destroy login activity once successful
//            finish()
//        })
//
//        username.afterTextChanged {
//            loginViewModel.loginDataChanged(
//                username.text.toString(),
//                password.text.toString()
//            )
//        }
//
//        password.apply {
//            afterTextChanged {
//                loginViewModel.loginDataChanged(
//                    username.text.toString(),
//                    password.text.toString()
//                )
//            }
//
//            setOnEditorActionListener { _, actionId, _ ->
//                when (actionId) {
//                    EditorInfo.IME_ACTION_DONE ->
//                        loginViewModel.login(
//                            username.text.toString(),
//                            password.text.toString()
//                        )
//                }
//                false
//            }
//
//            login.setOnClickListener {
//                loading.visibility = View.VISIBLE
//                loginViewModel.login(username.text.toString(), password.text.toString())
//            }
//        }
    // }
}