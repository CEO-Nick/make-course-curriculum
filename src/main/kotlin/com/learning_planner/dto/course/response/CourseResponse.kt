package com.learning_planner.dto.course.response

import com.learning_planner.domain.course.Course

data class CourseResponse(
    val courseName: String,
    val instructors: List<String>,
    val courseId: String,
    val lectureUnitCount: Int,
    val runtime: Int,
) {
    companion object {
        fun from(course: Course): CourseResponse {
            return CourseResponse(
                courseName = course.title,
                instructors = course.instructors,
                courseId = course.id,
                lectureUnitCount = course.lectureUnitCount,
                runtime = course.runtime,
            )
        }
    }
}