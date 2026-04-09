package com.example.whoowesme.model

enum class ReminderTone(val title: String, val description: String) {
    GENTLE(
        title = "Gentle",
        description = "Friendly follow-up for people who usually pay back."
    ),
    DIRECT(
        title = "Direct",
        description = "Clear and professional reminder with the amount due."
    ),
    URGENT(
        title = "Urgent",
        description = "Firm wording for overdue balances that need action."
    )
}
