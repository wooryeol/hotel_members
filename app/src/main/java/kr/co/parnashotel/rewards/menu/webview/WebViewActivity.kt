package kr.co.parnashotel.rewards.menu.webview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kr.co.parnashotel.R
import kr.co.parnashotel.databinding.ActWebviewBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.common.GlobalApplication
import kr.co.parnashotel.rewards.common.PopupFactory
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.common.UtilPermission
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.rewards.menu.home.MainActivity
import kr.co.parnashotel.rewards.menu.myPage.RewardActivity
import kr.co.parnashotel.rewards.model.MembershipUserInfo
import kr.co.parnashotel.rewards.model.TierModel
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date

class WebViewActivity : AppCompatActivity() {
    private lateinit var mBinding: ActWebviewBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var webview: WebView
    private var isTwo = false
    private var mUrl :String? = null

    // 사진 업로드 관련
    var cameraPath = ""
    var mWebViewImageUpload: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActWebviewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        webview = mBinding.webview

        //앱 사용중 화면 켜짐
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //카카오 로그인
        KakaoSdk.init(this, getString(R.string.kakao_app_key))

        val pushUrl = intent.getStringExtra("index")
        mUrl = pushUrl ?: Define.DOMAIN
        initWebview(mUrl!!)

        setMembershipNo()

        // 뒤로가기 버튼
        mBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun initWebview(url: String) {
        val settings = webview.settings
        settings.javaScriptEnabled = true
        //settings.pluginState = WebSettings.PluginState.ON
        // window.open 메서드를 이용할 때의 동작을 설정할 수 있게 한다
        settings.setSupportMultipleWindows(true)
        settings.supportMultipleWindows()
        settings.javaScriptCanOpenWindowsAutomatically = true
        //캐시 설정
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        //웹뷰 캐시사용 안함
        //settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //html 컨텐츠가 웹뷰에 맞게 나타나도록
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        webview.setInitialScale(1)
        //줌기능 관련
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //동영상 재생 문제
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            //간헐적인 동영상 플레이 장애처리
            settings.mediaPlaybackRequiresUserGesture = false

            //디버깅용 WebView 셋팅
            WebView.setWebContentsDebuggingEnabled(true)
        }*/

        //userAgent 추가
        val userAgent = settings.userAgentString
        settings.userAgentString = "$userAgent parnashotel_AOS"
        //브릿지 설정
        webview.addJavascriptInterface(AppScript(), "Android")

        //폰트크기 고정
        //settings.setTextZoom(100);
        //Javascript error를 무시
        settings.domStorageEnabled = true

        //동영상 전체 화면을 위해 추가
        // webview.webChromeClient = FullscreenableChromeClient(this)

