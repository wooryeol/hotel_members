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
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Message
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
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kr.co.parnashotel.R
import kr.co.parnashotel.databinding.ActWebviewBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.common.UtilPermission
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.rewards.menu.home.MainActivity
import kr.co.parnashotel.rewards.menu.myPage.RewardActivity
import kr.co.parnashotel.rewards.model.TierModel
import org.json.JSONObject
import java.net.URLDecoder

class WebViewActivity : AppCompatActivity() {
    private lateinit var mBinding: ActWebviewBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var webview: WebView
    private var isTwo = false
    private var mUrl :String? = null

    private var userData : TierModel? = null

    //사진 업로드 관련
    /*private var mUploadMessage: ValueCallback<Array<Uri>>? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActWebviewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        webview = mBinding.webview

        //앱 사용중 화면 켜짐
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val pushUrl = intent.getStringExtra("index")
        mUrl = pushUrl ?: Define.DOMAIN
        initWebview(mUrl!!)
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun initWebview(url: String) {
        val settings = webview.settings
        settings.javaScriptEnabled = true
        //settings.pluginState = WebSettings.PluginState.ON
        // window.open 메서드를 이용할 때의 동작을 설정할 수 있게 한다
        settings.setSupportMultipleWindows(true)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //동영상 재생 문제
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            //간헐적인 동영상 플레이 장애처리
            settings.mediaPlaybackRequiresUserGesture = false

            //디버깅용 WebView 셋팅
            WebView.setWebContentsDebuggingEnabled(true)
        }

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
        webview.webChromeClient = FullscreenableChromeClient(this)
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
        webview.webViewClient = object : WebViewClient() {
            // 유효하지 않은 것으로 판단되는 인증서의 문제가 없는지 판단하고 문제가 없을 경우 preceed()를 호출
            // 아닌 경우 cancel을 처리해야만 구글에서 업데이트를 허락함
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                //기존에 인증서가 유효하지 않아도 그냥 통과시켰다.
                //handler.proceed();
                //인증
                try {
                    //AlertDialog.Builder 뜨기전에 웹뷰 종류시 강제종류가 일어나
                    val builder = AlertDialog.Builder(mContext)
                    builder.setMessage("신뢰하는 보안 인증서가 아닙니다.\n계속 진행 하시겠습니까?")
                    builder.setPositiveButton("진행") { _, _ -> handler.proceed() }
                    builder.setNegativeButton("취소") { _, _ -> handler.cancel() }
                    val dialog = builder.create()
                    dialog.show()
                } catch (e: Exception) {
                    Utils.LogLine(e.message.toString())
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url)
                CookieManager.getInstance().flush()
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //Utils.Log("url ==> : "+url)
                if (url.startsWith("se://")) {
                    val uri = Uri.parse(url)
                    val param = uri.getQueryParameter("view")
                    if (param == "close") setResult(RESULT_OK)
                    finish()
                    return true
                }
                return false
            }
        }

        webview.loadUrl(url)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        request.setRequiresCharging(false)
                    }
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
                            kr.co.parnashotel.rewards.common.Define.STORAGE_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    inner class FullscreenableChromeClient(var mActivity: Activity) : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalOrientation = 0
        private var mFullscreenContainer: FrameLayout? = null
        private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)


        @SuppressLint("SetJavaScriptEnabled")
        override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message): Boolean {
            val newWebView = WebView(mActivity)
            val settings = newWebView.settings
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)
            settings.builtInZoomControls = true
            settings.setSupportZoom(true)
            settings.useWideViewPort = true
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val browserIntent = Intent(Intent.ACTION_VIEW)
                    browserIntent.data = Uri.parse(url)
                    mActivity.startActivity(browserIntent)
                    //return super.shouldOverrideUrlLoading(view, url);
                    return true
                }
            }
            return true
            //return super.onCreateWindow(view, dialog, userGesture, resultMsg);
        }

        override fun onCloseWindow(view: WebView) {
            super.onCloseWindow(view)
        }

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            drawPopup(message, result)
            return true
        }

        override fun onJsBeforeUnload(view: WebView, url: String,
                                      message: String, result: JsResult
        ): Boolean {
            // TODO Auto-generated method stub
            return super.onJsBeforeUnload(view, url, message, result)
        }

        override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
            // TODO Auto-generated method stub
            drawConfirmPopup(message, result)
            return true
        }

        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            // TODO Auto-generated method stub
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }

        override fun onJsTimeout(): Boolean {
            // TODO Auto-generated method stub
            return super.onJsTimeout()
        }

        /* 동영상 전체보기를 위한 셋팅 */
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden()
                    return
                }

                //영상 전체화면 가로모드
                mOriginalOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val decor = mActivity.window.decorView as FrameLayout
                mFullscreenContainer = FullscreenHolder(mActivity)
                mFullscreenContainer?.addView(view, COVER_SCREEN_PARAMS)
                decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS)
                mCustomView = view
                setFullscreen(true)
                mCustomViewCallback = callback
                mActivity.requestedOrientation = mOriginalOrientation
            }
            super.onShowCustomView(view, callback)
        }

        override fun onShowCustomView(view: View, requestedOrientation: Int, callback: CustomViewCallback) {
            this.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            if (mCustomView == null) {
                return
            }

            //영상 전체화면 종료 후 원상태로 복귀
            mOriginalOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setFullscreen(false)
            val decor = mActivity.window.decorView as FrameLayout
            decor.removeView(mFullscreenContainer)
            mFullscreenContainer = null
            mCustomView = null
            mCustomViewCallback?.onCustomViewHidden()
            mActivity.requestedOrientation = mOriginalOrientation
        }

        private fun setFullscreen(enabled: Boolean) {
            val win = mActivity.window
            val winParams = win.attributes
            val bits = WindowManager.LayoutParams.FLAG_FULLSCREEN
            if (enabled) {
                winParams.flags = winParams.flags or bits
                //상태바와 하단 소프트키 숨기기/보여주기
                mCustomView?.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            } else {
                winParams.flags = winParams.flags and bits.inv()
                if (mCustomView != null) {
                    mCustomView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
            win.attributes = winParams
        }

        inner class FullscreenHolder(ctx: Context) : FrameLayout(ctx) {
            override fun onTouchEvent(evt: MotionEvent): Boolean {
                return true
            }

            init {
                setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.black))
            }
        }

        private fun drawPopup(msg: String, result: JsResult) {
            val popup = AlertDialog.Builder(ContextThemeWrapper(mActivity, R.style.DialogTheme))
            popup.setTitle("알림")
            popup.setMessage(msg)
            popup.setPositiveButton("확인") { dialog, which -> // TODO Auto-generated method stub
                result.confirm()
            }
            popup.setOnCancelListener { result.cancel() }
            popup.setCancelable(true)
            val dialog = popup.show()
            //메세지 크기
            val textView = dialog.findViewById<View>(android.R.id.message) as TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            //버튼 크기와 색상
            val positive: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setTextColor(Utils.getColor(mActivity, R.color.black))
            positive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            dialog.show()
        }

        private fun drawConfirmPopup(msg: String, result: JsResult) {
            val popup = AlertDialog.Builder(ContextThemeWrapper(mActivity, R.style.DialogTheme))
            popup.setTitle("알림")
            popup.setMessage(msg)
            popup.setPositiveButton("확인") { dialog, which -> // TODO Auto-generated method stub
                result.confirm()
            }
            popup.setNegativeButton("취소") { dialog, which -> // TODO Auto-generated method stub
                result.cancel()
            }
            popup.setOnCancelListener { result.cancel() }
            popup.setCancelable(true)
            val dialog = popup.show()
            //메세지 크기
            val textView = dialog.findViewById<View>(android.R.id.message) as TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            //버튼 크기와 색상
            val positive: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setTextColor(Utils.getColor(mActivity, R.color.black))
            positive.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            val negative: Button = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negative.setTextColor(Utils.getColor(mActivity, R.color.black))
            negative.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            dialog.show()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webview.evaluateJavascript("javascript:getDeviceInfo($json)",null)
            } else {
                webview.loadUrl("javascript:getDeviceInfo($json)")
            }
        })
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
        fun callMyPage() {
            runOnUiThread {
                val intent = Intent(mContext, RewardActivity::class.java)
                intent.putExtra("userData", userData)
                startActivity(intent)
            }
        }

        //유저(로그인) 정보 저장
        @JavascriptInterface
        fun setUserInfo(data: String) {
            runOnUiThread {

                Log.d("test log", "setUserInfo >>> $data")
                val json = JSONObject(data)
                val name  = json.get("name").toString()
                val gradeName  = json.get("gradeName").toString()
                val membershipNo  = json.get("membershipNo").toString()
                val point  = json.get("point").toString().toInt()

                userData = TierModel(name, membershipNo, point, gradeName)

                val intent = Intent(mContext, RewardActivity::class.java)

                // 메인 액티비티 종료
                MainActivity.mainActivity!!.finishAffinity()

                if (MainActivity.isLoginButtonClicked) {
                    intent.putExtra("userData", userData)
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
        fun callAppUpdate(){
            val appPackageName = packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
            finish()
        }

        //세로화면
        @JavascriptInterface
        fun callPortrait(){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        //가로화면
        @JavascriptInterface
        fun callLandscape(){
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
                    UserApiClient.instance.me{ user, error ->
                        if (error != null){
                            Log.d("kakao login", "error >>> $error")
                        } else if (user != null){
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        webview.evaluateJavascript(
                                            "javascript:getKakaoInfo($jsonObject)", null
                                        )
                                    } else {
                                        webview.loadUrl("javascript:getKakaoInfo($jsonObject)")
                                    }
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
                            UserApiClient.instance.loginWithKakaoAccount(mContext, callback = callback)
                        }
                    } else if (token != null) {
                        Log.d("kakao login", "카카오계정으로 로그인 성공 ${token.accessToken}")
                        UserApiClient.instance.me { user, error ->
                            if (error != null) {
                                Log.d("kakao login", "사용자 정보 요청 실패", error)
                            }
                            else if (user != null) {
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
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            webview.evaluateJavascript(
                                                "javascript:getKakaoInfo($jsonObject)", null
                                            )
                                        } else {
                                            webview.loadUrl("javascript:getKakaoInfo($jsonObject)")
                                        }
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