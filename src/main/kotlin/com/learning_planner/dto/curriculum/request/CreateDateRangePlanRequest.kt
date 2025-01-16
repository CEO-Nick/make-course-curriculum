package com.learning_planner.dto.curriculum.request

data class CreateDateRangePlanRequest (
    val courseId: String,
    val startDate: String, // "2025-01-13" 형식
    val endDate: String,
    val preferredPlaybackSpeed: Float,
    val studyFrequency: StudyFrequency,
    val holidayInclusion: HolidayInclusionType,
)

enum class HolidayInclusionType {
    INCLUDE_HOLIDAYS,
    EXCLUDE_HOLIDAYS,
}