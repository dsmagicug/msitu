//package com.dsmagic.kibira
//
//import android.app.Activity
//import android.os.Bundle
//import android.view.View
//import android.view.inputmethod.EditorInfo
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ProgressBar
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModelProvider
//import com.dsmagic.kibira.databinding.ActivityLoginBinding
//import com.dsmagic.kibira.ui.login.LoginViewModel
//import com.dsmagic.kibira.ui.login.LoginViewModelFactory
//import com.dsmagic.kibira.ui.login.afterTextChanged
//import kotlinx.android.synthetic.main.activity_login.*
//
//class LoginActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        var user_name = findViewById<EditText>(R.id.username)
//        var password = findViewById<EditText>(R.id.password)
//        var btn_reset = findViewById<Button>(R.id.reset)
//        var btn_submit = findViewById<Button>(R.id.login)
//        var loading = findViewById<ProgressBar>(R.id.loading)
//
//        btn_reset.setOnClickListener {
//            password.setText("")
//            user_name.setText("")
//        }
//
//        btn_submit.setOnClickListener {
//            loading.visibility = View.VISIBLE
//            var username = user_name.text
//            var password = password.text
//            //Toast.makeText(applicationContext, user_name, Toast.LENGTH_LONG).show()
//        }
//    }
//}
////        val username = binding.username
////        val password = binding.password
////        val login = binding.login
////        val loading = binding.loading
////
////        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
////            .get(LoginViewModel::class.java)
////
////        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
////            val loginState = it ?: return@Observer
////
////            // disable login button unless both username / password is valid
////            login.isEnabled = loginState.isDataValid
////
////            if (loginState.usernameError != null) {
////                username.error = getString(loginState.usernameError)
////            }
////            if (loginState.passwordError != null) {
////                password.error = getString(loginState.passwordError)
////            }
////        })
////
////        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
////            val loginResult = it ?: return@Observer
////
////            loading.visibility = View.GONE
////            if (loginResult.error != null) {
////                showLoginFailed(loginResult.error)
////            }
////            if (loginResult.success != null) {
////                updateUiWithUser(loginResult.success)
////            }
////            setResult(Activity.RESULT_OK)
////
////            //Complete and destroy login activity once successful
////            finish()
////        })
////
////        username.afterTextChanged {
////            loginViewModel.loginDataChanged(
////                username.text.toString(),
////                password.text.toString()
////            )
////        }
////
////        password.apply {
////            afterTextChanged {
////                loginViewModel.loginDataChanged(
////                    username.text.toString(),
////                    password.text.toString()
////                )
////            }
////
////            setOnEditorActionListener { _, actionId, _ ->
////                when (actionId) {
////                    EditorInfo.IME_ACTION_DONE ->
////                        loginViewModel.login(
////                            username.text.toString(),
////                            password.text.toString()
////                        )
////                }
////                false
////            }
////
////            login.setOnClickListener {
////                loading.visibility = View.VISIBLE
////                loginViewModel.login(username.text.toString(), password.text.toString())
////            }
////        }
//   // }
////}