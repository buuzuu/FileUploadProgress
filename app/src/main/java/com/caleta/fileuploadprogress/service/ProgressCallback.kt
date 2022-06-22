package com.caleta.fileuploadprogress.service

import com.caleta.fileuploadprogress.ChatError

interface ProgressCallback {
    public fun onProgress(bytesUploaded: Long, totalBytes: Long)
    public fun onSuccess(url: String?)
    public fun onError(error: ChatError)
}