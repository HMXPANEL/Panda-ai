package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val category: String = "Important",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings_kv")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface PandaDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<Memory>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemoriesSync(): List<Memory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory)

    @Delete
    suspend fun deleteMemory(memory: Memory)

    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    @Query("SELECT * FROM settings_kv WHERE `key` = :key LIMIT 1")
    suspend fun getSettingByKey(key: String): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(entity: SettingsEntity)
}

@Database(entities = [Memory::class, ChatMessage::class, SettingsEntity::class], version = 1, exportSchema = false)
abstract class PandaDatabase : RoomDatabase() {
    abstract fun pandaDao(): PandaDao

    companion object {
        @Volatile
        private var INSTANCE: PandaDatabase? = null

        fun getDatabase(context: Context): PandaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PandaDatabase::class.java,
                    "panda_agent_db"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PandaRepository(private val dao: PandaDao) {
    val allMemories: Flow<List<Memory>> = dao.getAllMemoriesFlow()
    val allMessages: Flow<List<ChatMessage>> = dao.getAllMessagesFlow()

    fun getAllMemoriesSync(): List<Memory> = dao.getAllMemoriesSync()

    suspend fun insertMemory(memory: Memory) = dao.insertMemory(memory)
    suspend fun deleteMemory(memory: Memory) = dao.deleteMemory(memory)
    suspend fun clearAllMemories() = dao.deleteAllMemories()

    suspend fun insertMessage(message: ChatMessage): Long = dao.insertMessage(message)
    suspend fun updateMessage(message: ChatMessage) = dao.updateMessage(message)
    suspend fun clearChat() = dao.clearChatHistory()

    suspend fun getSetting(key: String, defaultValue: String): String {
        return dao.getSettingByKey(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        dao.saveSetting(SettingsEntity(key, value))
    }
}
