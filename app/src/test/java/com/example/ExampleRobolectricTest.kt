package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val appName = context.getString(R.string.app_name)
        assertEquals("YOUNOVA", appName)
    }

    @Test
    fun testRoomConversationAndMessagePersistence() = runBlocking {
        val conversationDao = db.conversationDao()
        val messageDao = db.messageDao()

        val convId = "conv-123"
        val conv = ConversationEntity(
            id = convId,
            title = "Quantum Computing Exploration",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insertConversation(conv)

        val retrievedConv = conversationDao.getConversationById(convId)
        assertNotNull(retrievedConv)
        assertEquals("Quantum Computing Exploration", retrievedConv?.title)

        val userMsg = MessageEntity(
            id = "msg-1",
            conversationId = convId,
            role = "user",
            content = "Explain qubits and superposition.",
            timestamp = 1000L
        )
        val assistantMsg = MessageEntity(
            id = "msg-2",
            conversationId = convId,
            role = "assistant",
            content = "A qubit can represent 0, 1, or both simultaneously via quantum superposition.",
            timestamp = 2000L
        )

        messageDao.insertMessage(userMsg)
        messageDao.insertMessage(assistantMsg)

        val messagesList = messageDao.getMessagesList(convId)
        assertEquals(2, messagesList.size)
        assertEquals("user", messagesList[0].role)
        assertEquals("Explain qubits and superposition.", messagesList[0].content)
        assertEquals(1000L, messagesList[0].timestamp)

        assertEquals("assistant", messagesList[1].role)
        assertEquals("A qubit can represent 0, 1, or both simultaneously via quantum superposition.", messagesList[1].content)
        assertEquals(2000L, messagesList[1].timestamp)

        val messagesFlow = messageDao.getMessagesForConversation(convId).first()
        assertEquals(2, messagesFlow.size)

        // Test cascade deletion
        conversationDao.deleteConversationById(convId)
        val afterDeleteMessages = messageDao.getMessagesList(convId)
        assertTrue(afterDeleteMessages.isEmpty())
    }
}
