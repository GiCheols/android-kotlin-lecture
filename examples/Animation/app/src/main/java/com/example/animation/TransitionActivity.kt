package com.example.animation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.transition.*
import com.example.animation.databinding.ActivityTransitionBinding

class TransitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransitionBinding
    private lateinit var scene1: Scene
    private lateinit var scene2: Scene

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransitionBinding.inflate(layoutInflater)
        // root == R.layout.activity_transition
        setContentView(binding.root)
        // sceneRoot = findViewById<FrameLayout>(R.id.scene_root)
        // scene 객체 생성
        scene1 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_1, this)
        scene2 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_2, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.transition_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // TransitionManager.go() 메소드를 사용해야 씬 지정을 해야 화면이 나타남
        when (item.itemId) {
            R.id.scene_1 -> TransitionManager.go(scene1, ChangeBounds())
            R.id.scene_2 -> TransitionManager.go(scene2, Fade().addListener(Scene1to2TransitionListener()))
        }
        return super.onOptionsItemSelected(item)
    }

    inner class Scene1to2TransitionListener : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) {
        }

        override fun onTransitionEnd(transition: Transition) {
            println("onTransitionEnd ########################")
        }

        override fun onTransitionCancel(transition: Transition) {
        }

        override fun onTransitionPause(transition: Transition) {
        }

        override fun onTransitionResume(transition: Transition) {
        }
    }

}