package com.example.repository_pattern

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyRepository(context: Context) {
    // Retrofit & Room을 데이터 베이스로 사용
    // viewModel 은 Repository 를 사용 -> ViewModel 은 Repository 에게 의존
    private val baseURL = "https://api.github.com/"
    private val api = retrofitInit(baseURL) // 네트워크 접근
    private val myDao = MyDatabase.getDatabase(context).myDao // DB 접근

    val repos = myDao.getAll()
    // LiveData<List<ReposD>>, viewModel에게 제공하는 데이터

    // 아래 메서드가 실행될 때마다 네트워크에 접근
    suspend fun refreshData() {
        withContext(Dispatchers.IO) {
            val repos = api.listRepos("jyheo")
            // convert Repo to RepoD
            // 네트워크에서 가져온 데이터를 Room 데이터에 맞게 변환
                // 일반적으로 네트워크에서 가져온 데이터가 DB 저장소의 데이터 타입이 일치하지 않음
                // 따라서 변환할 필요가 있음
                // Repo -> RepoD 타입으로의 변환이 필요 !
            // map으로 데이터 변환 !
            val repoDs = repos.map {
                RepoD(it.name, it.owner.login)
            }
            myDao.insertAll(repoDs)
        }
    }
}