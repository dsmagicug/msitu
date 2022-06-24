package com.dsmagic.kibira.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


    @Singleton
    class myServiceInterceptor constructor( val tokenType:String, private var sessionToken: String
                                           ) : Interceptor {

//        fun setSessionToken(sessionToken: String?) {
//            this.sessionToken = sessionToken
//        }

        @Throws(IOException::class)
        override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
            var request = chain.request()
            request = request.newBuilder().header("Authorization","$tokenType $sessionToken")
                .build()
            return chain.proceed(request)
//            if (request.header("Authorization") == null) {
//                // needs credentials
//                if (sessionToken == null) {
//                    throw RuntimeException("Session token should be defined for auth apis")
//                } else {
//
//                        val finalToken = "Bearer $sessionToken"
//                    requestBuilder
//                            .addHeader("Authorization",finalToken)
//                            .build()
//
//                }
//            }

        }



    }
