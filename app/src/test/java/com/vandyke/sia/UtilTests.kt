package com.vandyke.sia

import com.vandyke.sia.data.models.renter.fileExtension
import com.vandyke.sia.util.replaceLast
import org.amshove.kluent.shouldEqual
import org.junit.Test

class UtilTests {
    @Test
    fun replaceLast() {
        "wheee.jpg".replaceLast(".", " (1).") shouldEqual "wheee (1).jpg"
    }

    @Test
    fun fileExtension() {
        "wheee.jpg".fileExtension() shouldEqual "jpg"
        "he.llo.gif".fileExtension() shouldEqual "gif"
    }
}