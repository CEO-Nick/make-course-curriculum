package com.learning_planner.domain.course

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "courses")
class Course(
    @Id
    val id: String,  // 인프런 강의 ID

    @TextIndexed
    val title: String,  // 강의명
    val instructors: List<String>,  // 강사들명
    val lectureUnitCount: Int,
    val runtime: Int,
    val curriculum: CurriculumData?  // 검색 시, 커리큘럼 데이터 안가져오기 위해 nullable 설정


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Course

        if (id != other.id) return false
        if (title != other.title) return false
        if (instructors != other.instructors) return false
        if (lectureUnitCount != other.lectureUnitCount) return false
        if (runtime != other.runtime) return false
        if (curriculum != other.curriculum) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + instructors.hashCode()
        result = 31 * result + lectureUnitCount
        result = 31 * result + runtime
        result = 31 * result + (curriculum?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Course(id='$id', title='$title', instructors=$instructors, lectureUnitCount=$lectureUnitCount, runtime=$runtime, curriculum=$curriculum)"
    }


}

