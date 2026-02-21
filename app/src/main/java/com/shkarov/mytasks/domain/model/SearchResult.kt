package com.shkarov.mytasks.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchResult(
    val answer: String = "",
    val ids: ArrayList<String> = arrayListOf()
) : Parcelable