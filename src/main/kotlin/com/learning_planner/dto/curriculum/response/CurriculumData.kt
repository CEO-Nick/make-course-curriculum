package com.learning_planner.dto.curriculum.response

data class CurriculumData(
    val publishedAt: String,
    val lastUpdatedAt: String,
    val curriculum: List<Section>
)
