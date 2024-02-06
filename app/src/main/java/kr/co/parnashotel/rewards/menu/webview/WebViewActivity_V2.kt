package kr.co.parnashotel.rewards.menu.webview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kr.co.parnashotel.R
import kr.co.parnashotel.databinding.ActWebviewBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.common.GlobalApplication
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.common.UtilPermission
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.rewards.menu.home.MainActivity
import kr.co.parnashotel.rewards.menu.myPage.RewardActivity
import kr.co.parnashotel.rewards.model.MembershipUserInfoModel
import kr.co.parnashotel.rewards.model.MembershipUserInfoModel_V2
import kr.co.parnashotel.rewards.model.UserInfoModel
import kr.co.parnashotel.rewards.model.UserInfoModel_V2
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date

class WebViewActivity_V2 : AppCompatActivity() {
    private lateinit var mBinding: ActWebviewBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var webview: WebView
    private var isTwo = false
    private var mUrl :String? = null

    // 사진 업로드 관련
    /*var cameraPath = ""
    var mWebViewImageUpload: ValueCallback<Array<Uri>>? = null*/
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    private var mUri: Uri? = null

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
        Log.d("wooryeol", "mUrl >>> $mUrl")
        initWebview(mUrl!!)

        if(pushUrl != null) {
            setMembershipNo()
        }

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

