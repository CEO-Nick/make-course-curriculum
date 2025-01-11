package com.learning_planner.domain.course

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : MongoRepository<Course, String> {
}