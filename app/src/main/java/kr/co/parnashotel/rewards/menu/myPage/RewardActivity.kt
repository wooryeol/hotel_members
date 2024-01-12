package kr.co.parnashotel.rewards.menu.myPage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kr.co.parnashotel.R
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.databinding.ActivityRewardBinding
import kr.co.parnashotel.databinding.CellRewardBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.menu.home.MainActivity
import kr.co.parnashotel.rewards.menu.webview.WebViewActivity
import kr.co.parnashotel.rewards.model.HotelModel
import kr.co.parnashotel.rewards.model.TierModel
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("ResourceType")
class RewardActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private var _mBinding: ActivityRewardBinding? = null
    private val mBinding get() = _mBinding!!

    private var currentPosition = 0
    private var myHandler = MyHandler()
    private val intervalTime = 1500.toLong()

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mBinding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(_mBinding!!.root)

        mContext = this

        // 링크 걸어주는 곳
        mBinding.rsvn.setOnClickListener {
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.rsvn}")
        }
        mBinding.dining.setOnClickListener {
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.dining}")
        }
        mBinding.search.setOnClickListener {
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.search}")
        }
        mBinding.reservationCheck.setOnClickListener {
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.reservationCheck}")
        }
        mBinding.dashBoard.setOnClickListener{
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.dashBoard}")
        }

        //뷰 페이저
        val list: List<HotelModel> = arrayListOf(
            HotelModel(R.drawable.grand, "", "그랜드인터컨티넨탈", "${Define.DOMAIN}?hotelCode=21&lang=kor&prodID=163"),
            HotelModel(R.drawable.coex, "", "인터컨티넨탈 코엑스", "${Define.DOMAIN}?hotelCode=23&lang=kor&prodID=164"),
            HotelModel(R.drawable.parnas_jeju, "", "파르나스 호텔 제주", "${Define.DOMAIN}?hotelCode=26&lang=kor&prodID=170"),
            HotelModel(R.drawable.pangyo, "", "나인트리 판교", "${Define.DOMAIN}?hotelCode=27&lang=kor&prodID=165"),
            HotelModel(R.drawable.myoungdong_2, "", "나인트리 명동2", "${Define.DOMAIN}?hotelCode=29&lang=kor&prodID=167"),
            HotelModel(R.drawable.insadong, "", "나인트리 인사동", "${Define.DOMAIN}?hotelCode=30&lang=kor&prodID=168"),
            HotelModel(R.drawable.myoungdong_1, "", "나인트리 명동", "${Define.DOMAIN}?hotelCode=28&lang=kor&prodID=166"),
            HotelModel(R.drawable.dongdaemoon, "", "나인트리 동대문", "${Define.DOMAIN}?hotelCode=31&lang=kor&prodID=169")
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
            })
        }

        getSetting()
    }

    private fun getSetting(){
        val userDataIntent = intent
        val userData = userDataIntent.getSerializableExtra("userData") as TierModel
        Log.d("test log", "userData >>> $userData")

        MainActivity.isLoginButtonClicked = false

        val name  = userData.name
        val gradeName  = userData.gradeName
        val membershipNo  = userData.membershipNo
        val point  = userData.point

        /*val name  = getUserInfo().name
        val gradeName  = getUserInfo().gradeName
        val membershipNo  = getUserInfo().membershipNo
        val point  = getUserInfo().point*/

        // 회원명
        mBinding.userName.text = name

        // 회원 번호
        mBinding.memNumber.text = membershipNo

        // 포인트
        val formattedPoint = NumberFormat.getNumberInstance(Locale.getDefault()).format(point)
        mBinding.btmSheetPoint.text = formattedPoint

        // 현재 grade bubble 위치
        currentGrade(50)

        // 바코드 생성
        createBarcode(membershipNo)

        // 티어별 ui 설정
        tierSetting(gradeName)
    }

    private fun tierSetting(gradeName: String){
        when (gradeName) {
            "클럽" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_c))
                window.statusBarColor = Color.parseColor(getString(R.color.grade_c))
                mBinding.gradeImg.setImageResource(R.drawable.grade_c)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_club)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_club)
            }
            "v1" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v1))
                window.statusBarColor = Color.parseColor(getString(R.color.grade_v1))
                mBinding.gradeImg.setImageResource(R.drawable.grade_v1)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v1)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v1)
            }
            "v2" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v2))
                window.statusBarColor = Color.parseColor(getString(R.color.grade_v2))
                mBinding.gradeImg.setImageResource(R.drawable.grade_v2)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v2)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v2)
            }
            "v3" -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v3))
                window.statusBarColor = Color.parseColor(getString(R.color.grade_v3))
                mBinding.gradeImg.setImageResource(R.drawable.grade_v3)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v3)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v3)
            }
            else -> {
                mBinding.header.setBackgroundColor(mContext.resources.getColor(R.color.grade_v4))
                window.statusBarColor = Color.parseColor(getString(R.color.grade_v4))
                mBinding.gradeImg.setImageResource(R.drawable.grade_v4)
                mBinding.grade.setImageResource(R.drawable.speech_bubble_v4)
                mBinding.progressBar.setImageResource(R.drawable.progress_bar_v4)
            }
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

    private inner class MyHandler: Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (currentPosition == 7) {
                mBinding.viewPager.setCurrentItem(0, true)
                currentPosition = 0
            } else {
                mBinding.viewPager.setCurrentItem(++currentPosition, true)
                autoScrollStart(intervalTime)
            }
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

        inner class PagerViewHolder(private val binding: CellRewardBinding) : RecyclerView.ViewHolder(binding.root){
            fun bind(itemModel: HotelModel){

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
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):PagerViewHolder {
            val binding = CellRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PagerViewHolder(binding)
        }

        override fun getItemCount(): Int = datalist.size

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
            holder.bind(datalist[position])
        }
    }
}