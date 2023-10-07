package com.example.room

import androidx.lifecycle.LiveData
import androidx.room.*

data class StudentWithEnrollments(  // 1:N 관계
    @Embedded val student: Student,
    @Relation(
        parentColumn = "student_id",
        entityColumn = "sid"
    )
    val enrollments: List<Enrollment>
)

@Dao
interface MyDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // INSERT, key 충돌이 나면 새 데이터로 교체
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classInfo: ClassInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: Enrollment)

    @Query("SELECT * FROM student_table")
    fun getAllStudents(): LiveData<List<Student>>
    // LiveData<> 사용 -> 따라서 suspend 없이 동작 가능

    @Query("SELECT * FROM student_table WHERE name = :sname")
    // 메소드 인자를 SQL문에서 :을 붙여 사용
    suspend fun getStudentByName(sname: String): List<Student>

    @Delete
    suspend fun deleteStudent(student: Student);
    // primary key is used to find the student
    // suspend를 붙여 비동기(코루틴)로 동작하도록 설계해야 함
    // 학생 엔티티 내의 기본 키를 보고서 삭제

    // 1:N 관계를 이용하는, 2개의 select 문을 사용하기 때문에 하나의 트랜잭션으로 묶기 위해 사용하는 어노테이션
    @Transaction
    @Query("SELECT * FROM student_table WHERE student_id = :id")
    suspend fun getStudentsWithEnrollment(id: Int): List<StudentWithEnrollments>

    @Query("SELECT * FROM class_table WHERE id = :id")
    suspend fun getClassInfo(id: Int): List<ClassInfo>
}