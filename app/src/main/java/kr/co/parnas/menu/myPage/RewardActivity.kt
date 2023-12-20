package kr.co.parnas.menu.myPage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kr.co.parnas.R
import kr.co.parnas.common.Utils
import kr.co.parnas.databinding.ActivityRewardBinding

@SuppressLint("ResourceType")
class RewardActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    private var _mBinding: ActivityRewardBinding? = null
    private val mBinding get() = _mBinding!!

    private val bottomSheetLayout by lazy { findViewById<LinearLayout>(R.id.bottom_sheet_layout) }
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mBinding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(_mBinding!!.root)

        mContext = this

        // 상태바 색상
        window.statusBarColor = Color.parseColor(getString(R.color.grade_c))


        initializePersistentBottomSheet()
        persistentBottomSheetEvent()

        // 현재 grade
        currentGrade(40)

    }

    override fun onDestroy() {
        super.onDestroy()
        _mBinding = null
    }

    // Persistent BottomSheet 초기화
    private fun initializePersistentBottomSheet() {

        // BottomSheetBehavior에 layout 설정
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                // BottomSheetBehavior state에 따른 이벤트
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Log.d("wooryeol", "state: hidden")
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("wooryeol", "state: expanded")
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("wooryeol", "state: collapsed")
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Log.d("wooryeol", "state: dragging")
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("wooryeol", "state: settling")
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("wooryeol", "state: half expanded")
                    }
                }

            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    // PersistentBottomSheet 내부 버튼 click event
    private fun persistentBottomSheetEvent() {

        /*mBinding.rewardPointBtn.setOnClickListener {
            // BottomSheet의 최대 높이만큼 보여주기
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetHidePersistentButton.setOnClickListener {
            // BottomSheet 숨김
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }*/

    }

    // grade의 위치를 바꿔주는 곳
    private fun currentGrade(gradeRate: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val grade = findViewById<ImageView>(R.id.grade)

        progressBar.progress = gradeRate

        val layoutParams = grade.layoutParams as LinearLayout.LayoutParams

        progressBar.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                progressBar.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val totalWidth = progressBar.width
                val desiredPosition = (gradeRate / 100f) * totalWidth

                layoutParams.leftMargin = desiredPosition.toInt() - grade.width / 2
                grade.layoutParams = layoutParams
            }
        })
    }
}