package com.example.repository_pattern

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// 코루틴을 지원하는 CoroutineWorker 사용
class MyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    // CoroutineWorker는 doWork() 자체가 suspend
    override suspend fun doWork(): Result {
        val repository = MyRepository(applicationContext) 
        // 직접 Repository 객체를 만들어야 함
        // applicationContext를 사용할 수 있음

        try {
            repository.refreshData()    // 코루틴 사용 메서드
        } catch (e: Exception) {
            return Result.retry()   // 네트워크 오류 등으로 다시 시도 필요
        }
        return Result.success() // 성공일 경우 리턴
    }

    companion object {  // worker 식별자로 사용할 이름, 유니크함(유일무이)
        // 보통 도메인 이름을 많이 씀
        const val name = "com.example.repository_pattern.MyWorker"
    }
}
