package com.example.room

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.room.databinding.ActivityMainBinding
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var myDao: MyDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myDao = MyDatabase.getDatabase(this).getMyDao()
        // MyDatabase 객체 가져오기

        runBlocking {   // 주의: UI를 블록할 수 있는 DAO 메소드를
            // UI 스레드에서 바로 호출하면 안됨
            myDao.insertStudent(Student(1, "james"))
            // suspend 지정되어 있음
            // blocking 될 수 있는 연산자이기에 runBlocking 지정
        }
        val allStudent = myDao.getAllStudents()
        // LiveData는 Observer를 통해 비동기적으로 데이터 가져옴
        // LiveData 리턴하므로 굳이 runBlocking 안에 있을 필요 없음

        CoroutineScope(Dispatchers.IO).launch {
            with(myDao) {
                insertStudent(Student(1, "james"))
                insertStudent(Student(2, "john"))
                insertClass(ClassInfo(1, "c-lang", "Mon 9:00", "E301", 1))
                insertClass(ClassInfo(2, "android prog", "Tue 9:00", "E302", 1))
                insertEnrollment(Enrollment(1, 1))
                insertEnrollment(Enrollment(1, 2))
            }
        }


        // UI와 연결 - LiveData
        val allStudents = myDao.getAllStudents()    // LiveData<> 타입으로 리턴됨
        // Observer를 등록해 값을 읽어옴
        allStudents.observe(this) {
            val str = StringBuilder().apply {
                    for ((id, name) in it) {
                        // it은 List<Student>
                        // LiveData<>의 T 타입이 it임
                        append(id)
                        append("-")
                        append(name)
                        append("\n")
                    }
                }.toString()
            binding.textStudentList.text = str
        }

        binding.queryStudent.setOnClickListener {
            val id = binding.editStudentId.text.toString().toInt()
            CoroutineScope(Dispatchers.IO).launch {
                val results = myDao.getStudentsWithEnrollment(id)
                if (results.isNotEmpty()) {
                    val str = StringBuilder().apply {
                        append(results[0].student.id)
                        append("-")
                        append(results[0].student.name)
                        append(":")
                        for (c in results[0].enrollments) {
                            append(c.cid)
                            val cls_result = myDao.getClassInfo(c.cid)
                            if (cls_result.isNotEmpty())
                                append("(${cls_result[0].name})")
                            append(",")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        binding.textQueryStudent.text = str
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.textQueryStudent.text = ""
                    }
                }
            }
        }

        binding.addStudent.setOnClickListener {
            val id = binding.editStudentId.text.toString().toInt()
            val name = binding.editStudentName.text.toString()
            if (id > 0 && name.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    myDao.insertStudent(Student(id, name))
                }
            }
        }

    }
}