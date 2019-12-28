package net.gotev.cookiestoredemo

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginPayload(val username: String)

interface CookieAPI {
    @POST("login.php")
    fun login(@Body payload: LoginPayload): Call<Unit>
}
