package com.shkarov.mytasks.data_base

object TaskDataBaseContract {
    const val TABLE_NAME = "tasks_database"

    object Columns{
        const val ID = "id"
        const val CREATED = "created"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val TYPE = "type"
        const val DEAD_LINE = "deadLine"
        const val DEAD_LINE_MS = "deadLineMs"
        const val STATUS = "status"
    }
}
