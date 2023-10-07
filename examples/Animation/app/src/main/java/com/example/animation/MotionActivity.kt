package com.example.animation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MotionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion)
    }
    // 모션 레이아웃의 디버그 속성을 켜놓으면 애니메이션 궤적, 진행 정보를 앱에서 볼 수 있음
}