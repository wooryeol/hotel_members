package kr.co.parnashotel.rewards.menu.myPage

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import android.widget.Scroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.co.parnashotel.R
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.databinding.ActivityRewardBinding
import kr.co.parnashotel.databinding.CellRewardBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.menu.home.MainActivity
import kr.co.parnashotel.rewards.menu.webview.WebViewActivity_V2
import kr.co.parnashotel.rewards.model.HotelModel
import kr.co.parnashotel.rewards.model.UserInfoModel_V2
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class RewardActivity : AppCompatActivity() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        var rewardActivity: RewardActivity? = null
    }


    private lateinit var mContext: Context
    private var _mBinding: ActivityRewardBinding? = null
    private var currentPosition = 0
    private var userInfoModel: UserInfoModel_V2? = null
    private val mBinding get() = _mBinding!!
    private var myHandler = MyHandler()
    private val intervalTime = 3000L

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mBinding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(_mBinding!!.root)

        mContext = this
        rewardActivity = this

        // 링크 걸어주는 곳
        mBinding.rsvn.setOnClickListener {
            loginMove(Define.DOMAIN+Define.rsvn)
        }
        mBinding.dining.setOnClickListener {
            loginMove(Define.DOMAIN+Define.dining)
        }
        mBinding.search.setOnClickListener {
            loginMove(Define.DOMAIN+Define.search)
        }
        mBinding.reservationCheck.setOnClickListener {
            loginMove(Define.DOMAIN+Define.loggedInReservationCheck)
        }
        mBinding.dashBoard.setOnClickListener{
            loginMove(Define.DOMAIN+Define.dashBoard)
        }

        //뷰 페이저
        val list: List<HotelModel> = arrayListOf(
            HotelModel(R.drawable.grand_1, "", "그랜드 인터컨티넨탈 서울 파르나스", "${Define.DOMAIN}?hotelCode=21&lang=kor"),
            HotelModel(R.drawable.coex_2, "", "인터컨티넨탈 서울 코엑스", "${Define.DOMAIN}?hotelCode=23&lang=kor&"),
            HotelModel(R.drawable.parnas_jeju_3, "", "파르나스 호텔 제주", "${Define.DOMAIN}?hotelCode=26&lang=kor"),
            HotelModel(R.drawable.pangyo, "", "나인트리 프리미어 호텔 서울 판교", "${Define.DOMAIN}?hotelCode=27&lang=kor"),
            HotelModel(R.drawable.myoungdong_2, "", "나인트리 프리미어 호텔 명동 II", "${Define.DOMAIN}?hotelCode=29&lang=kor"),
            HotelModel(R.drawable.insadong, "", "나인트리 프리미어 호텔 인사동", "${Define.DOMAIN}?hotelCode=30&lang=kor"),
            HotelModel(R.drawable.myoungdong_1, "", "나인트리 호텔 명동", "${Define.DOMAIN}?hotelCode=28&lang=kor"),
            HotelModel(R.drawable.dongdaemoon, "", "나인트리 호텔 동대문", "${Define.DOMAIN}?hotelCode=31&lang=kor")
        )

        val viewPagerAdapter = ViewPagerAdapter(mContext)
        viewPagerAdapter.datalist = list
        mBinding.viewPager.adapter = viewPagerAdapter
        mBinding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로
        mBinding.dotsIndicator.attachTo(mBinding.viewPager)

        mBinding.viewPager.apply {
            registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    when(state) {
                        ViewPager2.SCROLL_STATE_IDLE -> autoScrollStart(intervalTime)
                        ViewPager2.SCROLL_STATE_DRAGGING -> autoScrollStop()
                    }
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // 사용자가 페이지를 변경할 때마다 currentPosition 업데이트
                    currentPosition = position
                }
            })
        }

        mBinding.viewPager.setPageTransformer { page, position ->
            page.apply {
                translationX = width * -position
                alpha = when {
                    position <= -1.0F || position >= 1.0F -> 0.0F
                    position == 0.0F -> 1.0F
                    else -> 1.0F - abs(position)
                }
            }
            Log.d("ViewPagerTransform", "Page position: $position, Alpha: ${page.alpha}")
        }

        getSetting()
    }

    private fun loginMove(domain: String){
        val intent = Intent(mContext, WebViewActivity_V2::class.java)
        intent.putExtra("index", domain)
        startActivity(intent)
    }
    private fun getSetting(){
        /*val userDataIntent = intent
        val userData = userDataIntent.getSerializableExtra("userData") as TierModel*/
        // Log.d("wooryeol", "userData >>> ${GlobalApplication.userInfo}")

        MainActivity.isLoginButtonClicked = false

        userInfoModel = UserInfoModel_V2().loadUserInfo(mContext)
        val name  = userInfoModel?.name
        val gradeName  = userInfoModel?.gradeName
        val membershipNo  = userInfoModel?.membershipNo
        val point  = userInfoModel?.point
        val percent  = userInfoModel?.percent

        // 회원명
        mBinding.userName.text = name

        // 회원 번호
        mBinding.memNumber.text = membershipNo

        // 포인트
        val formattedPoint = NumberFormat.getNumberInstance(Locale.getDefault()).format(point)
        mBinding.btmSheetPoint.text = formattedPoint

        // 바코드 생성
        createBarcode(membershipNo!!)

        // 티어별 ui 설정
        tierSetting(gradeName!!, percent)
    }

    private fun tierSetting(gradeName: String, percent: Int?){
        when (gradeName) {
            "클럽" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_c))
                //window.statusBarColor = Color.parseColor(getString(R.color.grade_c))
                window.statusBarColor = mContext.resources.getColor(R.color.grade_c)
                mBinding.gradeImg.setImageResource(R.drawable.grade_c)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_club)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_club)

                gradeSetting(percent!!.toFloat(), 25f)
            }
            "V1" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v1))
                window.statusBarColor = mContext.resources.getColor(R.color.grade_v1)
                mBinding.gradeImg.setImageResource(R.drawable.grade_v1)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v1)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v1)

                gradeSetting(25f, 50f)
            }
            "V2" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v2))
                window.statusBarColor = mContext.resources.getColor(R.color.grade_v2)
                mBinding.gradeImg.setImageResource(R.drawable.grade_v2)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v2)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v2)

                gradeSetting(50f, 75f)
            }
            "V3" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v3))
                window.statusBarColor = mContext.resources.getColor(R.color.grade_v3)
                mBinding.gradeImg.setImageResource(R.drawable.grade_v3)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v3)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v3)

                gradeSetting(75f, 100f)
            }
            else -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v4))
                window.statusBarColor = mContext.resources.getColor(R.color.grade_v4)
                mBinding.gradeImg.setImageResource(R.drawable.grade_v4)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v4)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v4)

                // gradeSetting(100)
                currentGrade(100)
            }
        }
    }

    private fun gradeSetting(startGrade: Float, maxGrade: Float) {
        val percent  = userInfoModel?.percent
        // let rangePercent: CGFloat = 25 // 각 등급의 퍼센트 범위
        // let totalValue: CGFloat = ((CGFloat(percent) - startGrade) / rangePercent) * 100

        if (percent != null) {
            /*if(percent > grade) {
                currentGrade((percent - grade) * 4)
            } else {
                currentGrade(percent)
            }*/

            val rangePercent = 25f
            val totalValue = ((percent.toFloat() - startGrade) / rangePercent) * 100
            currentGrade(totalValue.toInt())
        }
    }

    override fun onResume() {
        super.onResume()
        autoScrollStart(intervalTime)
    }

    override fun onPause() {
        super.onPause()
        autoScrollStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _mBinding = null
    }

    // 뷰페이저 자동스크롤
    private fun autoScrollStart(intervalTime: Long) {
        myHandler.removeMessages(0)
        myHandler.sendEmptyMessageDelayed(0, intervalTime)
    }

    private fun autoScrollStop() {
        myHandler.removeMessages(0)
    }

    @SuppressLint("HandlerLeak")
    private inner class MyHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (currentPosition == 7) {
                mBinding.viewPager.setCurrentItem(0, 500, false)
                currentPosition = 0
            } else {
                mBinding.viewPager.setCurrentItem(++currentPosition, 500)
                autoScrollStart(intervalTime)
            }

            // 다음 자동 슬라이드 시작
            autoScrollStart(intervalTime)
        }
    }

    private fun createBarcode(barcodeNumber: String){
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 590f,
            resources.displayMetrics
        )
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 80f,
            resources.displayMetrics
        )
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(barcodeNumber.toString(), BarcodeFormat.CODE_128, widthPx.toInt(), heightPx.toInt())
        mBinding.barcode.setImageBitmap(bitmap)
    }

    // grade의 위치를 바꿔주는 곳
    private fun currentGrade(gradeRate: Int) {
        val progressBar = mBinding.progressBar
        val grade = mBinding.grade

        val layoutParams = grade.layoutParams as LinearLayout.LayoutParams

        progressBar.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                progressBar.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val totalWidth = progressBar.width
                val desiredPosition = ( gradeRate / 100f) * totalWidth

                layoutParams.leftMargin = desiredPosition.toInt()
                grade.layoutParams = layoutParams
            }
        })
    }

    class ViewPagerAdapter(context: Context) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
        var datalist: List<HotelModel> = java.util.ArrayList()
        var mContext = context

        inner class PagerViewHolder(private val binding: CellRewardBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(itemModel: HotelModel) {

                itemView.setOnClickListener {
                    Utils.moveToPage(mContext, itemModel.url)
                }
                binding.imageView.setImageResource(itemModel.img)
                binding.cellTitle.text = itemModel.title
                binding.cellTitle.bringToFront()
                when (itemModel.data?.get(0)?.gradeName) {
                    "클럽" -> {
                        binding.cellTitle.setBackgroundColor(mContext.resources.getColor(R.color.grade_c))
                    }

                    "v1" -> {
                        binding.cellTitle.setBackgroundColor(mContext.resources.getColor(R.color.grade_v1))
                    }

                    "v2" -> {
                        binding.cellTitle.setBackgroundColor(mContext.resources.getColor(R.color.grade_v2))
                    }

                    "v3" -> {
                        binding.cellTitle.setBackgroundColor(mContext.resources.getColor(R.color.grade_v3))
                    }

                    else -> {
                        binding.cellTitle.setBackgroundColor(mContext.resources.getColor(R.color.grade_v4))
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
            val binding =
                CellRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PagerViewHolder(binding)
        }

        override fun getItemCount(): Int = datalist.size

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
            holder.bind(datalist[position])
        }
    }

    fun ViewPager2.setCurrentItem(
        item: Int,
        duration: Long,
        smooth: Boolean = true, // smooth 파라미터 추가
        interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
        pagePxWidth: Int = width, // Default value taken from getWidth() from ViewPager2 view
        pagePxHeight: Int = height
    ) {
        if (smooth) {
            val pxToDrag: Int = if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                pagePxWidth * (item - currentItem)
            } else {
                pagePxHeight * (item - currentItem)
            }

            val animator = ValueAnimator.ofInt(0, pxToDrag).apply {
                var previousValue = 0
                addUpdateListener { valueAnimator ->
                    val currentValue = valueAnimator.animatedValue as Int
                    val currentPxToDrag = (currentValue - previousValue).toFloat()
                    fakeDragBy(-currentPxToDrag)
                    previousValue = currentValue
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        beginFakeDrag()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        endFakeDrag()
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                this.interpolator = interpolator
                this.duration = duration
            }
            animator.start()
        } else {
            // smooth가 false일 경우 기본 전환 메커니즘 사용
            setCurrentItem(item, false)
        }
    }
}