package com.dsmagic.kibira

import androidx.appcompat.app.AppCompatActivity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Contacts
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible

import androidx.lifecycle.ViewModelProvider
import com.dsmagic.kibira.databinding.ActivityLoginBinding
import com.dsmagic.kibira.databinding.ActivityRegisterBinding
import com.dsmagic.kibira.services.AppModule
import com.dsmagic.kibira.services.RegisterDataclassX
import com.dsmagic.kibira.services.ResponseRegister
import com.dsmagic.kibira.services.apiInterface
import com.dsmagic.kibira.ui.login.LoginActivity
import com.dsmagic.kibira.ui.login.LoginViewModel
import com.dsmagic.kibira.ui.login.LoginViewModelFactory
import com.dsmagic.kibira.ui.login.afterTextChanged
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.loginLayout
import kotlinx.android.synthetic.main.activity_login.signUp
import kotlinx.android.synthetic.main.activity_login.signupLayout
import kotlinx.android.synthetic.main.activity_login.thelogin
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_register)
        signUp.setOnClickListener{
            signUp.background = resources.getDrawable(R.drawable.switch_tucks,null)
            thelogin.background = null
            loginLayout.visibility = View.GONE
            signupLayout.visibility = View.VISIBLE
            thelogin.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_grey))
            signUp.setTextColor(resources.getColor(R.color.white))

        }

        var login = findViewById<TextView>(R.id.thelogin)
        var registerName = findViewById<EditText>(R.id.register_name)
        var registerEmail = findViewById<EditText>(R.id.register_email)
        var registerPassword = findViewById<EditText>(R.id.register_password)
        var registerConfirmPassword =findViewById<EditText>(R.id.register_confirm_password)
        var registerButton = findViewById<Button>(R.id.register_button)
        var register_loading = findViewById<ProgressBar>(R.id.register_loading)

        lateinit var name: String
        lateinit var email: String
        lateinit var password: String
        lateinit var password_confirm: String

        registerButton.setOnClickListener{
            register_loading.isVisible = true
           name = registerName.text.toString()
            email = registerEmail.text.toString()
            password = registerPassword.text.toString()
           password_confirm = registerConfirmPassword.text.toString()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                register_loading.isVisible = false
                alertfail("Fields can not be empty")

            }
            if(password.length <4){
                register_loading.isVisible = false
                alertfail("Password should be >4")
            }
            else if(!password.equals(password_confirm)){
                register_loading.isVisible = false
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
        val retrofitDataObject = AppModule.retrofitInstance()
            val modal = RegisterDataclassX(email,name,password,password_confirm)
            val retrofitData = retrofitDataObject.registerUser(modal)
            retrofitData.enqueue(object : Callback<ResponseRegister?> {
                override fun onResponse(
                    call: Call<ResponseRegister?>,
                    response: Response<ResponseRegister?>
                ) {
if(response.isSuccessful){
    if(response.body()!!.message == "Success"){
        register_loading.isVisible = false
        SuccessAlert("Successfully Registered")
       lo()
    }else{

        alertfail("Email already taken")
    }
} else{
    alertfail("$response")
}



                }

                override fun onFailure(call: Call<ResponseRegister?>, t: Throwable) {

                    alertfail("Something went wrong")
                }
            })
        }

    fun lo(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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