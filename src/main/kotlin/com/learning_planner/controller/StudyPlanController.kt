package com.learning_planner.controller

import com.learning_planner.dto.curriculum.request.CreateDailyHoursPlanRequest
import com.learning_planner.dto.curriculum.request.CreateDateRangePlanRequest
import com.learning_planner.service.LearningPlanService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class StudyPlanController(
    private val learningPlanService: LearningPlanService
) {

    @PostMapping("/study-plans/date-range")
    fun makeCurriculum(@RequestBody request: CreateDateRangePlanRequest): String? {
        return learningPlanService.makeCurriculum(request)?.result?.output?.content
    }

    @PostMapping("/study-plans/daily-hours")
    fun makeCurriculum(@RequestBody request: CreateDailyHoursPlanRequest): String? {
        return learningPlanService.makeCurriculum(request)?.result?.output?.content
    }

}