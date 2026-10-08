package com.midterm.danhba

import com.midterm.danhba.model.Contact
import com.midterm.danhba.util.ContactSearch
import com.midterm.danhba.util.ContactValidator
import org.junit.Assert.*
import org.junit.Test

class ContactRulesTest {
    private val contact = Contact("1", "Đặng Ánh", "0901 234-567")
    @Test fun vietnameseSearchIsCaseAndAccentInsensitive() {
        assertTrue(ContactSearch.matches(contact, "DANG anh"))
        assertTrue(ContactSearch.matches(contact, "ÁNH"))
        assertFalse(ContactSearch.matches(contact, "Bình"))
    }
    @Test fun phoneSearchIgnoresFormattingAndEmptyNumericQueries() {
        assertTrue(ContactSearch.matches(contact, "0901-234"))
        assertTrue(ContactSearch.matches(contact, "  "))
        assertFalse(ContactSearch.matches(contact, "abc"))
        assertFalse(ContactSearch.matches(contact, "0988"))
    }
    @Test fun phoneValidationRejectsLettersAndMisplacedPlus() {
        assertTrue(ContactValidator.validPhone("+84 (901) 234-567"))
        assertFalse(ContactValidator.validPhone("09abc123456"))
        assertFalse(ContactValidator.validPhone("09+123456"))
        assertFalse(ContactValidator.validPhone("++84901234567"))
        assertFalse(ContactValidator.validPhone("12"))
        assertFalse(ContactValidator.validPhone(""))
        assertFalse(ContactValidator.validPhone("1234567890123456"))
    }
    @Test fun duplicatesAreBlockedButEditingSameContactIsAllowed() {
        assertTrue(ContactValidator.duplicatePhone("0901(234)567", "2", listOf(contact)))
        assertFalse(ContactValidator.duplicatePhone("0901(234)567", "1", listOf(contact)))
        assertFalse(ContactValidator.duplicatePhone("0909999999", "2", listOf(contact)))
    }
    @Test fun phoneNormalizationPreservesLeadingZeroAndCountryCode() {
        assertEquals("0901234567", ContactSearch.normalizePhone("0901 234-567"))
        assertEquals("+84901234567", ContactSearch.normalizePhone("+84 (901) 234-567"))
    }
}
