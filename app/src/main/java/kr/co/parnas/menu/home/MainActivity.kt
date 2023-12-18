package kr.co.parnas.menu.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.http.SslError
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kr.co.parnas.R
import kr.co.parnas.common.Define
import kr.co.parnas.common.SharedData
import kr.co.parnas.common.UtilPermission
import kr.co.parnas.common.Utils
import kr.co.parnas.common.custom.GridSpacingItemDecoration
import kr.co.parnas.databinding.ActMainBinding
import kr.co.parnas.databinding.CellHotelBinding
import kr.co.parnas.network.model.HotelModel
import org.json.JSONObject
import java.net.URLDecoder
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

        // 레이아웃의 높이를 비율에 맞게 계산
        val totalWeight = 1.3f + 1f
        val area01Height = screenHeight * (1.3f / totalWeight)
        val area02Height = screenHeight * (1f / totalWeight)

        // 레이아웃의 LayoutParams 객체를 생성하고 높이를 설정
        val area01LayoutParams = mBinding.area01.layoutParams
        val area02LayoutParams = mBinding.area02.layoutParams

        area01LayoutParams.height = area01Height.toInt()
        // area02LayoutParams.height = area02Height.toInt()

        // 레이아웃에 새로운 높이를 적용합니다.
        mBinding.area01.layoutParams = area01LayoutParams
        // mBinding.area02.layoutParams = area02LayoutParams

        /*mBinding.superView.post {
            val superViewHeight = mBinding.superView.height
            val totalRatio = 2.5f // 1.5 + 1
            val area01Height = (superViewHeight * (1.5f / totalRatio)).toInt()
            val area02Height = (superViewHeight * (1f / totalRatio)).toInt()

            mBinding.area01.layoutParams = (mBinding.area01.layoutParams as ViewGroup.LayoutParams).apply {
                height = area01Height
            }
            mBinding.area02.layoutParams = (mBinding.area02.layoutParams as ViewGroup.LayoutParams).apply {
                height = area02Height
            }
        }*/

        val list: List<HotelModel> = arrayListOf(
            HotelModel(R.drawable.hotel_grandinter, "", "테스트1"),
            HotelModel(R.drawable.hotel_grandinter, "", "테스트2"),
            HotelModel(R.drawable.hotel_grandinter, "", "테스트3"),
            HotelModel(R.drawable.hotel_grandinter, "", "테스트4"),
            HotelModel(R.drawable.hotel_grandinter, "", "테스트5")
        )

        val popularityAdapter = RecyclerViewAdapter(mContext)
        popularityAdapter.datalist = list
        mBinding.recyclerView.adapter = popularityAdapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false) // 레이아웃 매니저 연결, 가로 스크롤
    }
}

class RecyclerViewAdapter(context: Context): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    var datalist: List<HotelModel> = ArrayList()
    var mContext = context

    inner class ViewHolder(private val binding: CellHotelBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: HotelModel) {
            itemView.setOnClickListener {

            }
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