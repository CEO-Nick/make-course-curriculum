package com.learning_planner.dto.course.response

class CourseInfoApiResponse(
    val data: SimpleCourseData
)

data class SimpleCourseData(
    val title: String,
    val unitSummary: UnitSummary,
    val instructors: List<Instructor>
)

data class UnitSummary(
    val lectureUnitCount: Int,
    val runtime: Int
)

data class Instructor(
    val name: String
)