package com.dsmagic.kibira.data

import android.util.Log
import com.dsmagic.kibira.data.model.LoggedInUser
import com.dsmagic.kibira.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication

            val User = LoggedInUser(java.util.UUID.randomUUID().toString(), username)
            return Result.Success(User)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}