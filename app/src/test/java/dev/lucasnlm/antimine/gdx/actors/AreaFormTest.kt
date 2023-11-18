package dev.lucasnlm.antimine.gdx.actors

import com.google.common.truth.Truth.assertThat
import dev.lucasnlm.antimine.gdx.actors.AreaForm.all
import dev.lucasnlm.antimine.gdx.actors.AreaForm.allBut
import dev.lucasnlm.antimine.gdx.actors.AreaForm.none
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AreaFormTest {
    @Test
    fun test_all() {
        assertTrue(AreaForm.ALL.all())

        listOf(
            AreaForm.TOP,
            AreaForm.BOTTOM,
            AreaForm.LEFT,
            AreaForm.RIGHT,
            AreaForm.TOP_LEFT,
            AreaForm.TOP_RIGHT,
            AreaForm.BOTTOM_LEFT,
            AreaForm.BOTTOM_RIGHT,
        ).forEach {
            assertThat(it.all()).isFalse()
        }
    }

    @Test
    fun test_none() {
        assertFalse(AreaForm.ALL.none())

        listOf(
            AreaForm.TOP,
            AreaForm.BOTTOM,
            AreaForm.LEFT,
            AreaForm.RIGHT,
            AreaForm.TOP_LEFT,
            AreaForm.TOP_RIGHT,
            AreaForm.BOTTOM_LEFT,
            AreaForm.BOTTOM_RIGHT,
        ).forEach {
            assertThat(it.none()).isFalse()
        }

        assertThat(0.none()).isTrue()
    }

    @Test
    fun test_all_but() {
        assertThat(AreaForm.ALL.allBut(AreaForm.ALL)).isFalse()

        listOf(
            AreaForm.TOP,
            AreaForm.BOTTOM,
            AreaForm.LEFT,
            AreaForm.RIGHT,
            AreaForm.TOP_LEFT,
            AreaForm.TOP_RIGHT,
            AreaForm.BOTTOM_LEFT,
            AreaForm.BOTTOM_RIGHT,
        ).forEach {
            assertThat(AreaForm.ALL.allBut(it)).isFalse()
        }

        val allButTop = merge(
            AreaForm.BOTTOM,
            AreaForm.LEFT,
            AreaForm.RIGHT,
            AreaForm.BOTTOM_LEFT,
            AreaForm.BOTTOM_RIGHT,
        ).allBut(AreaForm.TOP, AreaForm.TOP_LEFT, AreaForm.TOP_RIGHT)

        assertThat(allButTop).isTrue()

        val allButTopFalse = merge(
            AreaForm.BOTTOM,
            AreaForm.LEFT,
            AreaForm.RIGHT,
            AreaForm.TOP,
            AreaForm.BOTTOM_LEFT,
            AreaForm.BOTTOM_RIGHT,
        ).allBut(AreaForm.TOP, AreaForm.TOP_LEFT, AreaForm.TOP_RIGHT)

        assertThat(allButTopFalse).isFalse()
    }

    private fun merge(vararg flags: Int): Int {
        var result = 0
        flags.forEach { result = result or it }
        return result
    }
}
