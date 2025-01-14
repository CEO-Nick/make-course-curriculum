package com.learning_planner.dto.curriculum.request

data class CreateDailyHoursPlanRequest (
    val courseId: String,
    val dailyStudyMinutes: Int,
    val studyFrequency: StudyFrequency,
    val preferredPlaybackSpeed: Float,
)

enum class StudyFrequency {
    DAILY,              // 매일
    FIVE_TIMES_WEEK,    // 주 5회
    THREE_TIMES_WEEK,   // 주 3회
    TWO_TIMES_WEEK      // 주 2회
}