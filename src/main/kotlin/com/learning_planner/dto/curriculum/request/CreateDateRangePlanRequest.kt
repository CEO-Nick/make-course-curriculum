package com.learning_planner.dto.curriculum.request

data class CreateDateRangePlanRequest (
    val courseId: String,
    val startDate: String, // "2025-01-13" 형식
    val endDate: String,
    val preferredPlaybackSpeed: Float,
    val studyFrequency: StudyFrequency,
    val holidayInclusion: HolidayInclusionType,
)

enum class WeekendStudyPlan {
    INCLUDE_WEEKENDS,       // 주말 포함
    EXCLUDE_WEEKENDS,       // 주말 제외
    WEEKEND_ONLY_ONE_DAY,   // 주말 하루만 포함
}

enum class HolidayInclusionType {
    INCLUDE_HOLIDAYS,
    EXCLUDE_HOLIDAYS,
}