package com.learning_planner.dto.course.response

import com.learning_planner.domain.course.Course

data class CourseResponse(
    val courseName: String,
    val instructor: String,
    val courseId: String,
) {
    companion object {
        fun from(course: Course): CourseResponse {
            return CourseResponse(
                courseName = course.title,
                instructor = course.instructor,
                courseId = course.id
            )
        }
    }
}