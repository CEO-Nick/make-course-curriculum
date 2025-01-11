package com.learning_planner.dto.curriculum.response

data class Unit(
    val id: Long,
    val title: String,
    val runtime: Int,
    val isPreview: Boolean,
    val hasAttachment: Boolean,
    val isComplete: Boolean
)