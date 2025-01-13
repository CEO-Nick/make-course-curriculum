package com.learning_planner.controller

import com.learning_planner.service.CurriculumService
import org.springframework.web.bind.annotation.RestController

@RestController("/curriculum")
class CurriculumController(
    private val curriculumService: CurriculumService
) {


}