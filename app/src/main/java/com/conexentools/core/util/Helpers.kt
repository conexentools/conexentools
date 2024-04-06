package com.conexentools.core.util

import android.util.Log
import com.conexentools.BuildConfig

fun log(message: String) = Log.i(BuildConfig.LOG_TAG, message)
fun logError(message: String) = Log.e(BuildConfig.LOG_TAG, message)

