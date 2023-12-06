package kr.co.parnas.net

import kr.co.parnas.BuildConfig
import kr.co.parnas.common.Define
import kr.co.parnas.net.model.MemoInfoModel
import kr.co.parnas.net.model.PhotoListModel
import kr.co.parnas.net.model.ResultModel

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiClientService {
    //푸시키값 넘기기
    @FormUrlEncoded
    @POST
    fun requestAppInfo(
        @Url url: String?,
        @Field("device_token") token_id: String?,
        @Field("user_id") user_id: String?,
        @Field("device_OS") os: String?,
        @Field("push_yn") push: String?
    ): Call<ResultModel>

    //푸시키값 넘기기
    @FormUrlEncoded
    @POST
    fun requestAppInfo(
        @Url url: String?,
        @Field("device_token") token_id: String?,
        @Field("device_OS") os: String?,
        @Field("push_yn") push: String?
    ): Call<String>

    //강연 정보 가져오기
    @GET
    fun getLectureInfo(
        @Url url: String?,
        @Query("Ea_Code") code: String?
    ): Call<MemoInfoModel>

    //수정을 위한 메모 정보 가져오기
    @GET
    fun getMemoInfo(
        @Url url: String?,
        @Query("user_id") user_id: String?,
        @Query("Ea_Index") index: String?
    ): Call<MemoInfoModel>

    //메모 등록 내용 전송
    @FormUrlEncoded
    @POST
    fun sendRegMemo(
        @Url url: String?,
        @Field("user_id") user_id: String?,
        @Field("Ea_Code") code: String?,
        @Field(value = "Ea_Content") content: String?,
        @FieldMap map: LinkedHashMap<String?, String?>?
    ): Call<ResultModel>

    //메모 수정 내용 전송
    @FormUrlEncoded
    @POST
    fun sendUpdateMemo(
        @Url url: String?,
        @Field("user_id") user_id: String?,
        @Field("Ea_Index") index: String?,
        @Field(value = "Ea_Content") content: String?,
        @FieldMap map: LinkedHashMap<String?, String?>?
    ): Call<ResultModel>

    @Multipart
    @POST
    fun fileUpload(
        @Url url: String?,
        @Part list: ArrayList<MultipartBody.Part?>?
    ): Call<PhotoListModel>

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
                .baseUrl(Define.DOMAIN)
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