package kr.co.parnas.net

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AddCookiesInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        // Preference에서 cookies를 가져오는 작업을 수행
        //val preferences: Set<String> = SharedData.getSharedData(null, SharedData.COOKIES, HashSet<String>())

        /*for (cookie in preferences) {
            builder.addHeader("Cookie", cookie)
        }*/
        // Web,Android,iOS 구분을 위해 User-Agent세팅
        builder.removeHeader("User-Agent").addHeader("User-Agent", "Android")
        return chain.proceed(builder.build())
    }
}