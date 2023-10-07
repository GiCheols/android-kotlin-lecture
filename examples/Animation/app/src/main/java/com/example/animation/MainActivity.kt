package com.example.animation

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.example.animation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startAnimation.setOnClickListener {
            startAnimation()
            startAnimatorSet()
            startXMLAnimator()
        }
    }

    // 1. Drawable Animation
    private fun startAnimation() {
        // DO NOT set the bitmap of the imageView
        // 이미지 뷰의 배경으로 해당 리소스를 지정
        // "배경"임의 주의하여 setBACKGROUNDResource로 지정
        binding.imageView.setBackgroundResource(R.drawable.moving_circle)
        // or set the background of the imageView as @drawable/moving_circle in XML
        // background는 애니메이션을 시작하고 종료하는 메서드가 없으므로 AnimationDrawable로 캐스팅
        val animation = binding.imageView.background as AnimationDrawable
        animation.stop()    // stop을 먼저 한 이유는 반복할 때 첫 프레임부터 시작하도록 하기 위해
        // 첫 화면부터 시작하기 위해 stop()부터 
        animation.start()
        // 활용도 떨어짐 -> 재생버튼에 의한 애니메이션이기 때문

        // 2. Value Animator
        // ofFloat(시작값, 끝값) -> Interpolator 지정 안했으므로 LinearInterpolator
        // apply: 범위 지정 함수
        ValueAnimator.ofFloat(0f, 200f).apply {
            duration = 2000 // 2s
            // 0~200 값들이 리스너에 의해 콜백 됨
            addUpdateListener { updatedAnimation ->
                // animatedValue = 0~200 값을 tvValueAnimator의 텍스트로 2초동안 전달
                binding.tvValueAnimator.text = "${updatedAnimation.animatedValue as Float}"
                // translationX = x값 + animatedValue
                binding.tvValueAnimator.translationX = updatedAnimation.animatedValue as Float
            }
            start() // 애니메이션 시작
        }

        // 3. Object Animator
        // 속성 이름은 String으로 주기 때문에 오타 안나도록 유의할 것
        ObjectAnimator.ofFloat(binding.tvObjectAnimator, "translationX", 0f, 200f).apply {
            duration = 2000
            interpolator = AccelerateInterpolator() // Interpolator도 지정 해봤음
            // 점점 빨라지는 interpolator
            start()
        }
    }

    // 2개 이상의 애니메이터를 동시에, 순서대로 사용하기 위한 방법
    private fun startAnimatorSet() {
        binding.tvAnimatorSet.alpha = 1.0f
        val ani1 = ObjectAnimator.ofFloat(binding.tvAnimatorSet, "translationX", 0f, 200f).apply {
            duration = 1500
        }
        val ani2 = ObjectAnimator.ofFloat(binding.tvAnimatorSet, "translationY", 0f, 200f).apply {
            duration = 1500
        }
        // alpha 값은 투명도 -> 1~0이 되면 투명해져서 안보임, 사라지는 효과
        val fadeAnim = ObjectAnimator.ofFloat(binding.tvAnimatorSet, "alpha", 1f, 0f).apply {
            duration = 500
        }
        AnimatorSet().apply {
            // play() -> 애니메이션 동작 방법 지시, 실제 시작은 start()
            // ani1을 AnimatorSet에다가 넣는 느낌
            play(ani1).with(ani2)
            play(fadeAnim).after(ani1)  // before()도 있음
            // playTogether(): 동시에(1, 2, 3, ...)
            // playSequentially(): 순서대로(1, 2, 3, ... 나열)
            // 위 코드를 playTogether()와 playSequentially()로 하려면
            // ani3 = playTogether(ani1, ani2) -> playSequentially(ani3, fadeAnim)
            // 이 때 ani3은 AnimatorSet!
            start()
        }
        // 이와 같은 코드를 XMLAnimator로 만들면 코드로 구현하는 것보다 쉽게 구현이 가능함
    }

    // XML로 애니메이션 정의, res/animator/animator_xml.xml에 있음
    private fun startXMLAnimator() {
        binding.tvXMLAnimator.alpha = 1.0f
        // 아래 코드는 XML로 정의한 애니메이션을 로드해 애니메이터에 적용
        // 이 때 AnimatorInflater.loadAnimator() 메소드 사용
        (AnimatorInflater.loadAnimator(this, R.animator.animator_xml) as AnimatorSet).apply {
            setTarget(binding.tvXMLAnimator)
            //XML에는 어떤 것을 사용할 것인지 정해져 있지 않으므로 Target을 설정해줌
            start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.transition -> startActivity(
                Intent(this, TransitionActivity::class.java)
            )
            R.id.motionLayout -> startActivity(
                Intent(this, MotionActivity::class.java)
            )
        }
        return super.onOptionsItemSelected(item)
    }
}