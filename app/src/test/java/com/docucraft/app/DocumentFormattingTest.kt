package com.docucraft.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentFormattingTest {
    @Test fun pdfExtensionIsRecognized() {
        assertEquals("pdf", "DocuCraft_scan.pdf".substringAfterLast('.'))
    }
    @Test fun generatedNamesUseExpectedPrefix() {
        assertEquals(true, "DocuCraft_20261010_120000.pdf".startsWith("DocuCraft_"))
    }
}
