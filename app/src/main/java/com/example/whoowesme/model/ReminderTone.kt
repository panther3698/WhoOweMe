package com.example.whoowesme.model

import com.example.whoowesme.R

enum class ReminderTone(val titleRes: Int, val descriptionRes: Int) {
    GENTLE(
        titleRes = R.string.reminder_tone_gentle_title,
        descriptionRes = R.string.reminder_tone_gentle_desc
    ),
    DIRECT(
        titleRes = R.string.reminder_tone_direct_title,
        descriptionRes = R.string.reminder_tone_direct_desc
    ),
    URGENT(
        titleRes = R.string.reminder_tone_urgent_title,
        descriptionRes = R.string.reminder_tone_urgent_desc
    )
}
