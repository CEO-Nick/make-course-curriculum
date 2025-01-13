package com.learning_planner.controller

import com.learning_planner.dto.curriculum.request.CourseCreateRequest
import com.learning_planner.dto.curriculum.response.CourseResponse
import com.learning_planner.service.CourseService
import org.springframework.web.bind.annotation.*

@RestController
class CourseController(
    private val courseService: CourseService
) {

    @PostMapping("/course")
    fun addCourse(@RequestBody request: CourseCreateRequest): CourseResponse {
        return CourseResponse.from(courseService.addCourse(request))
    }

    @GetMapping("/course")
    fun searchCourse(@RequestParam searchTerm: String): List<CourseResponse> {
        val searchCourses = courseService.searchCourse(searchTerm)
        return searchCourses.map { CourseResponse.from(it) }
    }

    @GetMapping("/course/all")
    fun getAllCourses(): List<CourseResponse> {
        return courseService.findAll().map { CourseResponse.from(it) }
    }
}