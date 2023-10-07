package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestSinglePermission(Manifest.permission.POST_NOTIFICATIONS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0
            createNotificationChannel()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_notification -> showNotification()
            R.id.show_noti_bigtext -> showNotificationBigText()
            R.id.show_noti_bigpicture -> showNotificationBigPicture()
            R.id.show_noti_progress -> showNotificationProgress()
            R.id.show_noti_button -> showNotificationButton()
            R.id.show_noti_reg_activity -> showNotificationRegularActivity()
            R.id.show_noti_special_activity -> showNotificationSpecialActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestSinglePermission(permission: String) {
        /* permission: 권한 인자
        알림 표시 권한
        POST_NOTIFICATION을 줌.
        권한 요청의 흐름: 권한 검사 - Contract를 만들어 권한을 요청 - 권한 요청 다이얼로그를 띄움
        단, 한 번 거부를 하는 경우 권한을 요청하는 이유를 띄움
         */
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
            // 권한이 있는지 체크
            return

        val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it == false) { // permission is not granted!
                // 만약, 권한 요청이 거부되는 경우, 권한이 없어서는 안되는 이유를 경고로 띄움
                AlertDialog.Builder(this).apply {
                    setTitle("Warning")
                    setMessage(getString(R.string.no_permission, permission))
                }.show()
            }
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            // you should explain the reason why this app needs the permission.
            // 권한에 대한 설명을 출력하는 부분
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage(getString(R.string.req_permission_reason, permission))
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ -> }
            }.show()
        } else {
            // should be called in onCreate() -> 반드시 여기서 해야 함
            // 권한 요청 보냄
            requestPermLauncher.launch(permission)
        }
    }

    private val channelID = "default"
    // 채널을 구분할 수 있는 식별자

    // Build 버전이 8.0 이상인 경우에만 알림 채널을 만들 수 있으므로 버전 확인이 반드시 필요
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        // compat 없음
        val channel = NotificationChannel(
                channelID, "default channel",   // 채널 이름
                NotificationManager.IMPORTANCE_DEFAULT  // 중요도
        )
        channel.description = "description text of this channel." //채널에 대한 자세한 설명
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    // 단순 알림 생성, notify()
    private var myNotificationID = 1
        get() = field++

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification Title")
                .setContentText("Notification body")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        NotificationManagerCompat.from(this)
                .notify(myNotificationID, builder.build())
        // 메서드를 이어서 연결하여 빌더 생성
        // this는 액티비티, 액티비티로부터 NotificationManager를 가져와서 notify().build()
        // build() 메서드를 통해 notification 객체 생성, 리턴
        // myNotificationID 값을 다르게 주고 notify() 호출 시 알림이 또 표시됨
        // 아이디가 같고 notify()를 하게 되면 원래 알림은 지워짐 == replace 의 효과
    }

    private fun showNotificationBigText() {
        val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification Title")
                .setContentText("Notification body")
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(resources.getString(R.string.long_notification_body)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        NotificationManagerCompat.from(this)
                .notify(myNotificationID, builder.build())
    }

    private fun showNotificationBigPicture() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.android_hsu)
        val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)   // 알림창 오른쪽 끝에 아이콘을 넣을 수 있음
                .setContentTitle("Notification Title")
                .setContentText("Notification body")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigPictureStyle() // 비트맵을 지정하여 설정
                        .bigPicture(bitmap)
                        .bigLargeIcon(null))  // 알림창에는 큰 그림이 나오지 않게 설정
        NotificationManagerCompat.from(this)
                .notify(myNotificationID, builder.build())
    }

    private fun showNotificationButton() {
        // 알림에 버튼 추가
        // 알림에 버튼 추가하고 버튼을 누르면 Intent로 Activity나 Broadcast 시작
        // Action 버튼을 누르면 TestActivity가 시작되는 예제
        val intent = Intent(this, TestActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        // 알림은 앱을 종료해도 기기에 남아 있어야 하기 때문에 pendingIntent로 감싸줌
        // 인텐트만으로는 시작할 수 있는 권한 없음
        // 그 권한을 pendingIntent로 위임
        // PendingIntent.FLAG_IMMUTABLE는 pendingIntent를 함부로 실행할 수 없도록 플래그 설정
        val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Notification Title")
                .setContentText("Notification body")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.android_hsu, "Action", pendingIntent)
        // addAction은 시작되는 액션 이름과 무슨 동작을 할건지 지정
        NotificationManagerCompat.from(this)
                .notify(myNotificationID, builder.build())
        // 감싸는 이유: 인텐트는 앱 종료시에는 실행할 수 없기 때문. 앱 종료시 권한 상실
        // 따라서 알림에게 그 앱의 액티비티를 실행할 수 있는 권한을 위임하는 것 !
    }

    // 알림에 프로그래스 표시 -> 파일 다운로드 진행 상황을 보여주는 상황...
    private fun showNotificationProgress() {
        val progressNotificationID = myNotificationID
        val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Progress")
                .setContentText("In progress")
                .setProgress(100, 0, false)
                // 현재 알림에서 보여줄 프로그래스의 최대값과 현재 값 지정
                // false: 정해져 있는 값을 줄 때 사용
                .setPriority(NotificationCompat.PRIORITY_LOW)
        // need to change channel importance LOW for Android 8.0 or higher.
        // PRIORITY_HIGH로 하면 프로그래스가 바뀔 때마다 소리남
        NotificationManagerCompat.from(this)
                .notify(progressNotificationID, builder.build())

        CoroutineScope(Dispatchers.Default).apply {
            // 별도의 스레드로 실행하도록 지정
            launch {
                for (i in (1..100).step(10)) {
                    // 1초에 한 번씩 값 변경
                    Thread.sleep(1000)
                    builder.setProgress(100, i, false)
                    NotificationManagerCompat.from(applicationContext)
                        .notify(progressNotificationID, builder.build())
                }
                // 전부 완료시 없앰
                builder.setContentText("Completed")
                    .setProgress(0, 0, false)
                NotificationManagerCompat.from(applicationContext)
                    .notify(progressNotificationID, builder.build())
            }
            // NotificationID를 같은 것을 주기 때문에 알림이 새로 뜨는 것이 아닌 덮어쓰게 됨
        }
    }

    // 알림에 액티비티 연결하기
    private fun showNotificationRegularActivity() {
        // 알림 터치시 연결된 액티비티 실행
        val intent = Intent(this, SecondActivity::class.java)
        val pendingIntent = with (TaskStackBuilder.create(this)) {
            // pendingIntent 사용
            // 연결된 액티비티가 일반 액티비티, 알림 전용 액티비티인지에 따라 백스택 관리가 달라짐
            // 일반 액티비티: 일반적인 앱의 액티비티
                // 백스택: A 앱에서 B 앱을 실행 후 백버튼을 누르면 A 앱으로 돌아가도록
            // 알림전용: 알림하고만 연결되어 실행 가능한 액티비티로 알림을 확장하는 개념
                // 알림으로 B를 실행하고 백버튼 클릭시 A로 돌아가지 않음
                // 사용자가 다른 방법으로 시작하지는 못하게 함
            // 알림 터치 시 일반 액티비티인 SecondActivity가 시작, 이 때 메인 위에 세컨드 오도록 백스택 생성
            // 매니패스트 파일 SecondActivity의 parentActivity로 메인 지정
            addNextIntentWithParentStack(intent)
            // 메인 액티비티를 인텐트의 뒤로
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
        val builder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Notification Title")
            .setContentText("Notification body")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
                // 알림 터치시, 알림은 사라지고 SecondActivity 실행
                // 이 상태로 백 or Up 버튼 누르면 MainActivity가 나옴
                // 백스택에 없는 다른 액티비티를 Second의 parent로 지정 시 달라짐
            .setAutoCancel(true)
        // auto remove this notification when user touches it
        NotificationManagerCompat.from(this)
            .notify(myNotificationID, builder.build())
    }

    /** 태스크와 백스택(Back Stack)
     * - 태스크(Task): 어떤 작업을 하기 위한 액티비티의 그룹
     *  - 태스크마다 자신의 백스택을 가지고 있음
     *  - foreground, background task
     *      - 최근 앱 보기에서 선택하거나 앱 아이콘을 눌러 foreground task로 전환 가능
     *  - 앱마다 하나 이상의 태스크로 실행 / 새 태스크로 시작할 것인지, 기존 태스크로 실행할지 결정
     * - 액티비티를 시작할 때 플래그에 따라 다르게 동작이 가능함
     *  - A라는 액티비티를 시작 액티비티로 두고,
     *  - 플래그가 없다면 액티비티 A의 새 인스턴스를 항상 시작
     *  - FLAG_ACTIVITY_NEW_TASK: 새 태스크로 A 시작
     *      - 하지만 이미 실행중인 A가 있다면
     *      - 새로 만들지 않고 A의 인스턴스가 포함된 태스크를 foreground로 가져옴
     *      - 이후 onNewIntent() 호출
     *          - FLAG_ACTIVITY_CLEAR_TASK: A의 인스턴스와 관련된 모든 기존 태스크 제거
     *          이후 새로 A의 인스턴스 시작, FLAG_ACTIVITY_NEW_TASK와 같이 사용
     *  - FLAG_ACTIVITY_SINGLE_TOP: A의 인스턴스가 태스크 백스택 탑에 존재하는 경우
     *      - 새로 만들지 않고 A의 onNewIntent() 호출
     *  - FLAG_ACTIVITY_CLEAR_TOP: A의 인스턴스가 이미 시작 중인 경우 백스택에서 A의
     *  인스턴스 위에 있는 다른 액티비티 인스턴스들을 모두 제거하고 A의 onNewIntent() 호출
     *      - FLAG_ACTIVITY_NEW_TASK와 같이 자주 사용
     */

    // 알림 전용 액티비티
    // 알림 터치시 알림전용 액태비티인 TempActivity 시작
    // 매니패스트에 정의 시, excludeFromRecent="true" -> 최근 보기에서 삭제
    // taskAffinity="" -> 이게 똑같은 애들끼리 묶음, 널이므로 포함 안됨, 단독 실행
    // 따라서 NEW_TASK or CLEAR_TASK
    // 최근 앱 보기를 눌러보면 TempActivity가 보이지 않음
    // 백버튼 클릭시, 독립적 액티비티이기 때문에 홈 화면으로 돌아가게 됨
    private fun showNotificationSpecialActivity() {
        val intent = Intent(this, TempActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Notification Title")
            .setContentText("Notification body")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) 
        // auto remove this notification when user touches it
        // 사용자가 알림 터치시 자동 삭제되는 알림
        NotificationManagerCompat.from(this)
            .notify(myNotificationID, builder.build())
    }
}
