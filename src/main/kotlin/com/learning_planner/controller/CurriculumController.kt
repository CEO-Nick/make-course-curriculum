package com.learning_planner.controller

import com.learning_planner.dto.curriculum.request.CurriculumCreateRequest
import com.learning_planner.dto.curriculum.response.CurriculumResponse
import com.learning_planner.service.CurriculumService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CurriculumController(
    private val curriculumService: CurriculumService
) {

    @PostMapping("/curriculum")
    fun makeCurriculum(@RequestBody request: CurriculumCreateRequest): CurriculumResponse {
        return curriculumService.makeCurriculum(request)
    }
}