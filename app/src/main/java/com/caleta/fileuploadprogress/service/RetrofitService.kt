package com.caleta.fileuploadprogress.service
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.Request
import retrofit2.http.Tag


interface RetrofitService {

    @Multipart
    @POST("uploadFile")
    fun uploadFile( @Part file: MultipartBody.Part, @Tag progressCallback: ProgressCallback?  ) :
            Call<JsonObject>

    @Multipart
    @POST("uploadFile")
    fun uploadFile( @Part file: MultipartBody.Part  ) :
            Call<JsonObject>
}