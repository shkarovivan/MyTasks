package com.shkarov.mytasks.data

import android.os.Parcelable
import androidx.room.*
import com.shkarov.mytasks.data_base.TaskDataBaseContract
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = TaskDataBaseContract.TABLE_NAME)

@TypeConverters(StatusConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = TaskDataBaseContract.Columns.ID)
    val id: String,
    @ColumnInfo(name = TaskDataBaseContract.Columns.CREATED)
    val created: String,
    @ColumnInfo(name = TaskDataBaseContract.Columns.TITLE)
    val title: String,
    @ColumnInfo(name = TaskDataBaseContract.Columns.DESCRIPTION)
    val description: String,
    @ColumnInfo(name = TaskDataBaseContract.Columns.TYPE)
    val type: String,
    @ColumnInfo(name = TaskDataBaseContract.Columns.DEAD_LINE)
    val deadLine: String,
    @ColumnInfo(name = TaskDataBaseContract.Columns.DEAD_LINE_MS)
    val deadLineMs: Long,
    @ColumnInfo(name = TaskDataBaseContract.Columns.STATUS)
    val status: Status
): Parcelable


enum class Status(val status: String){
    STARTED("started"),
    WAITING("waiting"),
    PAUSED("paused"),
    STOPPED("stopped")
}

enum class Type (val value: String) {
    DAILY("daily"),
    MEDIUM("medium"),
    LARGE("large"),
}

class StatusConverter {
    @TypeConverter
    fun convertStatusToString(status: Status): String = status.status

    @TypeConverter
    fun convertStringToStatus( statusString: String) : Status = Status.valueOf(statusString)
}