        webview.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {

                if (url != "about:blank") {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        if (url.startsWith("intent:") ||
                            url.contains("http://market.android.com") ||
                            url.contains("http://m.ahnlab.com/kr/site/download") ||
                            url.contains("market://") ||
                            url.contains("vguard") ||
                            url.contains("droidxantivirus") ||
                            url.contains("v3mobile") ||
                            url.contains(".apk") ||
                            url.contains("mvaccine") ||
                            url.contains("smartwall://") ||
                            url.contains("nidlogin://") ||
                            url.contains("http://m.ahnlab.com/kr/site/download") ||
                            url.endsWith(".apk")
                        ) {
                            return urlSchemeIntent(view, url)
                        } else {
                            view.loadUrl(url)
                        }
                    } else return if (url.startsWith("mailto:")) {
                        return false
                    } else if (url.startsWith("tel:")) {
                        false
                    } else {
                        url.let {
                            if (it.startsWith("intent:#Intent;action=com.kakao.talk.intent.action")){
                                try {
                                    val intent = Intent.parseUri(it, Intent.URI_INTENT_SCHEME)
                                    intent?.let {
                                        view.context.startActivity(it)
                                        return true
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(mContext, "카카오톡이 설치되어 있지 않습니다.", Toast.LENGTH_LONG).show()
                                    val marketLaunch = Intent(Intent.ACTION_VIEW)
                                    marketLaunch.data = Uri.parse("https://play.google.com/store/apps/details?id=com.kakao.talk&hl=ko&gl=US")
                                    startActivity(marketLaunch)
                                    finish()
                                    return false
                                }
                            }
                        }
                        return urlSchemeIntent(view, url)
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        webview.webChromeClient = object : WebChromeClient() {

            val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data

                    if(intent == null){ //바로 사진을 찍어서 올리는 경우
                        val results = arrayOf(Uri.parse(cameraPath))
                        mWebViewImageUpload!!.onReceiveValue(results!!)
                    }
                    else{ //사진 앱을 통해 사진을 가져온 경우
                        val results = intent!!.data!!
                        mWebViewImageUpload!!.onReceiveValue(arrayOf(results!!))
                    }
                }
                else{ //취소 한 경우 초기화
                    mWebViewImageUpload!!.onReceiveValue(null)
                    mWebViewImageUpload = null
                }
            }

            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                try {
                    try{
                        mWebViewImageUpload = filePathCallback!!
                        var takePictureIntent : Intent?
                        takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if(takePictureIntent.resolveActivity(packageManager) != null){
                            var photoFile : File?

                            photoFile = createImageFile()
                            takePictureIntent.putExtra("PhotoPath",cameraPath)

                            if(photoFile != null){
                                cameraPath = "file:${photoFile.absolutePath}"
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile))
                            }
                            else takePictureIntent = null
                        }
                        val contentSelectionIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        contentSelectionIntent.type = "image/*"

                        var intentArray: Array<Intent?>

                        if(takePictureIntent != null) intentArray = arrayOf(takePictureIntent)
                        else intentArray = takePictureIntent?.get(0)!!

                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(Intent.EXTRA_TITLE,"사용할 앱을 선택해주세요.")
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        launcher.launch(chooserIntent)
                    }
                    catch (e : Exception){
                        Log.d("wooryeol", "error >>> $e")
                    }

                } catch (e: Exception) {
                    Log.d("wooryeol", "error >>> $e")
                }
                return true
            }
        }

        //파일 다운로드 리스너
        webview.setDownloadListener(CustomDownloadListener(this))
        webview.setOnTouchListener { v, event -> // TODO Auto-generated method stub
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> if (!v.hasFocus()) {
                    v.requestFocus()
                }
            }
            false
        }
        webview.loadUrl(url)
    }

    fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun urlSchemeIntent(view: WebView, url: String): Boolean {
        if (url.startsWith("intent")) {
            var intent: Intent? = null
            intent = try {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } catch (ex: URISyntaxException) {
                return false
            }

            if (intent?.let { packageManager.resolveActivity(it, 0) } == null) {
                val packagename = intent?.getPackage()
                if (packagename != null) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://search?q=pname:$packagename")
                        )
                    )
                    return true
                }
            }
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(intent?.getDataString()))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                return false
            }
        } else {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: java.lang.Exception) {
                Toast.makeText(mContext, "에러가 발생하였습니다.", Toast.LENGTH_LONG).show()
            }
        }

        return true
    }

    //쿠키 가져오기
    /**
     * url : 읽어올 쿠키 url
     * name : 읽어올 쿠키 name
     */
    fun getCookie(url: String?, name: String?): String? {
        var value: String? = null
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(url)
        if (cookies != null) {
            val temp = cookies.split(";").toTypedArray()
            for (ar1 in temp) {
                name?.let {
                    if (ar1.contains(it)) {
                        val temp1 = ar1.split("=").toTypedArray()
                        value = temp1[1]
                    }
                }
            }
        }
        return value
    }

    inner class FileChooser(
        private val context: Context,
    ) {
        fun show() {
            val chooserIntent = createChooserIntent()
            context.startActivity(chooserIntent)
        }

        private fun createChooserIntent(): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                val mimeTypes = arrayOf("image/*", "application/pdf")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            return Intent.createChooser(intent, "첨부파일 선택")
        }
    }

    inner class CustomDownloadListener(val activity: Activity): DownloadListener {
        override fun onDownloadStart(
            url: String?,
            userAgent: String?,
            contentDisposition: String,
            mimetype: String?,
            contentLength: Long
        ) {
            Utils.Log("userAgent ==> : $userAgent")
            Utils.Log("contentDisposition ==> : $contentDisposition")
            Utils.Log("mimetype ==> : $mimetype")
            //pdf 뷰어가 필요한 경우 웹에서 열어준다.
            if(mimetype.equals("application/pdf")){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }else {
                var content = contentDisposition
                try {
                    val request = DownloadManager.Request(Uri.parse(url))
                    val dm = activity.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    content = URLDecoder.decode(content, "UTF-8") //디코딩
                    //attachment; filename*=UTF-8''뒤에 파일명이있는데 파일명만 추출하기위해 앞에 attachment; filename*=UTF-8''제거
                    var fileName = content.replace("attachment;filename=", "")
                    fileName = fileName.replace("attachment; filename=", "")
                    //;로 끝나면 마지막 ; 삭제
                    if (fileName.endsWith(";"))
                        fileName = fileName.substring(0, fileName.length - 1)

                    request.setMimeType(mimetype)
                    request.addRequestHeader("User-Agent", userAgent)
                    request.setDescription("Downloading File")
                    request.setAllowedOverMetered(true)
                    request.setAllowedOverRoaming(true)
                    request.setTitle(
                        fileName //위에서 디코딩하고 앞에 내용을 자른 최종 파일명
                    )
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        request.setRequiresCharging(false)
                    }*/
                    request.allowScanningByMediaScanner()
                    request.setAllowedOverMetered(true)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName //위에서 디코딩하고 앞에 내용을 자른 최종 파일명
                    )
                    dm.enqueue(request)
                    Utils.Toast(activity, "파일을 다운로드합니다.")
                } catch (e: Exception) {
                    Utils.Log("e ==> : ${e.message}")
                    if (!UtilPermission.isStoragePermission(activity)) {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            Define.STORAGE_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(canGoBack() == true){
            goBack()
        }else {
            finish()
            /*if (!isTwo) {
                Toast.makeText(mContext, getString(R.string.main_finish_toast), Toast.LENGTH_SHORT)
                    .show()
                val timer = FinishTimer(2000, 1) //2초동안 수행
                timer.start()
            } else {
                finish()
            }*/
        }
    }

    private fun canGoBack(): Boolean? {
        return if (webview != null) {
            webview?.canGoBack()
        } else false
    }

    private fun goBack() {
        if (webview != null) {
            webview?.goBack()
        }
    }

    inner class FinishTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            isTwo = false
        }

        override fun onTick(millisUntilFinished: Long) {
        }

        init {
            isTwo = true
        }
    }

    //웹에 디바이스 정보 넘기기
    fun setDeviceInfo(){
        val json = JsonObject()
        val token = SharedData.getSharedData(mContext, SharedData.PUSH_TOKEN, "")
        val deviceId = Utils.getDeviceId(mContext)
        val version = Utils.appVersionName(mContext)
        json.addProperty("deviceToken", token)
        json.addProperty("deviceId", deviceId)
        json.addProperty("aplctnVer", version)
        json.addProperty("mobileHw", "Android")
        json.addProperty("mobileOsCd", Build.VERSION.RELEASE)

        webview.post(Runnable {
            webview.loadUrl("javascript:getDeviceInfo($json)")
        })
    }

    // 멤버십 번호 전달
    private fun setMembershipNo() {
        val membershipNo = GlobalApplication.userInfo?.membershipNo
        val accessToken = GlobalApplication.userInfo?.accessToken

        Log.d("test log", "membershipNo >>> $membershipNo")
        Log.d("test log", "accessToken >>> $accessToken")

        if (membershipNo?.isNotEmpty() == true && accessToken?.isNotEmpty() == true) {
            webview.post(Runnable {
                // Log.d("test log", "222 accessToken >>> $accessToken")
                webview.loadUrl("javascript:getMembershipNo('$membershipNo', '$accessToken')")
            })
        }
    }

    inner class AppScript {
        //종료하기
        @JavascriptInterface
        fun close() {
            finish()
        }

        //디바이스 정보 가져오기
        @JavascriptInterface
        fun callDeviceInfo() {
            setDeviceInfo()
        }

        @JavascriptInterface
        fun setMembershipUserInfo(data: String) {
            runOnUiThread {
                Log.d("test log", "setMembershipUserInfo >>> $data")
                SharedData.setSharedData(mContext, "membershipUserInfo", GlobalApplication.membershipUserInfo.toString())
                GlobalApplication.membershipUserInfo = Gson().fromJson(data, MembershipUserInfo::class.java)
                //Log.d("test log", "GlobalApplication.membershipUserInfo >>> ${GlobalApplication.membershipUserInfo}")
            }
        }

        @JavascriptInterface
        fun callAccessToken(data : String) {
            val accessToken = GlobalApplication.userInfo?.accessToken
            val membershipUserInfo = GlobalApplication.membershipUserInfo

            Log.d("test log", "membershipUserInfo >>> $membershipUserInfo")
            SharedData.setSharedData(mContext, "membershipUserInfo", GlobalApplication.membershipUserInfo.toString())

            runOnUiThread {
                webview.post(Runnable {
                    if (accessToken != null) {
                        Log.d("test log", "333 accessToken >>> $accessToken")
                        webview.loadUrl("javascript:setAccessToken('$accessToken')")
                    }

                    if (membershipUserInfo != null) {
                        val gsonMembershipUserInfo = Gson().toJson(membershipUserInfo)
                        Log.d("test log", "json membershipUserInfo >>> $gsonMembershipUserInfo")
                        webview.loadUrl("javascript:setUserMembershipInfo($gsonMembershipUserInfo)")
                    }
                })
            }
        }

        @JavascriptInterface
        fun callMyPage() {
            runOnUiThread {
                val intent = Intent(mContext, RewardActivity::class.java)
                /*intent.putExtra("userData", userData)*/
                intent.putExtra("userData", GlobalApplication.userInfo)
                startActivity(intent)
            }
        }

        @JavascriptInterface
        fun callMembershipNumber() {
            runOnUiThread {
                setMembershipNo()
            }
        }

        @JavascriptInterface
        fun callKakaoUserInfo() {
            runOnUiThread {

            }
        }

        @JavascriptInterface
        fun logout() {
            runOnUiThread {
                RewardActivity.rewardActivity!!.finish()
                this@WebViewActivity.finish()
                GlobalApplication.userInfo = null
                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("userData", GlobalApplication.userInfo)
                startActivity(intent)
            }
        }


        //유저(로그인) 정보 저장
        @JavascriptInterface
        fun setUserInfo(data: String) {
            runOnUiThread {
                Log.d("test log", "setUserInfo >>> $data")
                val json = JSONObject(data)
                val name = json.get("name").toString()
                val gradeName = json.get("gradeName").toString()
                val membershipNo = json.get("membershipNo").toString()
                val point = json.get("point").toString().toInt()
                val accessToken = json.get("accessToken").toString()

                GlobalApplication.userInfo =
                    TierModel(name, membershipNo, point, gradeName, accessToken)

                SharedData.setSharedData(mContext, "userInfo", GlobalApplication.userInfo.toString())

                val intent = Intent(mContext, RewardActivity::class.java)

                // 메인 액티비티 종료
                MainActivity.mainActivity!!.finishAffinity()

                if (MainActivity.isLoginButtonClicked) {
                    intent.putExtra("userData", GlobalApplication.userInfo)
                    this@WebViewActivity.finish()
                    startActivity(intent)
                }
            }
        }

        //토스트 띄우기
        @JavascriptInterface
        fun callToast(msg: String) {
            Utils.Toast(mContext, msg)
        }

        //앱 업데이트
        @JavascriptInterface
        fun callAppUpdate() {
            val appPackageName = packageName
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
            finish()
        }

        //세로화면
        @JavascriptInterface
        fun callPortrait() {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        //가로화면
        @JavascriptInterface
        fun callLandscape() {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        //카카오 로그인
        @JavascriptInterface
        fun kakaoLogin() {
            Log.d("kakao login", "카카오 로그인 들어옴")

            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("kakao login", "error >>> $error")
                } else if (token != null) {
                    Log.d("kakao login", "카카오계정으로 로그인 성공 ${token.accessToken}")
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.d("kakao login", "error >>> $error")
                        } else if (user != null) {
                            Log.d("kakao login", "name >>> ${user.kakaoAccount?.name}")
                            Log.d("kakao login", "id >>> ${user.id}")
                            Log.d("kakao login", "ci >>> ${user.kakaoAccount?.ci}")

                            // 여기에 던져주는 JSON 설정하기
                            val user1 = user.kakaoAccount
                            Log.d("", "사용자 계정$user1")

                            try {
                                val jsonObject = JSONObject()
                                jsonObject.put("userName", user.kakaoAccount?.name)
                                jsonObject.put("echannelId", user.id)
                                jsonObject.put("userCi", user.kakaoAccount?.ci)
                                webview.post {
                                    webview.loadUrl("javascript:getKakaoInfo($jsonObject)")
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(mContext)) {
                UserApiClient.instance.loginWithKakaoTalk(mContext) { token, error ->
                    if (error != null) {
                        Log.d("kakao login", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        } else {
                            // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                            UserApiClient.instance.loginWithKakaoAccount(
                                mContext,
                                callback = callback
                            )
                        }
                    } else if (token != null) {
                        Log.d("kakao login", "카카오계정으로 로그인 성공 ${token.accessToken}")
                        UserApiClient.instance.me { user, error ->
                            if (error != null) {
                                Log.d("kakao login", "사용자 정보 요청 실패", error)
                            } else if (user != null) {
                                Log.d("kakao login", "name >>> ${user.kakaoAccount?.name}")
                                Log.d("kakao login", "id >>> ${user.id}")
                                Log.d("kakao login", "ci >>> ${user.kakaoAccount?.ci}")

                                // 여기에 던져주는 JSON 설정하기
                                try {
                                    val jsonObject = JSONObject()
                                    jsonObject.put("userName", user.kakaoAccount?.name)
                                    jsonObject.put("echannelId", user.id)
                                    jsonObject.put("userCi", user.kakaoAccount?.ci)
                                    webview.post {
                                        webview.loadUrl("javascript:getKakaoInfo($jsonObject)")
                                    }
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(mContext, callback = callback)
            }
        }
    }

    fun kakaoLogin() {
        Log.d("kakao login", "카카오 로그인 들어옴")

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("kakao login", "error >>> $error")
            } else if (token != null) {
                Log.d("kakao login", "카카오계정으로 로그인 성공 ${token.accessToken}")
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.d("kakao login", "error >>> $error")
                    } else if (user != null) {
                        Log.d("kakao login", "name >>> ${user.kakaoAccount?.name}")
                        Log.d("kakao login", "id >>> ${user.id}")
                        Log.d("kakao login", "ci >>> ${user.kakaoAccount?.ci}")

                        // 여기에 던져주는 JSON 설정하기
                        val user1 = user.kakaoAccount
                        Log.d("", "사용자 계정$user1")

                        try {
                            val jsonObject = JSONObject()
                            jsonObject.put("userName", user.kakaoAccount?.name)
                            jsonObject.put("echannelId", user.id)
                            jsonObject.put("userCi", user.kakaoAccount?.ci)
                            webview.post {
                                webview.loadUrl("javascript:getKakaoInfo($jsonObject)")
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(mContext)) {
            UserApiClient.instance.loginWithKakaoTalk(mContext) { token, error ->
                if (error != null) {
                    Log.d("kakao login", "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    } else {
                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(
                            mContext,
                            callback = callback
                        )
                    }
                } else if (token != null) {
                    Log.d("kakao login", "카카오계정으로 로그인 성공 ${token.accessToken}")
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.d("kakao login", "사용자 정보 요청 실패", error)
                        } else if (user != null) {
                            Log.d("kakao login", "name >>> ${user.kakaoAccount?.name}")
                            Log.d("kakao login", "id >>> ${user.id}")
                            Log.d("kakao login", "ci >>> ${user.kakaoAccount?.ci}")

                            // 여기에 던져주는 JSON 설정하기
                            try {
                                val jsonObject = JSONObject()
                                jsonObject.put("userName", user.kakaoAccount?.name)
                                jsonObject.put("echannelId", user.id)
                                jsonObject.put("userCi", user.kakaoAccount?.ci)
                                webview.post {
                                    webview.loadUrl("javascript:getKakaoInfo($jsonObject)")
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(mContext, callback = callback)
        }
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
    }

    override fun onPause() {
        super.onPause()
        webview.onPause()
    }

    //화면 회전 시 레이아웃 변경이 없도록
    override fun onConfigurationChanged(newConfig: Configuration) {
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
        super.onConfigurationChanged(newConfig)
    }
}