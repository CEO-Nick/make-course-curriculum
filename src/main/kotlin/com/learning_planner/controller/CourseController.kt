package com.learning_planner.controller

import com.learning_planner.dto.course.request.CreateCourseRequest
import com.learning_planner.dto.course.response.CourseResponse
import com.learning_planner.service.CourseService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/proxy")
class CourseController(
    private val courseService: CourseService
) {

    @PostMapping("/course")
    fun addCourse(@RequestBody request: CreateCourseRequest): CourseResponse {
        return CourseResponse.from(courseService.addCourse(request))
    }

    @GetMapping("/course")
    fun searchCourse(@RequestParam searchTerm: String): List<CourseResponse> {
        val searchCourses = courseService.searchCourse(searchTerm)
        return searchCourses.map { CourseResponse.from(it) }
    }

    @GetMapping("/course/all")
    fun getAllCourses(): List<CourseResponse> {
        return courseService.getLatestCourses().map { CourseResponse.from(it) }
    }
}