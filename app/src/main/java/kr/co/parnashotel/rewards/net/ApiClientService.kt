package kr.co.parnashotel.rewards.net

import kr.co.parnashotel.BuildConfig
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.net.model.DashboardInfoModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiClientService {
    @GET(Define.DASHBOARD_INFO)
    fun requestDashboardInfo(
        @Header("Authorization") authorization: String?,
        @Query("membershipNo") membershipNo: String?,
    ):Call<DashboardInfoModel>

    //푸시키값 넘기기
    @FormUrlEncoded
    @POST
    fun requestAppInfo(
        @Url url: String?,
        @Field("device_token") token_id: String?,
        @Field("device_OS") os: String?,
        @Field("push_yn") push: String?
    ): Call<String>

    companion object {
        val interceptor: HttpLoggingInterceptor
            get() {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                return httpLoggingInterceptor.apply {
                    httpLoggingInterceptor.level =
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }
            }

        val builder: OkHttpClient.Builder
            get() = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                /*.cookieJar(JavaNetCookieJar(CookieManager()))*/

        //Gson으로 리턴
        val retrofit: Retrofit
            get() = Retrofit.Builder()
                .baseUrl(Define.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build()

        //String으로 리턴
        val retrofitString: Retrofit
            get() = Retrofit.Builder()
                .baseUrl(Define.DOMAIN)
                .addConverterFactory(ToStringConverterFactory())
                .client(builder.build())
                .build()
    }
}