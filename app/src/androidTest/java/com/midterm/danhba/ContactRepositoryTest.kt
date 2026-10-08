package com.midterm.danhba

import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.midterm.danhba.data.ContactRepository
import com.midterm.danhba.model.Contact
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ContactRepositoryTest {
    private fun context(): ContextWrapper {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(app.cacheDir, "repository-test-" + UUID.randomUUID()).apply { mkdirs() }
        return object : ContextWrapper(app) {
            override fun getFilesDir() = folder
            override fun getApplicationContext() = this
        }
    }
    private fun awaitResult(action: ((Result<List<Contact>>) -> Unit) -> Unit): Result<List<Contact>> {
        val latch = CountDownLatch(1)
        var result: Result<List<Contact>>? = null
        action { result = it; latch.countDown() }
        assertTrue("Repository callback timed out", latch.await(10, TimeUnit.SECONDS))
        return result!!
    }
    @Test fun commitsSurviveNewRepositoryAndDeletion() {
        val ctx = context()
        val first = ContactRepository(ctx)
        val c = Contact("one", "Đặng Ánh", "0901234567", group = "family")
        assertTrue(awaitResult { first.list(it) }.getOrThrow().isEmpty())
        assertEquals(listOf(c), awaitResult { first.save(c, it) }.getOrThrow())
        awaitResult { first.toggleFavorite(c.id, it) }.getOrThrow()
        val second = ContactRepository(ctx)
        assertTrue(awaitResult { second.list(it) }.getOrThrow().single().isFavorite)
        assertEquals("Tên mới", awaitResult { second.save(c.copy(name = "Tên mới", isFavorite = true), it) }.getOrThrow().single().name)
        awaitResult { second.delete(c.id, it) }.getOrThrow()
        assertTrue(awaitResult { ContactRepository(ctx).list(it) }.getOrThrow().isEmpty())
    }
    @Test fun corruptFileIsPreservedAndMutationsFail() {
        val ctx = context()
        val file = File(ctx.filesDir, "contacts.json")
        file.writeText("broken-json")
        val repository = ContactRepository(ctx)
        assertTrue(awaitResult { repository.list(it) }.isFailure)
        assertTrue(awaitResult { repository.save(Contact("1", "An", "0901234567"), it) }.isFailure)
        assertEquals("broken-json", file.readText())
    }
    @Test fun failedWriteAndDuplicateKeepCommittedSnapshot() {
        val ctx = context()
        val repository = ContactRepository(ctx)
        val c = Contact("1", "An", "0901234567")
        awaitResult { repository.save(c, it) }.getOrThrow()
        assertTrue(awaitResult { repository.save(c.copy(id = "2", phone = "0901 234-567"), it) }.isFailure)
        assertEquals(listOf(c), awaitResult { repository.list(it) }.getOrThrow())
        // Block AtomicFile's staging path to simulate a write failure.
        File(ctx.filesDir, "contacts.json.new").mkdir()
        assertTrue(awaitResult { repository.save(c.copy(name = "Changed"), it) }.isFailure)
        assertEquals(listOf(c), awaitResult { repository.list(it) }.getOrThrow())
        assertEquals(listOf(c), awaitResult { ContactRepository(ctx).list(it) }.getOrThrow())
    }
}
