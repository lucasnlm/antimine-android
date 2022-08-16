package dev.lucasnlm.antimine.common.level.database.converters

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import org.junit.Assert.assertEquals
import org.junit.Test

class AreaConverterTest {
    private val expectedJson =
        """
        [
           {
              "id":1,
              "posX":2,
              "posY":3,
              "minesAround":5,
              "hasMine":false,
              "mistake":false,
              "isCovered":true,
              "mark":"None",
              "revealed":false,
              "neighborsIds":[],
              "dimNumber":false
           },
           {
              "id":2,
              "posX":5,
              "posY":3,
              "minesAround":0,
              "hasMine":true,
              "mistake":true,
              "isCovered":false,
              "mark":"PurposefulNone",
              "revealed":false,
              "neighborsIds":[],
              "dimNumber":false
           },
           {
              "id":3,
              "posX":1,
              "posY":1,
              "minesAround":3,
              "hasMine":true,
              "mistake":false,
              "isCovered":true,
              "mark":"Flag",
              "revealed":false,
              "neighborsIds":[],
              "dimNumber":false
           },
           {
              "id":4,
              "posX":0,
              "posY":0,
              "minesAround":6,
              "hasMine":false,
              "mistake":false,
              "isCovered":true,
              "mark":"Question",
              "revealed":true,
              "neighborsIds":[],
              "dimNumber":true
           }
        ]
        """.trimIndent().replace(
            " ",
            "",
        ).replace("\n", "")

    private val areaList =
        listOf(
            Area(
                1, 2, 3, 5,
                hasMine = false,
                mistake = false,
                isCovered = true,
                mark = Mark.None,
                revealed = false,
                neighborsIds = listOf(),
            ),
            Area(
                2, 5, 3, 0,
                hasMine = true,
                mistake = true,
                isCovered = false,
                mark = Mark.PurposefulNone,
                revealed = false,
                neighborsIds = listOf(),
            ),
            Area(
                3, 1, 1, 3,
                hasMine = true,
                mistake = false,
                isCovered = true,
                mark = Mark.Flag,
                revealed = false,
                neighborsIds = listOf(),
            ),
            Area(
                4, 0, 0, 6,
                hasMine = false,
                mistake = false,
                isCovered = true,
                mark = Mark.Question,
                revealed = true,
                neighborsIds = listOf(),
                dimNumber = true,
            ),
        )

    @Test
    fun toAreaList() {
        val fieldConverter = AreaConverter()
        val list = fieldConverter.toAreaList(expectedJson)
        assertEquals(
            areaList,
            list,
        )
    }

    @Test
    fun toJsonString() {
        val fieldConverter = AreaConverter()
        val result = fieldConverter.toJsonString(areaList)
        assertEquals(expectedJson, result)
    }
}
