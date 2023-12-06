package kr.co.parnas.net

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ToStringConverterFactory : Converter.Factory() {
    private val MEDIA_TYPE = "text/plain".toMediaTypeOrNull()

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody?, *>? {
        if (String::class.java == type) {
            return Converter {
                    value -> value.toString()
            }
        } else {
            return null
        }
    }

    override fun requestBodyConverter(type: Type, parameterAnnotations: Array<Annotation?>?, methodAnnotations: Array<Annotation?>?, retrofit: Retrofit?): Converter<*, RequestBody?>? {
        if (String::class.java == type) {
            return Converter<String?, RequestBody?> {
                    value ->
                value.toRequestBody(MEDIA_TYPE)
            }
        } else {
            return null
        }
    }
}