package com.learning_planner.controller

import com.learning_planner.domain.course.Section
import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.dto.curriculum.response.DailyHoursStudyPlanResponse
import com.learning_planner.service.CourseService
import com.learning_planner.service.LearningPlanService
import com.learning_planner.service.LectureSchedule
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class StudyPlanController(
    private val learningPlanService: LearningPlanService,
    private val courseService: CourseService,
) {
    @PostMapping("/study-plans/date-range")
    fun makeCurriculum(@RequestBody request: CreateDateRangePlanRequest): List<LectureSchedule> {
        return learningPlanService.makeCurriculum(request)
    }

    @PostMapping("/study-plans/daily-hours")
    fun makeCurriculum(@RequestBody request: CreateDailyHoursPlanRequest): DailyHoursStudyPlanResponse {
        val newCurriculum = learningPlanService.makeCurriculum(request)
        val response = DailyHoursStudyPlanResponse(
            weeklyPlans = newCurriculum,
            totalWeeks = newCurriculum.size,
            totalDays = newCurriculum.last().dayPlans.last().dayNumber,
        )
        return response
    }

    @GetMapping("/curriculum")
    fun getCurriculum(@RequestParam courseId: String ): List<Section> {
        return courseService.getCurriculum(courseId)
    }

//    @PostMapping("/study-plans/date-range/AI")
//    fun makeCurriculumWithAI(@RequestBody request: CreateDateRangePlanRequest): String? {
//        return learningPlanService.makeCurriculumWithAI(request)
//    }
//
//    @PostMapping("/study-plans/daily-hours/AI")
//    fun makeCurriculumWithAI(@RequestBody request: CreateDailyHoursPlanRequest): String? {
//        return learningPlanService.makeCurriculumWithAI(request)
//    }

}