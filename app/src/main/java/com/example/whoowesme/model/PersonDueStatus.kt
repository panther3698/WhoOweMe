package com.example.whoowesme.model

data class PersonDueStatus(
    val person: Person,
    val balance: Double,
    val dueDate: Long,
    val promisedPaymentDate: Long? = null,
    val isOverdue: Boolean,
    val isPromiseMissed: Boolean,
    val daysOffset: Long
)
