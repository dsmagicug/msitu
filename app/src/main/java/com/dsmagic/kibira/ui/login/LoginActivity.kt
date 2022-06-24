package com.dsmagic.kibira.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dsmagic.kibira.MainActivity
//import com.dsmagic.kibira.MainActivity
import com.dsmagic.kibira.R
import com.dsmagic.kibira.RegisterActivity
import com.dsmagic.kibira.databinding.ActivityLoginBinding
import com.dsmagic.kibira.services.LoginDataClassX
import com.dsmagic.kibira.services.apiInterface
import com.dsmagic.kibira.services.loginDataclass
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val register_page = binding.registerPage

        register_page?.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }

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
            finish()
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
              loginUser(username.text.toString(), password.text.toString())
                //loginViewModel.login(username.text.toString(), password.text.toString())
            }


        }
    }

    fun loginUser(email:String,password: String)
    {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(MainActivity().BaseUrl)
            .build()
            .create(apiInterface::class.java)
        val modal = LoginDataClassX(email,password)
        val retrofitData = retrofitBuilder.loginUser(modal)
        retrofitData.enqueue(object : Callback<loginDataclass?> {
            override fun onResponse(
                call: Call<loginDataclass?>,
                response: Response<loginDataclass?>
            ) {
                loading.visibility = View.INVISIBLE
                if(response.isSuccessful) {
                    if (response.body() != null) {
                        val tag = response.body()!!.tag
                        val myemail = response.body()!!.email
                        val user_id = response.body()!!.user_id
                        val token = response.body()!!.token
                        if (tag == "V") {
                            updateUiWithUser(myemail, user_id,token)
                            SuccessAlert("Successfully Logged in")
                        }
                        else {
                            alertfail("Invalid Credentials!")
                        }


                    } else {
                        alertfail("Body null!")
                    }
                }
                else{
                    alertfail("Response not successful!")
                }


            }

            override fun onFailure(call: Call<loginDataclass?>, t: Throwable) {
                loading.visibility = View.INVISIBLE
                alertfail("something Went wrong!")
            }
        })
    }
    fun alertfail(S:String){
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setIcon(R.drawable.cross)
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
    private fun updateUiWithUser(email: String,user_id:Int,token:String) {
        val welcome = getString(R.string.welcome)
        val displayEmail = email
        val user_id = user_id
        var apiToken = token

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userID","$user_id")
        intent.putExtra("email", displayEmail)
        intent.putExtra("token",apiToken)
        startActivity(intent)

        Toast.makeText(
            applicationContext,
            "$welcome $displayEmail",
            Toast.LENGTH_LONG
        ).show()
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