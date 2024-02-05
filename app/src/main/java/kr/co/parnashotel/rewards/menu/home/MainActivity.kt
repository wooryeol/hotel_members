package kr.co.parnashotel.rewards.menu.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.parnashotel.R
import kr.co.parnashotel.databinding.ActMainBinding
import kr.co.parnashotel.databinding.CellHotelBinding
import kr.co.parnashotel.rewards.common.Define
import kr.co.parnashotel.rewards.common.GlobalApplication
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.common.UtilPermission
import kr.co.parnashotel.rewards.common.Utils
import kr.co.parnashotel.rewards.menu.myPage.RewardActivity
import kr.co.parnashotel.rewards.menu.webview.WebViewActivity
import kr.co.parnashotel.rewards.model.HotelModel
import kr.co.parnashotel.rewards.model.MembershipUserInfoModel_V2
import kr.co.parnashotel.rewards.model.UserInfoModel_V2
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        var mainActivity : MainActivity? = null
        var isLoginButtonClicked = false
    }

    private lateinit var mBinding: ActMainBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //앱 사용중 화면 켜짐
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mContext = this
        mainActivity = this



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
            isLoginButtonClicked = true
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.login}")
        }

        if (GlobalApplication.isLoggedIn){
            mBinding.loginTv.visibility = View.GONE
            mBinding.regTv.text = getString(R.string.btn_logout)
            mBinding.regTv.setOnClickListener {
                logout()
            }
        } else {
            mBinding.regTv.setOnClickListener {
                Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.signUp}")
            }
        }
        mBinding.overlayView.setOnClickListener {
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.rsvn}")
        }
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
        mBinding.mainPopup.setOnClickListener {
            Utils.moveToPage(mContext, "${Define.DOMAIN}${Define.membershipIntroduction}")
        }
    }

    private fun isLoggedIn(domain: String){
        mBinding.regTv.setOnClickListener {
            val intent = Intent(mContext, WebViewActivity::class.java)
            intent.putExtra("index", domain)
            startActivity(intent)
        }
    }

    private fun logout() {
        runOnUiThread {
            GlobalApplication.isLoggedIn = false
            UserInfoModel_V2().clearUserInfo(mContext)
            MembershipUserInfoModel_V2().clearMembershipUserInfo(mContext)
            mBinding.loginTv.visibility = View.VISIBLE
            mBinding.regTv.text = getString(R.string.main_reg_tv)

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