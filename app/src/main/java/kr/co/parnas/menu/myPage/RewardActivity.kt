package kr.co.parnas.menu.myPage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kr.co.parnas.R
import kr.co.parnas.common.Utils
import kr.co.parnas.databinding.ActivityRewardBinding
import kr.co.parnas.databinding.CellRewardBinding
import kr.co.parnas.network.model.HotelModel

@SuppressLint("ResourceType")
class RewardActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private var _mBinding: ActivityRewardBinding? = null
    private val mBinding get() = _mBinding!!

    private var currentPosition = 0
    private var myHandler = MyHandler()
    private val intervalTime = 1500.toLong()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mBinding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(_mBinding!!.root)

        mContext = this

        // 상태바 색상
        window.statusBarColor = Color.parseColor(getString(R.color.grade_c))

        // 현재 grade
        currentGrade(0)

        //뷰 페이저
        val list: List<HotelModel> = arrayListOf(
            HotelModel(R.drawable.grand, "", "그랜드인터컨티넨탈", "https://parnashotel.com?hotelCode=21&lang=kor&prodID=163"),
            HotelModel(R.drawable.coex, "", "인터컨티넨탈 코엑스", "https://parnashotel.com?hotelCode=23&lang=kor&prodID=164"),
            HotelModel(R.drawable.parnas_jeju, "", "파르나스 호텔 제주", "https://parnashotel.com?hotelCode=26&lang=kor&prodID=170"),
            HotelModel(R.drawable.pangyo, "", "나인트리 판교", "https://parnashotel.com?hotelCode=27&lang=kor&prodID=165"),
            HotelModel(R.drawable.myoungdong_2, "", "나인트리 명동2", "https://parnashotel.com?hotelCode=29&lang=kor&prodID=167"),
            HotelModel(R.drawable.insadong, "", "나인트리 인사동", "https://parnashotel.com?hotelCode=30&lang=kor&prodID=168"),
            HotelModel(R.drawable.myoungdong_1, "", "나인트리 명동", "https://parnashotel.com?hotelCode=28&lang=kor&prodID=166"),
            HotelModel(R.drawable.dongdaemoon, "", "나인트리 동대문", "https://parnashotel.com?hotelCode=31&lang=kor&prodID=169")
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

        // 바코드 생성
        createBarcode()

        // 링크 걸어주는 곳
        mBinding.rsvn.setOnClickListener {
            Utils.moveToPage(mContext, getString(R.string.rsvn))
        }
        mBinding.dining.setOnClickListener {
            Utils.moveToPage(mContext, getString(R.string.dining))
        }
        mBinding.search.setOnClickListener {
            Utils.moveToPage(mContext, getString(R.string.search))
        }
        mBinding.reservationCheck.setOnClickListener {
            Utils.moveToPage(mContext, getString(R.string.reservation_check))
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

    private fun createBarcode(){
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 590f,
            resources.displayMetrics
        )
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 80f,
            resources.displayMetrics
        )
        val barcodeNumber = "13450673"
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(barcodeNumber, BarcodeFormat.CODE_128, widthPx.toInt(), heightPx.toInt())
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