package com.learning_planner.domain.course

data class CurriculumData(
    val publishedAt: String,
    val lastUpdatedAt: String,
    val curriculum: List<Section>
)
