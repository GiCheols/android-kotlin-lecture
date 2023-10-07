package com.example.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 반드시 사용할 entity들을 정의해야 함 + 버전 번호 반드시 들어가야 함
// 버전 1이기 때문에 Migration이 딱히 필요 없음
@Database(entities = [Student::class, ClassInfo::class, Enrollment::class, Teacher::class],
    exportSchema = false, version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract fun getMyDao() : MyDAO
    // 위 메서드로 인해 MyDatabase 클래스는 abstract class

    // companion object == static 객체처럼 동작 + 싱글톤 패턴으로 만들기 위함
    companion object {
        // 인스턴스 == 유일한 마이 데이터베이스 객체를 저장하기 위한 변수
        private var INSTANCE: MyDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE student_table ADD COLUMN last_update INTEGER")
            }
        }
        // getDatabase() static 메서드 생성
        fun getDatabase(context: Context) : MyDatabase {
            // 싱글톤 패턴으로 만들기 위해 인스턴스가 null일 때만 생성
            if (INSTANCE == null) {
                // 인스턴스를 아래 Room.databaseBuilder를 사용해 만듬
                INSTANCE = Room.databaseBuilder(
                    // school_database는 실제로 저장되는 데이터베이스 파일의 이름
                    context, MyDatabase::class.java, "school_database")
                        // migration 방법으로 버전 업그레이드
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                // for in-memory database
                /*INSTANCE = Room.inMemoryDatabaseBuilder(
                    context, MyDatabase::class.java
                ).build()*/
            }
            // 널이 아닌 경우 저장된 인스턴스를 반환
            // 스레드 하나만 사용할 때 사용
            // 두 개 이상이라면 if문에 동기화가 필요 -> 경쟁 조건(INSTANCE) 발생하므로
            return INSTANCE as MyDatabase
        }
    }
}

/** 여러개의 Migration 지정 가능
 *
private val MIGRATION_1_2 = object : Migration(1, 2){  // version 1 -> 2
    버전 1 -> 2로 바뀔 때, last_update라는 정수형 컬럼이 추가되어야 함
    이 때, execSQL 명령어를 사용
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE student_table ADD COLUMN last_update INTEGER")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3){   // version 2 -> 3
버전 2 -> 3로 바뀔 때, last_update라는 정수형 컬럼이 class_table에 추가되어야 함
실제로 저장된 데이터 버전이 2일 때, 우리가 쓰는 테이블이 버전 3이 필요하다면 Migration이 실행됨
값이 더 필요하다면 INSERT를 하면 됨
drop, create는 웬만해선 쓰지 않는게 좋고, Migration시 가급적 기존의 내용들을 빼지 않는 것이 좋음 = 안전상의 이유
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE class_table ADD COLUMN last_update INTEGER")
    }
}

 */