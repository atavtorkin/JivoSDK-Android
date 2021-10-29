package com.jivosite.sdk.model

import android.content.Context

/**
 * Created on 12/5/20.
 *
 * @author Alexandr Shibelev (av.shibelev@gmail.com)
 */
data class SdkContext(
    val appContext: Context,
    val widgetId: String
)