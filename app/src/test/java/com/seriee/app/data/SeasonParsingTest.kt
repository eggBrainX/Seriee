package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeasonParsingTest {

    @Test
    fun `parses numeric season`() {
        assertEquals(4, parseSeasonNumberFromText("Re:Zero Season 4"))
        assertEquals(2, parseSeasonNumberFromText("Some Show Season-2"))
        assertEquals(3, parseSeasonNumberFromText("Some Show Season: 3"))
    }

    @Test
    fun `parses roman numeral season`() {
        assertEquals(4, parseSeasonNumberFromText("Some Show Season IV"))
        assertEquals(1, parseSeasonNumberFromText("Some Show Season I"))
    }

    @Test
    fun `parses ordinal season and split cour`() {
        assertEquals(2, parseSeasonNumberFromText("Some Show 2nd Season"))
        assertEquals(2, parseSeasonNumberFromText("Some Show Part 2"))
    }

    @Test
    fun `parses shorthand season code`() {
        assertEquals(4, parseSeasonNumberFromText("Some Show S4"))
        assertEquals(4, parseSeasonNumberFromText("Some Show S04"))
        assertEquals(4, parseSeasonNumberFromText("Some Show - S4 - Episode 1"))
    }

    @Test
    fun `rejects missing or out of range seasons`() {
        assertNull(parseSeasonNumberFromText(null))
        assertNull(parseSeasonNumberFromText(""))
        assertNull(parseSeasonNumberFromText("Arcane"))
        assertNull(parseSeasonNumberFromText("Season 0"))
        assertNull(parseSeasonNumberFromText("Season 99"))
    }

    @Test
    fun `does not confuse words containing s and digits`() {
        assertNull(parseSeasonNumberFromText("Se7en"))
        assertNull(parseSeasonNumberFromText("News4"))
    }
}
