package com.example.repository_pattern

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

// The repository pattern is a strategy for abstracting data access.
// ViewModel delegates the data-fetching process to the repository.

class MainActivity : AppCompatActivity() {
    private lateinit var myViewModel : MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startWorker).setOnClickListener { startWorker() }
        findViewById<Button>(R.id.stopWorker).setOnClickListener { stopWorker() }

        // ViewModelProvider() 호출 시에 Factory를 만들어서 넣어줌
        // this는 context
        myViewModel = ViewModelProvider(this, MyViewModel.Factory(this)).get(MyViewModel::class.java)

        // repos 속성은 repository.repos == LiveData
        // 라이브 데이터를 observe == RepoD 테이블 객체의 인스턴스
        myViewModel.repos.observe(this) { repos ->
            val response = StringBuilder().apply {
                repos.forEach {
                    append(it.name)
                    append(" - ")
                    append(it.owner)
                    append("\n")
                }
            }.toString()
            // Room DB의 데이터 변동 일어나면 observe() 실행 -> UI 변경
            findViewById<TextView>(R.id.textResponse).text = response
        }

        // Worker 상태 확인하는 코드
        // App Inspection > Background Task Inspector 로도 확인이 가능함
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(MyWorker.name)
            .observe(this) { workInfo ->
                if (workInfo.isNotEmpty()) {
                    when (workInfo[0].state) {
                        WorkInfo.State.ENQUEUED -> println("Worker enqueued!")
                        WorkInfo.State.RUNNING -> println("Worker running!")
                        WorkInfo.State.SUCCEEDED -> println("Worker succeeded!")  // only for one time worker
                        WorkInfo.State.CANCELLED -> println("Worker cancelled!")
                        else -> println(workInfo[0].state)
                    }
                }
            }
    }

    private fun startWorker() {
        //val oneTimeRequest = OneTimeWorkRequest.Builder<MyWorker>()
        //        .build()

        // 제약조건 -> 와이파이 && 배터리 충전 시에만 가능
        val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.UNMETERED) // un-metered network such as WiFi
            setRequiresBatteryNotLow(true)
            //setRequiresCharging(true)
            // setRequiresDeviceIdle(true) // android 6.0(M) or higher
        }.build()

        //val repeatingRequest = PeriodicWorkRequestBuilder<MyWorker>(1, TimeUnit.DAYS)-> 하루 주기로 !
        // 15분 주기, 빌더를 사용해 생성, 더 짧게는 불가능
        val repeatingRequest = PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)    // 제약조건 부여
            .build()    // build, workrequest 객체 생성/리턴

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(    // 주기 작업 시작
            MyWorker.name,  // MyWorker에서 정의한 식별자용 이름
            ExistingPeriodicWorkPolicy.KEEP,    // 기존에 동일 이름의 Worker가 있을 때, 기존 정책을 어떻게 할건지
            // 여기에서는 KEEP == 그대로 유지
            repeatingRequest)
        // enqueue 시, 바로 작업 시작, 15분 뒤에 다시 시작
    }

    private fun stopWorker() {
        // to stop the MyWorker
        // 식별자 이름을 인자로 줌 -> Queue에 들어간 작업을 실행 취소
        WorkManager.getInstance(this).cancelUniqueWork(MyWorker.name)
    }
}