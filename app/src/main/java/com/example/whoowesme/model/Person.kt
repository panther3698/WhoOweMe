package com.example.whoowesme.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val personId: Long = 0,
    val name: String,
    val phoneNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
