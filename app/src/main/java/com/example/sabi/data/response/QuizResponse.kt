package com.example.sabi.data.response
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class QuizGroupsResponse(
    val status: String,
    val message: String,
    val data: QuizGroupsData
)

data class QuizGroupsData(
    val groups: List<QuizGroup>
)

data class QuizGroup(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val totalQuestion: Int
)

data class QuizGroupResponse(
    val status: String,
    val message: String,
    val data: UserAnswerGroupData
)

data class UserAnswerGroupData(
    val userAnswerGroup: UserAnswerGroup
)

data class UserAnswerGroup(
    val id: Int,
    val userId: Int,
    val updatedAt: String,
    val createdAt: String
)

data class QuizQuestionsResponse(
    val status: String,
    val message: String,
    val data: UserAnswerGroupQuestionsData
)

data class UserAnswerGroupQuestionsData(
    val userAnswerGroup: UserAnswerGroupWithQuestions
)

data class UserAnswerGroupWithQuestions(
    val id: Int,
    val userAnswers: List<UserAnswer>
)

data class UserAnswer(
    val id: Int,
    val question: Question,
    var selectedOptionId: Int? = null
)

data class Question(
    val id: Int,
    val question: String,
    val options: List<Option>
)

data class Option(
    val id: Int,
    val option: String
)

data class QuizResultsResponse(
    val status: String,
    val message: String,
    val data: ResultsData
)

data class ResultsData(
    val results: List<QuizResult>
)

@Parcelize
data class QuizResult(
    val userAnswerId: Int,
    val isCorrect: Boolean
) : Parcelable

data class CreateQuizGroupBody(
    val groupId: Int,
    val limit: Int
)

data class SubmitQuizBody(
    val userAnswerGroupId: Int,
    val quizAnswers: List<QuizAnswer>
)

data class QuizAnswer(
    val userAnswerId: Int,
    val optionId: Int
)