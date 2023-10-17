package com.example.internet_ex

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.core.content.FileProvider.getUriForFile
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import javax.net.SocketFactory
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.io.File
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    private val serverAddress = "cse.hansung.ac.kr"
    private val scheme = "http"
    private lateinit var output : TextView
    private lateinit var outputBy : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        output = findViewById(R.id.textView)
        outputBy = findViewById(R.id.textViewBy)

        if (isNetworkAvailable())
            Snackbar.make(output, "Network available", Snackbar.LENGTH_SHORT).show()
        else
            Snackbar.make(output, "Network unavailable", Snackbar.LENGTH_SHORT).show()

        findViewById<Button>(R.id.java_socket).setOnClickListener { javaSocket() }
        findViewById<Button>(R.id.java_http).setOnClickListener { httpLib() }
        findViewById<Button>(R.id.retrofit).setOnClickListener { retrofitWithCoroutine() }
        findViewById<Button>(R.id.download).setOnClickListener { downloadManager() }
        findViewById<Button>(R.id.openDownload).setOnClickListener { openDownload() }
        findViewById<Button>(R.id.volley).setOnClickListener { volley() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.restApi -> startActivity(Intent(this, RestActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    // 네트워크 연결 상태를 알아보는 메소드
    private fun isNetworkAvailable(): Boolean {
        // 네트워크 연결 상태를 알아보기 위해서는 ConnectivityManager가 필요
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 현재 활성화된 네트워크를 리턴
        val nw = connectivityManager.activeNetwork ?: return false
        // val nw = connectivityManager.allNetworks => 모든 네트워크를 리턴

        // 현재 네트워크의 Capability를 리턴
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

        // 와이파이, 이더넷 연결 상태를 알아봄
        println("${actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}, ${actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}, " +
                "${actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)}")
        
        // 인터넷 가능 여부 판단
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

    }

    // 자바 소켓 API 활용
    private fun javaSocket() {
        output.text = ""
        outputBy.text = ""

        // THIS IS A BAD EXAMPLE. Just for showing that Java Socket API can be used in Android.
        CoroutineScope(Dispatchers.IO).launch {
            // 소켓 팩토리를 활용
            val sock = SocketFactory.getDefault().createSocket(serverAddress, 80) //HTTP 포트 80
            //val sock = Socket("naver.com", 80)
            val istream = sock.getInputStream()
            val ostream = sock.getOutputStream()
            ostream.write("GET /\r\n".toByteArray()) // GET 메소드
            ostream.flush()
            val r = istream.readBytes()
            
            // 현재 Dispatchers.IO에서 수행중이므로 UI 업데이트를 위해 Main으로 변환
            // UI 업데이트 코드
            withContext(Dispatchers.Main) {
                outputBy.text = "JAVA Socket"
                output.text = r.decodeToString()
            }
            sock.close()
        }
        /**
         * 노란 줄로 처리되어 있는 createSocket, getInputStream 등..
         * 네트워크나 파일 I/O같이 블록 가능성이 있는 API를 호출할 때는 반드시 비동기로 호출
         *      - 예제에서는 Dispatcher.IO에서 실행하는 코루틴으로 되어 있음
         * 서버 소켓 사용할 경우 네트워크 상황 (NAT 여부 등) 확인이 필요
         *      - 경우에 따라 불가능할 수도 있음
         *      - 모바일 네트워크의 경우 외부에서 접속이 안됨
         * 대부분의 사이트는 Https를 사용해 80번 포트로 GET 요청을 보내도 웹 페이지를 리턴하지 않는 경우가 있음
         * 서버 소켓에서 http를 사용한다면 접속 허용이 안될 수도 있음
         *  - 셀룰러는 블록 가능성이 있음 == 보안
         */
    }

    private fun httpLib() {
        output.text = ""
        outputBy.text = ""

        CoroutineScope(Dispatchers.IO).launch {
            val conn = when(scheme) {
                // URL 객체 만든 후 openConnection -> HttpsURLConnection으로 타입 캐스팅
                "https" -> URL("$scheme://$serverAddress").openConnection() as HttpsURLConnection
                else -> URL("http://$serverAddress").openConnection() as HttpURLConnection
                // for http, cleartext without encryption
                // add android:usesCleartextTraffic="true" in the <application> tag of the Manifest.
            }
            val istream = conn.inputStream
            val r = istream.readBytes()
            // UI 업데이트는 메인 스레드에서
            withContext(Dispatchers.Main) {
                outputBy.text = "HTTP URL Connection"
                output.text = r.decodeToString()
            }
            conn.disconnect()
        }
        // https://developer.android.com/reference/java/net/HttpURLConnection\

        /** HTTP URL Connection API
         * 자바에서 제공하는 HttpURLConnection, HttpsURLConnection
         *  - HTTP(s) 프로토콜을 손쉽게 사용해주는 API
         * 
         * 비동기 호출이 필요하므로 CoroutineScope(Dispatchers.IO) 범위에서
         * launch(코루틴 생성) 함
         * ** 주의: HTTP 사용시 Manifest 파일의 application 테그에 android:usesCleartextTraffic="true"
         */
    }

    interface RestApi {
        @GET("/")
        fun getRoot(): Call<String>
        // Call == CallBack -> 뒤에서 정의가 필요
        // CallBack 등록이 필요

        @GET("/")
        suspend fun getRoot2(): String
        // CallBack을 리턴하지 않고 싶다면 suspend 붙이고 비동기 처리
    }

    // 문자열, 코루틴 사용
    // API 인터페이스 메소드 중 suspend가 붙은 것
    private fun retrofitWithCoroutine() {
        output.text = ""
        outputBy.text = ""

        val retrofit = Retrofit.Builder()
            .baseUrl("$scheme://$serverAddress") // base url + @GET(인자)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val api = retrofit.create(RestApi::class.java)
        var result : String

        // suspend가 붙었으므로 Coroutine 내에서 호출 필요
        CoroutineScope(Dispatchers.IO).launch {
            try {
                result = api.getRoot2()
            } catch (e: Exception) {
                result = "Failed to connect $serverAddress"
            }
            withContext(Dispatchers.Main) {
                // UI 업데이트는 메인쓰레드에서
                // 결과값을 Coroutine 밖에서 사용해서는 안됨
                outputBy.text = "Retrofit with Coroutine"
                output.text = result
            }
        }

    }

    // Retrofit - 문자열, CALL 리턴 받기
    private fun retrofit() {
        output.text = ""
        outputBy.text = ""

        // Retrofit.Builder()로 retrofit 객체 생성
        // .addConverterFactory()로 변환기 지정
        val retrofit = Retrofit.Builder()
            .baseUrl("$scheme://$serverAddress")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        // retrofit.create(API 인터페이스) - API 객체 생성
        val api = retrofit.create(RestApi::class.java)

        // enqueue를 해야 실제 동작
        // error handling이 쉬움
        // Callback<String>인 이유는 처리하려는 요청이 String이기 때문
        api.getRoot().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                outputBy.text = "Retrofit"
                output.text = response.body()
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                outputBy.text = "Retrofit"
                output.text = "Failed to connect $serverAddress"
            }
        })
    }

    private fun volley() {
        output.text = ""
        outputBy.text = ""

        val queue = Volley.newRequestQueue(this)
        val url = "$scheme://$serverAddress"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                outputBy.text = "Volley"
                output.text = response },
            {   outputBy.text = "Volley"
                output.text = "Failed to connect $serverAddress" })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    // 다운로드 완료 방송을 받는 방송 수신자
    // 다운로드 시작시 리턴된 아이디를 downloadId 와 비교해 다운받은 파일이 맞는지 확인
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            when (intent?.action) {
                // 다운로드 완료시 경로만 출력
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "testdownload.png")
                    println("Download Completed! $filePath ${filePath.length()}")
                }
                else -> {
                    // 다운로드 중에 알림 클릭
                    // DownloadManager.ACTION_NOTIFICATION_CLICKED
                    // 완료되었을 때 액션 뷰 실행
                    // DownloadManager.ACTION_VIEW_DOWNLOADS
                    println(intent?.action)
                }
            }
        }

    }

    // Download Manager
    // 시스템에서 제공하는 다운로드 기능
    // URL과 다운로드 위치만 알려주면 알아서 알림도 표시하고 다운로드 수행
    // 수백 MB의 데이터를 다운로드 받기 위한 Manager -> 음악 혹은 동영상 ...
    private fun downloadManager() {
        val downloadURL = Uri.parse("https://www.hansung.ac.kr/sites/hansung/images/common/logo.png")

        // 다운로드가 완료되엇음을 알기 위한 Receiver 등록
        val iFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(receiver, iFilter)  // need to register/unregister appropriately by the lifecycle of Activity.

        // 다운로드 매니저 가져오기
        val dManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // Request -> 학교 로고 가져오기 -> Request 객체 가져오기
        val request = DownloadManager.Request(downloadURL).apply {
            // 다운로드 경로, 설정
            setTitle("Download")
            setDescription("Downloading a File")
            // setRequiresDeviceIdle(true)
            // 다운로드가 완료되어도 알림을 유지하고 싶다면 아래 코드 추가
            // 알림 터치시 다운 받은 파일을 보여줌
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // 아래 위치로 다운로드 공용 디렉토리를 사용할 수 없으므로 앱만 접근 가능한 위치 지정
            // 다운된 파일은 해당 앱만 접근 가능하므로 불편하긴 함
            setDestinationInExternalFilesDir(baseContext, Environment.DIRECTORY_DOWNLOADS, "testdownload.png")
            // 셀룰러에서 다운 못하도록 설정
            // setAllowedOverMetered(false)
        }
        // 최종적으로 다운로드 매니저에 enqueue
        val dID = dManager.enqueue(request)
        // you can use the dID for removing/deleting the download. dManager.remove(dID)
    }

    // FileProvider
    /**
     * 다운로드 받은 파일을 요청한 앱 내에서 사용하는 것은 문제 없음
     * 안드로이드 최신 버전부터는 public directory 경로에 저장하는 것은 불가능
     *  - MediaStore 를 이용해 미디어 파일 저장은 가능
     *      - API 사용
     *  - 안드로이드 보안 정책 강화로 인해 ...
     * FileProvider를 사용해 외부에 파일 제공 가능
     *  - 컨텐트 제공자(Content Provider)의 한 종류
     */
    private fun openDownload() {
        // Implict Intent로 fileprovider가 제공하는 파일 열기
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "testdownload.png")
        val contentUri: Uri =
            getUriForFile(this, "com.example.internet_ex.file_provider", filePath)
        // we need <provider> in AndroidManifest.xml for the getUriForFile
        // AndroidManifest.xml 파일에 <provider> 태그를 추가해야 함
        // <application> 태그 내에 정의
        println(contentUri)
        val i = Intent().apply {
            action = Intent.ACTION_VIEW
            data = contentUri
            // READ_URI_PERMISSION 필요
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(i)
    }
}