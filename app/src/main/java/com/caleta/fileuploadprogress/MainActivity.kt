package com.caleta.fileuploadprogress

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.caleta.fileuploadprogress.databinding.ActivityMainBinding
import com.caleta.fileuploadprogress.service.ProgressCallback
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.apache.commons.io.IOUtils.toByteArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt
import java.io.InputStream


class MainActivity : AppCompatActivity(), ProgressCallback {
    private lateinit var binding : ActivityMainBinding

    var TAG = "MAIN_ACTIVITY"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btn1.setOnClickListener {
                uploadDocument(R.raw.one_mb)
            }
            btn2.setOnClickListener {
                uploadDocument(R.raw.five_mb)
            }
            btn3.setOnClickListener {
                uploadDocument(R.raw.fifty_mb)
            }
        }
    }

    private fun uploadDocument(oneMb: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i("^^^^", "Request sent to upload file ")

            val file: File =
                File(filesDir.toString() + File.separator + "abcdefgh.pdf")
             //val byte = toByteArray(applicationContext.resources.openRawResource(oneMb));
            try {
                val inputStream: InputStream = applicationContext.resources.openRawResource(oneMb)
                val fileOutputStream = FileOutputStream(file)
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    fileOutputStream.write(buf, 0, len)
                }
                fileOutputStream.close()
                inputStream.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }

//            val requestFile: RequestBody = RequestBody.create(
//                "application/*".toMediaTypeOrNull(),
//                byte
//            )
/*
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("myFile", getRandomString(10)+".pdf", requestFile)

            FileUploadApplication().getInstance()?.getRetrofit()?.uploadFile(body, this@MainActivity)
                ?.enqueue(object :
                Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.i("^^^^onResponse",response.code().toString())
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e("^^^^onError", t.localizedMessage)
                    t.printStackTrace()
                }

            })*/
            //$$$$$$$$$$$$$$$$

            val fileBody = FileUploadApplication.ProgressRequestBody2(file, this@MainActivity)
            val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("myFile", getRandomString(10)+".pdf", fileBody)

            FileUploadApplication().getInstance()?.getRetrofit()?.uploadFile(filePart)?.enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.i("^^^^onResponse",response.code().toString())
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.e("^^^^onError", t.localizedMessage)
                    t.printStackTrace()
                }

            })
        }
    }
    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
        val total = ((bytesUploaded.toFloat() /totalBytes.toFloat()) * 100).roundToInt();
        Log.i(TAG, "Bytes Uploaded ${bytesUploaded} && TotalBytes ${totalBytes} && Percentage $total")
        binding.txt.text = "$total %"

/*        CoroutineScope(Dispatchers.Main).launch {
            val total = (bytesUploaded /totalBytes) * 100;
            binding.txt.text = "$total %"
        }*/

    }

    override fun onSuccess(url: String?) {
        Log.i(TAG, "")
    }

    override fun onError(error: ChatError) {
        Log.i(TAG, "")
    }
}