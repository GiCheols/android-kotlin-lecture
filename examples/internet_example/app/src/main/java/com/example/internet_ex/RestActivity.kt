package com.example.internet_ex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.lang.StringBuilder

data class Owner(val login: String)
// 이름이 매우 중요. 이름을 기준으로 값을 가져오기 때문
data class Repo(val name: String, val owner: Owner, val url: String)
data class Contributor(val login: String, val contributions: Int)

// Retrofit - JSON
// Repo 클래스와 RestApi 인터페이스
// moshiConverter가 알아서 List로 변환함
interface RestApi {
    @GET("users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String): List<Repo>
    // 아래 리스트 중 필요한 프로퍼티만 추출 필요
    // 앞선 예제들은 이러한 문자열들의 파싱이 필요함 -- 번거로움
    /*
    [
        {
            "id": 74595421,
            "node_id": "MDEwOlJlcG9zaXRvcnk3NDU5NTQyMQ==",
            "name": "2ndProject",
            "full_name": "jyheo/2ndProject",
            "private": false,
            "owner": {
                "login": "jyheo",
                "id": 4907532,
                 ... 생략 ...
            },
            "html_url": "https://github.com/jyheo/2ndProject",
            "description": null,
            "fork": true,
            "url": "https://api.github.com/repos/jyheo/2ndProject",
            ... 생략 ...
        },
        {
            ... 생략 ...
        },
        ... 생략 ...
    ]
    */

    @GET("/repos/{owner}/{repo}/contributors")
    suspend fun contributors(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<Contributor>
}

class MyViewModel : ViewModel() {
    private val baseURL = "https://api.github.com/"
    private lateinit var api: RestApi

    val response = MutableLiveData<String>()

    init {
        retrofitInit()
        refreshData()
    }

    fun refreshData() {
        // 데이터 가져오기, viewModel 내에서는 viewModelScope를 사용함
        viewModelScope.launch {
            try {
                //val c = api.contributors("square", "retrofit")
                val repos = api.listRepos("jyheo")
                // 직접 UI를 업데이트하지 않음
                // response는 MutableLiveData
                // MutableLiveData의 Observer에게 데이터 업데이트시 알림
                response.value = StringBuilder().apply {
                    repos.forEach {
                        append(it.name)
                        append(" - ")
                        append(it.owner.login)
                        append("\n")
                    }
                }.toString()
            } catch (e: Exception) {
                response.value = "Failed to connect to the server"
            }
        }
    }

    // Retrofit - JSON
    private fun retrofitInit() {
        // JSON 변환기 지정
        val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            // Converter를 JSON Converter 를 지정
            .addConverterFactory(MoshiConverterFactory.create())

            .build()

        // RestApi 인터페이스
        api = retrofit.create(RestApi::class.java)
    }
}


class RestActivity : AppCompatActivity() {
    private lateinit var myViewModel : MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rest)

        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.response.observe(this) {
            findViewById<TextView>(R.id.textResponse).text = it
        }
    }
}