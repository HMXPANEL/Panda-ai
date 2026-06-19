package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.mock
import org.junit.Assert.*

class GeminiNetworkTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testQueryGeminiStream_emptyApiKey_returnsError() = runBlockingTest {
        val results = mutableListOf<String>()
        
        GeminiNetwork.queryGeminiStream("Test prompt", "", "gemini-2.0-flash")
            .collect { results.add(it) }
        
        assertTrue(results.any { it.contains("Error") || it.contains("API key") })
    }

    @Test
    fun testQueryGeminiStream_placeholderKey_returnsError() = runBlockingTest {
        val results = mutableListOf<String>()
        
        GeminiNetwork.queryGeminiStream("Test prompt", "MY_GEMINI_API_KEY", "gemini-2.0-flash")
            .collect { results.add(it) }
        
        assertTrue(results.any { it.contains("Error") || it.contains("API key") })
    }
}