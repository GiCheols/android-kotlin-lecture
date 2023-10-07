package com.example.room

import androidx.room.*

// 엔티티는 테이블 스키마 정의
// CREATE TABLE student_table (student_id INTEGER PRIMARY KEY, name TEXT NOT NULL);
@Entity(tableName = "student_table")    // 테이블 이름을 student_table로 지정함
data class Student (
    // 이름이 Student인 이유는, Student_table은 클래스 이름으로 적합하지 않음
    @PrimaryKey @ColumnInfo(name = "student_id") val id: Int,
    // id를 nullable 로 바꾸기 위해서는 val id:Int? 로 지정
    val name: String
)
// data class 사용하면 편리: copy, hash, equals,...의 메서드를 사용하기 편리
// @PrimaryKey로 기본 키 지정
// @ColumnInfo로 실제 테이블 스키마에서 사용할 이름 지정

@Entity(tableName = "class_table")
// Entity(table_name = "")으로 실제 테이블 이름 지정 가능
data class ClassInfo (
    @PrimaryKey val id: Int,
    // @PrimaryKey(autoGenerate = true)를 하면 키를 자동 생성
    // 이 경우 프로퍼티 정의 부분, data class XXX() { 이 부분에 val id: Int }
    val name: String,
    val day_time: String,
    val room: String,
    val teacher_id: Int
) // 소괄호임에 주의 !
// 생성자 인자로 val 주면 생성자의 property가 되므로 테이블의 컬럼이 되는 것 !
// val을 안주면 테이블의 컬럼이 되지 않으며, 생성자 인자로서만 쓰이게 됨

@Entity(tableName = "enrollment",
    primaryKeys = ["sid", "cid"],   // 복합키의 경우 이와 같이 저장
    foreignKeys = [ // 외래키 배열 !!
        ForeignKey(entity = Student::class, parentColumns = ["student_id"], childColumns = ["sid"]),
        ForeignKey(entity = ClassInfo::class, parentColumns = ["id"], childColumns = ["cid"])
    ],
    // 해석하면, Student 클래스의 student_id를 참조하는 이 클래스의 외래키는 sid로 지정!
    indices = [Index(value=["sid", "cid"])]
)
// @Entity의 PrimaryKeys와 foreignKeys 인자를 통해 프라이머리 키와 외래키 지정 가능
// 외래키는 관련된 외부 부모 Entity와 컬럼(parentColumns)을 지정
// 외래키 컬럼(childColumns)을 지정
data class Enrollment (
    val sid: Int,   // Student 의 기본 키
    val cid: Int,   // Class 의 기본 키
    // 위 두 컬럼을 합쳐 Enrollment의 기본 키로 지정
    val grade: String? = null // grade는 nullable
)

@Entity(tableName = "teacher_table")
data class Teacher (
    @PrimaryKey val id: Int,
    val name: String,
    val position: String
)

