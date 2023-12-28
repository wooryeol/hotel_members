package kr.co.parnashotel.rewards.menu.home

import android.app.*
import android.content.Context
import android.os.*
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.parnashotel.R
import kr.co.parnashotel.databinding.ActMainBinding
import kr.co.parnashotel.databinding.CellHotelBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.rewards.menu.myPage.RewardActivity
import kr.co.parnashotel.rewards.model.HotelModel
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActMainBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //앱 사용중 화면 켜짐
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mContext = this
        mActivity = this

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        val weightHeight: Float
        val weightHeightCalc: Float

        if(screenHeight in 1000..1280) {
            weightHeight = 1.0f
            weightHeightCalc = 1f
        } else if(screenHeight in 2000..2030) {
            weightHeight = 1.5f
            weightHeightCalc = 1.3f
        } else if(screenHeight in 2031..2050) {
            weightHeight = 1.4f
            weightHeightCalc = 1.26f
        } else if(screenHeight in 2051..2400) {
            weightHeight = 1.4f
            weightHeightCalc = 1.3f
        } else if(screenHeight in 2401..2900) {
            weightHeight = 1.5f
            weightHeightCalc = 1.5f
        } else {
            weightHeight = 1.3f
            weightHeightCalc = 1.26f
        }

        Utils.Log("screen Height01 ===> $weightHeight")
        Utils.Log("screen Height02 ===> $weightHeightCalc")
        Utils.Log("screen Height03 ===> $screenHeight")

        // 레이아웃의 높이를 비율에 맞게 계산
        val totalWeight = weightHeight + 1f
        val area01Height = screenHeight * (weightHeightCalc / totalWeight)

        // 레이아웃의 LayoutParams 객체를 생성하고 높이를 설정
        val area01LayoutParams = mBinding.area01.layoutParams

        area01LayoutParams.height = area01Height.toInt()

        // 레이아웃에 새로운 높이를 적용합니다.
        mBinding.area01.layoutParams = area01LayoutParams

        val list: List<HotelModel> = arrayListOf(
            HotelModel(R.drawable.grand, "", "그랜드 인터컨티넨탈 서울 파르나스", "${Define.DOMAIN}?hotelCode=21&lang=kor"),
            HotelModel(R.drawable.coex, "", "인터컨티넨탈 서울 코엑스", "${Define.DOMAIN}?hotelCode=23&lang=kor"),
            HotelModel(R.drawable.parnas_jeju, "", "파르나스 호텔 제주", "${Define.DOMAIN}?hotelCode=26&lang=kor"),
            HotelModel(R.drawable.pangyo, "", "나인트리 프리미어 호텔 서울 판교", "${Define.DOMAIN}?hotelCode=27&lang=kor"),
            HotelModel(R.drawable.myoungdong_2, "", "나인트리 프리미어 호텔 명동2", "${Define.DOMAIN}?hotelCode=29&lang=kor"),
            HotelModel(R.drawable.insadong, "", "나인트리 프리미어 호텔 인사동", "${Define.DOMAIN}?hotelCode=30&lang=kor"),
            HotelModel(R.drawable.myoungdong_1, "", "나인트리 호텔 명동", "${Define.DOMAIN}?hotelCode=28&lang=kor"),
            HotelModel(R.drawable.dongdaemoon, "", "나인트리 호텔 동대문", "${Define.DOMAIN}?hotelCode=31&lang=kor")
        )

        val popularityAdapter = RecyclerViewAdapter(mContext)
        popularityAdapter.datalist = list
        mBinding.recyclerView.adapter = popularityAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false) // 레이아웃 매니저 연결, 가로 스크롤

        // 링크 걸어주는 곳
        mBinding.loginTv.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.login}")
        }
        mBinding.regTv.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.signUp}")
        }
        mBinding.overlayView.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.rsvn}")
        }
        mBinding.rsvn.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.rsvn}")
        }
        mBinding.dining.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.dining}")
        }
        mBinding.search.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.search}")
        }
        mBinding.reservationCheck.setOnClickListener {
            Utils.moveToPage(mContext, "${kr.co.parnashotel.rewards.common.Define.DOMAIN}${kr.co.parnashotel.rewards.common.Define.reservationCheck}")
        }
        mBinding.mainLogo.setOnClickListener {
            Utils.nextPage(mContext, RewardActivity(), 0, true)
        }
    }
}

class RecyclerViewAdapter(context: Context): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    var datalist: List<HotelModel> = ArrayList()
    var mContext = context

    inner class ViewHolder(private val binding: CellHotelBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: HotelModel) {
            itemView.setOnClickListener {
                Utils.moveToPage(mContext, itemModel.url)
            }
            binding.hotelName.text = itemModel.title
            binding.thumb.setImageResource(itemModel.img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CellHotelBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datalist.size

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 마지막 마진 제거
        if(position == (itemCount - 1)) {
            val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginEnd = 0
            holder.itemView.layoutParams = layoutParams
        }

        holder.bind(datalist[position])
    }
}