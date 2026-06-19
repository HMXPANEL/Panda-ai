package com.example.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import com.example.data.Memory
import com.example.data.PandaDatabase
import com.example.data.PandaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class PandaRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var database: PandaDatabase
    private lateinit var repository: PandaRepository

    @Before
    fun setUp() {
        val context = org.robolectric.RuntimeEnvironment.getApplication().applicationContext
        database = Room.inMemoryDatabaseBuilder(context, PandaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PandaRepository(database.pandaDao())
    }

    @After
    fun tearDown() {
        database.close()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testInsertAndGetMemory() = runBlockingTest {
        val memory = Memory(content = "Test memory", category = "Important")
        repository.insertMemory(memory)
        
        val memories = repository.allMemories.first()
        assertEquals(1, memories.size)
        assertEquals("Test memory", memories[0].content)
        assertEquals("Important", memories[0].category)
    }

    @Test
    fun testDeleteMemory() = runBlockingTest {
        val memory = Memory(content = "Test memory", category = "Important")
        repository.insertMemory(memory)
        
        var memories = repository.allMemories.first()
        assertEquals(1, memories.size)
        
        repository.deleteMemory(memories[0])
        memories = repository.allMemories.first()
        assertEquals(0, memories.size)
    }

    @Test
    fun testClearAllMemories() = runBlockingTest {
        repository.insertMemory(Memory(content = "Memory 1", category = "Important"))
        repository.insertMemory(Memory(content = "Memory 2", category = "Conversations"))
        
        var memories = repository.allMemories.first()
        assertEquals(2, memories.size)
        
        repository.clearAllMemories()
        memories = repository.allMemories.first()
        assertEquals(0, memories.size)
    }

    @Test
    fun testInsertAndGetChatMessage() = runBlockingTest {
        val message = ChatMessage(text = "Hello", isUser = true)
        val id = repository.insertMessage(message)
        
        val messages = repository.allMessages.first()
        assertEquals(1, messages.size)
        assertEquals("Hello", messages[0].text)
        assertTrue(messages[0].isUser)
    }

    @Test
    fun testUpdateMessage() = runBlockingTest {
        val message = ChatMessage(text = "Original", isUser = false)
        val id = repository.insertMessage(message)
        
        val updatedMessage = ChatMessage(id = id.toInt(), text = "Updated", isUser = false)
        repository.updateMessage(updatedMessage)
        
        val messages = repository.allMessages.first()
        assertEquals("Updated", messages[0].text)
    }

    @Test
    fun testClearChat() = runBlockingTest {
        repository.insertMessage(ChatMessage(text = "Message 1", isUser = true))
        repository.insertMessage(ChatMessage(text = "Message 2", isUser = false))
        
        var messages = repository.allMessages.first()
        assertEquals(2, messages.size)
        
        repository.clearChat()
        messages = repository.allMessages.first()
        assertEquals(0, messages.size)
    }

    @Test
    fun testSaveAndGetSetting() = runBlockingTest {
        repository.saveSetting("test_key", "test_value")
        val value = repository.getSetting("test_key", "default")
        
        assertEquals("test_value", value)
    }

    @Test
    fun testGetSettingDefault() = runBlockingTest {
        val value = repository.getSetting("nonexistent_key", "default_value")
        assertEquals("default_value", value)
    }
}