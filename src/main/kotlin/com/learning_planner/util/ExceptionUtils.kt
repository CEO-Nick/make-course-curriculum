package com.learning_planner.util

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

fun fail(message: String?="IllegalArgumentException 발생!") : Nothing {
    throw IllegalArgumentException(message)
}

fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID) : T {
    return this.findByIdOrNull(id) ?: fail("id에 해당하는 데이터가 없습니다!")
}