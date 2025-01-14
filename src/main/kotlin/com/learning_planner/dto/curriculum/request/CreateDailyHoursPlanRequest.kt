package com.learning_planner.dto.curriculum.request

data class CreateDailyHoursPlanRequest (
    val courseId: String,
    val dailyStudyMinutes: Int,
    val studyFrequency: StudyFrequency,
    val preferredPlaybackSpeed: Float,
    val holidayInclusion: HolidayInclusionType,
)

enum class StudyFrequency {
    DAILY,              // Every day
    THREE_TIMES_WEEK,   // Three times per week
    TWO_TIMES_WEEK      // Two times per week
}