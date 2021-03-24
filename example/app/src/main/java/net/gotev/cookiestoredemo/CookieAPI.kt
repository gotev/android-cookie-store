package net.gotev.cookiestoredemo

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginPayload(val username: String)

interface CookieAPI {
    @POST("login.php")
    suspend fun login(@Body payload: LoginPayload)

    @GET("home.php")
    suspend fun home(): String
}
