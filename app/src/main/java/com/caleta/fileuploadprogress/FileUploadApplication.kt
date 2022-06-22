package com.caleta.fileuploadprogress

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.caleta.fileuploadprogress.service.ProgressCallback
import com.caleta.fileuploadprogress.service.RetrofitService
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit


class FileUploadApplication :Application() {

    private var instance : FileUploadApplication? = null
    private var retrofitService: RetrofitService? = null


    override fun onCreate() {
        super.onCreate()
        getInstance()
    }

     fun getInstance(): FileUploadApplication? {
        if (instance == null) {
            instance = FileUploadApplication()
            return instance
        }
        return instance
    }

    fun getRetrofit(): RetrofitService {
        if (retrofitService == null) {
            retrofitService = Retrofit.Builder().baseUrl("https://test-caleta-server.herokuapp.com")
                .client(setLoggingInterceptor().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService::class.java)
            return retrofitService as RetrofitService
        }
        return retrofitService as RetrofitService
    }

    private fun setLoggingInterceptor() :OkHttpClient.Builder{
        return OkHttpClient.Builder()
            .connectTimeout(3,TimeUnit.MINUTES)
            .writeTimeout(3,TimeUnit.MINUTES)
            .readTimeout(3,TimeUnit.MINUTES)
            .addNetworkInterceptor(ProgressInterceptor())
    }

    internal class ProgressInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val progressCallback = request.tag(ProgressCallback::class.java)
            if (progressCallback != null) {
                Log.i("^^^^onIntercept","Request Wrapped")

                return chain.proceed(wrapRequest(request, progressCallback))
            }
            return chain.proceed(request)
        }
        private fun wrapRequest(request: Request, progressCallback: ProgressCallback): Request {
            return request.newBuilder()
                .post(ProgressRequestBody(request.body!!, progressCallback))
                .build()
        }
    }

    internal class ProgressRequestBody(
        private val delegate: RequestBody,
        private val callback: ProgressCallback,
    ) : RequestBody() {
        override fun contentType(): MediaType? = delegate.contentType()
        override fun contentLength(): Long = delegate.contentLength()

        override fun writeTo(sink: BufferedSink) {
            Log.i("^^^^onWriteTo","Buffer Sink Wrapped")
            val countingSink = CountingSink(sink).buffer()
            delegate.writeTo(countingSink)
            countingSink.flush()
        }
        private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
            private val handler = Handler(Looper.getMainLooper())
            private val total = contentLength()
            private var uploaded = 0L

            override fun write(source: Buffer, byteCount: Long) {
                Log.i("^^^^onWrite","Buffer size ${source.size} and byte count $byteCount")

                super.write(source, byteCount)
                uploaded += byteCount
                handler.post { callback.onProgress(uploaded, total) }
            }
        }
    }

    internal class ProgressRequestBody2(
        var file : File,
        var callback: ProgressCallback
    ) : RequestBody(){
        override fun contentType(): MediaType? {
            return "application/*".toMediaTypeOrNull()
        }

        override fun contentLength(): Long {
            return file.length()
        }

        override fun writeTo(sink: BufferedSink) {
            val fileLength: Long = file.length()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val `in` = FileInputStream(file)
            var uploaded: Long = 0
            try {
                var read: Int
                val handler = Handler(Looper.getMainLooper())
                while (`in`.read(buffer).also { read = it } != -1) {
                    handler.post{
                        callback.onProgress(uploaded, fileLength)
                    }
                    uploaded += read.toLong()
                    sink.write(buffer, 0, read)
                }
            } finally {
                `in`.close()
            }
        }

    }


}