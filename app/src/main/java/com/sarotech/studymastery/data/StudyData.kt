package com.sarotech.studymastery.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subtitle: String = "",
    val isOfficial: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val part: String,
    val name: String,
    val details: String,
    val status: String = "NOT_STARTED",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface StudyDao {
    @Query("SELECT * FROM subjects ORDER BY isOfficial DESC, name COLLATE NOCASE")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY part, name COLLATE NOCASE")
    fun observeTopics(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT COUNT(*) FROM topics")
    fun observeTopicCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM topics WHERE status = 'COMPLETED'")
    fun observeCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTopic(topic: TopicEntity)

    @Query("UPDATE topics SET status = :status WHERE id = :topicId")
    suspend fun updateTopicStatus(topicId: String, status: String)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopic(topicId: String)

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubject(id: String): SubjectEntity?
}

@Database(entities = [SubjectEntity::class, TopicEntity::class], version = 1, exportSchema = true)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile private var INSTANCE: StudyDatabase? = null

        fun get(context: Context): StudyDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "study_mastery.db"
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedFromAsset(context, db)
                }
            }).build().also { INSTANCE = it }
        }

        private fun seedFromAsset(context: Context, db: androidx.sqlite.db.SupportSQLiteDatabase) {
            val json = context.assets.open("syllabus_seed.json").bufferedReader().use { it.readText() }
            val subjects = JSONObject(json).getJSONArray("subjects")
            db.beginTransaction()
            try {
                for (i in 0 until subjects.length()) {
                    val subject = subjects.getJSONObject(i)
                    val subjectId = subject.getString("code")
                    db.execSQL("INSERT INTO subjects (id, name, subtitle, isOfficial, createdAt) VALUES (?, ?, ?, ?, ?)", arrayOf(subjectId, subject.getString("name"), subject.optString("subtitle"), if (subject.optBoolean("isOfficial")) 1 else 0, System.currentTimeMillis()))
                    val units = subject.getJSONArray("units")
                    for (j in 0 until units.length()) {
                        val unit = units.getJSONObject(j)
                        val topicId = "$subjectId-${j + 1}"
                        db.execSQL("INSERT INTO topics (id, subjectId, part, name, details, status, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)", arrayOf(topicId, subjectId, unit.getString("part"), unit.getString("name"), unit.getString("details"), "NOT_STARTED", System.currentTimeMillis()))
                    }
                }
                db.setTransactionSuccessful()
            } finally { db.endTransaction() }
        }
    }
}