        WebView.setWebContentsDebuggingEnabled(true)
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
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("webViewClient", "onPageFinished >>> $url")
                if (url?.let { backPressedUrl(it) } == false) {
                    mBinding.backBtn.visibility = View.GONE
                } else if (url?.let { backPressedUrl(it) } == true) {
                    mBinding.backBtn.visibility = View.VISIBLE
                }
                super.onPageFinished(view, url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {
                Log.d("webViewClient", "shouldOverrideUrlLoading >>> $url")
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
                        }  else if(url.startsWith("https://www.parnashotel.com/myPage/myPoint/pointsAdjustment/#")) {
                            view.loadUrl("https://www.parnashotel.com/myPage/myPoint/pointsAdjustment/")
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
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams): Boolean {
                Log.d("download Log", "111")
                if (mUploadMessage != null) {
                    Log.d("download Log", "222")
                    mUploadMessage?.onReceiveValue(null)
                    mUploadMessage = null
                }
                Log.d("download Log", "333")
                mUploadMessage = filePathCallback

                if (UtilPermission.isCameraPermission(mActivity)) {
                    Log.d("download Log", "444")
                    chooserPicture()
                } else {
                    if (mUploadMessage != null) {
                        Log.d("download Log", "555")
                        mUploadMessage?.onReceiveValue(null)
                        mUploadMessage = null
                    }

                    mActivity.requestPermissions(arrayOf(Manifest.permission.CAMERA), Define.STORAGE_REQUEST_CODE)
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

    /*fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }*/

    @SuppressLint("QueryPermissionsNeeded")
    fun chooserPicture(){
        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent?.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile(mContext)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.message?.let { it1 -> Utils.Toast(mContext, it1) }
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                mUri = FileProvider.getUriForFile(mContext, Define.AUTHORITY, it)
                takePictureIntent?.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            }
        }

        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "image/*"

        val intentArray: Array<Intent?>
        intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)

        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "사진 가져올 방법을 선택해 주세요.")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        startActivityForResult(chooserIntent, Define.GET_PHOTO_REQUEST_CODE)
    }

    @SuppressLint("SimpleDateFormat")
    fun createImageFile(context: Context): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("SE_${timeStamp}", ".jpg",storageDir)
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

    private fun backPressedUrl(url: String):Boolean {
        val urlList = arrayOf(
            "&errorCode=&orderNo=",
            "errorCode=400_0",
            "easypay.co.kr",
            "online-pay.kakao.com",
            "ansimclick.hyundaicard.com",
            "acs.hanacard.co.kr",
            "ui.vpay.co.kr",
            "dacs.wooricard.com",
            "vbv.shinhancard.com",
            "sps.lottecard.co.kr",
            "vbv.samsungcard.co.kr",
            "accesscontrol.citibank.co.kr",
            "pay.kbcard.com",
            "?resCd=0000&errorCode=0000&"
            )
        for (it in urlList) {
            if (url.contains(it)) {
                return false
            }
        }
        return true
    }

    override fun onBackPressed() {
        if(canGoBack() == true){
            if (webview.url?.let { backPressedUrl(it) } == true) {
                goBack()
            }
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
        val userInfoModel = UserInfoModel_V2().loadUserInfo(mContext)

        if(userInfoModel?.accessToken != "") {
            webview.loadUrl("javascript:getMembershipNo('${userInfoModel?.membershipNo}', '${userInfoModel?.accessToken}')")
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
                Log.d("wooryeol", "membershipUserInfo >>> $data")
                // GlobalApplication.membershipUserInfo = Gson().fromJson(data, MembershipUserInfoModel::class.java)

                val json = JSONObject(data)
                val membershipYn = json.get("membershipYn").toString()
                val membershipId = json.get("membershipId").toString()
                val membershipNo = json.get("membershipNo").toString()
                val memberName = json.get("memberName").toString()
                val memberGender = json.get("memberGender").toString()
                val memberEmail = json.get("memberEmail").toString()
                val memberMobile = json.get("memberMobile").toString()
                val memberFirstName = json.get("memberFirstName").toString()
                val memberLastName = json.get("memberLastName").toString()
                val employeeStatus = json.get("employeeStatus").toString()
                val recommenderStatus = json.get("recommenderStatus").toString()
                val temporaryYn = if(data.contains("temporaryYn")) {
                    json.get("temporaryYn").toString()
                } else {
                    json.get("gradeName").toString()
                }

                val membershipUserInfoModelV2 = MembershipUserInfoModel_V2(
                    membershipYn,
                    membershipId,
                    membershipNo,
                    memberName,
                    memberGender,
                    memberEmail,
                    memberMobile,
                    memberFirstName,
                    memberLastName,
                    employeeStatus,
                    recommenderStatus,
                    temporaryYn,
                )

                membershipUserInfoModelV2.save(mContext)
            }
        }

        @JavascriptInterface
        fun callAccessToken() {
            runOnUiThread {
                val userInfoModel = UserInfoModel_V2()
                val membershipUserInfo = MembershipUserInfoModel_V2()

                // Log.d("wooryeol", "callAccessToken data >>> $data")
                Log.d("wooryeol", "userInfoModel.accessToken >>> ${userInfoModel.loadUserInfo(mContext)?.accessToken}")

                if(userInfoModel.loadUserInfo(mContext)?.accessToken != null) {
                    webview.loadUrl("javascript:setAccessToken('${userInfoModel.loadUserInfo(mContext)?.accessToken}')")
                }

                if(membershipUserInfo.LoadMembershipUserInfo(mContext) != null) {
                    val gsonMembershipUserInfo = Gson().toJson(membershipUserInfo.LoadMembershipUserInfo(mContext))
                    webview.loadUrl("javascript:setUserMembershipInfo($gsonMembershipUserInfo)")
                }
            }
        }

        @JavascriptInterface
        fun callMyPage() {
            runOnUiThread {
                val intent = Intent(mContext, RewardActivity::class.java)
                /*intent.putExtra("userData", userData)*/
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
                if (!GlobalApplication.isLoggedIn) {
                    RewardActivity.rewardActivity!!.finish()
                }
                this@WebViewActivity_V2.finish()

                GlobalApplication.isLoggedIn = false
                UserInfoModel_V2().clearUserInfo(mContext)
                MembershipUserInfoModel_V2().clearMembershipUserInfo(mContext)

                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("userData", GlobalApplication.userInfo)
                startActivity(intent)
            }
        }


        @JavascriptInterface
        fun setUserInfoTest(data: String) {
            Log.d("wooryeol", "setUserInfo222 >>> $data")
        }

        //유저(로그인) 정보 저장
        @JavascriptInterface
        fun setUserInfo(data: String) {
            runOnUiThread {
                Log.d("wooryeol", "setUserInfo02 >>> $data")
                GlobalApplication.userInfo = Gson().fromJson(data, UserInfoModel_V2::class.java)

                val json = JSONObject(data)
                val name = json.get("name").toString()
                val gradeName = json.get("gradeName").toString()
                val membershipNo = json.get("membershipNo").toString()
                val coupon = json.get("coupon").toString().toInt()
                val point = json.get("point").toString().toInt()
                val accessToken = json.get("accessToken").toString()
                val userInfoModel = UserInfoModel_V2(
                    name,
                    gradeName,
                    membershipNo,
                    point,
                    coupon,
                    accessToken,
                )
                userInfoModel.save(mContext)

                // 메인 액티비티 종료
                MainActivity.mainActivity!!.finishAffinity()

                if (MainActivity.isLoginButtonClicked) {
                    val intent = Intent(mContext, RewardActivity::class.java)
                    intent.putExtra("userData", userInfoModel)
                    startActivity(intent)
                    finish()
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Define.GET_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data)
                    return
                }

                if (data == null) {    //카메라 촬영
                    mUri?.let {
                        mUploadMessage?.onReceiveValue(arrayOf(it))
                    }
                } else {    //앨범 가져오기
                    mUploadMessage?.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(
                            resultCode,
                            data
                        )
                    )
                }
                mUploadMessage = null
            }
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