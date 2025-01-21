package com.learning_planner.domain.course

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : MongoRepository<Course, String> {

    // 검색 결과에는 curriculum 데이터 제외
    @Query(
        value = "{ \$text: { \$search: ?0 }}",
        fields = "{ " +
                "score: { \$meta: 'textScore' }, " +
                "curriculum: 0 " +
                "}"
    )
    fun searchCourses(term: String, sort: Sort): List<Course>
}