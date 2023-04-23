package com.shkarov.mytasks.data

data class Task(
    val id: String,
    val created: String,
    val description: String,
    val type: String,
    val deadLine: String,
    val deadLineMs: Long,
    val status: Status
)


enum class Status(val status: String){
    STARTED("started"),
    WAITING("waiting"),
    PAUSED("paused"),
    STOPPED("stopped")
}