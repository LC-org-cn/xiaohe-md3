package com.xiaohe.english.network.model

// ---- auth & common
data class AppVersion(
    val description: String? = null, val id: Long = 0, val url: String? = null,
    val versionName: String? = null, val versionNum: String? = null,
)
data class GradeInfo(val id: Long = 0, val name: String? = null, val orderNum: Int = 0)
data class DictData(val colorType: String? = null, val cssClass: String? = null, val label: String? = null, val value: String? = null)
data class AgreementInfo(val agreementType: Int = 0, val content: String? = null, val id: Long = 0, val title: String? = null)
data class CaptchaImage(val image: String? = null, val uuid: String? = null)
data class AreaInfo(val children: List<AreaInfo>? = null, val id: Long = 0, val level: Int = 0, val name: String? = null, val parentId: Long? = null)
data class SchoolInfo(val address: String? = null, val areaId: Long = 0, val areaName: String? = null, val id: Long = 0, val name: String? = null)
data class UserFeedback(
    val contactInformation: String? = null, val createTime: Long = 0,
    val feedbackTypeLevel1: String? = null, val feedbackTypeLevel2: String? = null,
    val id: Long = 0, val problemDescription: String? = null,
    val problemImgOrVideo1: String? = null, val problemImgOrVideo2: String? = null,
    val problemImgOrVideo3: String? = null, val problemOccurrenceTime: Long? = null,
    val processingContent: String? = null,
)
data class IntroductionPage(
    val id: Long = 0, val introductionType: Int = 0, val imageUrl: String? = null,
    val sort: Int = 0, val title: String? = null, val description: String? = null,
)
data class PresignUploadResponse(val url: String? = null, val headers: Map<String, String>? = null)

// ---- homework / work
data class HomeworkInfo(
    val completedQuestions: Int = 0, val deadline: String? = null, val description: String? = null,
    val endTime: String? = null, val id: Long = 0, val score: Float? = null,
    val startTime: String? = null, val status: Int = 0, val title: String? = null,
    val totalQuestions: Int = 0,
)
data class QuestionInfo(
    val correctAnswer: String? = null, val id: Long = 0, val options: List<String>? = null,
    val orderNum: Int = 0, val questionAudioUrl: String? = null,
    val questionContent: String? = null, val questionType: Int = 0, val score: Float = 0f,
)
data class HomeworkDetail(
    val completedQuestions: Int = 0, val deadline: String? = null, val description: String? = null,
    val id: Long = 0, val questions: List<QuestionInfo>? = null, val score: Float? = null,
    val status: Int = 0, val timeLimit: Int? = null, val title: String? = null,
    val totalQuestions: Int = 0, val totalScore: Float = 0f,
)
data class HomeworkProgress(val currentQuestionIndex: Int = 0, val homeworkId: Long = 0, val lastUpdateTime: String? = null)
data class HomeworkResult(
    val completedAt: String? = null, val correctRate: Float = 0f, val homeworkId: Long = 0,
    val rank: Int? = null, val score: Float = 0f, val timeUsed: Long = 0,
    val totalScore: Float = 0f, val totalStudents: Int? = null,
)
data class HomeworkPublishStudent(
    val id: String? = null, val homeworkId: Long = 0, val studentId: Long = 0,
    val homeworkStatus: String? = null, val homeworkTitle: String? = null,
    val endTime: String? = null,
)
data class ExercisesRecordItem(
    val createTime: Long? = null, val errorNumber: Int = 0, val errorRate: Float = 0f,
    val exercisesClass: String? = null, val exercisesName: String? = null,
    val exercisesNum: Int = 0, val exercisesType: String? = null,
    val finishedExercisesNum: Int = 0, val id: String? = null, val maxScore: Int = 0,
    val studentScore: Int = 0, val takeTime: Long = 0,
)
data class VocabularyNotebookItem(
    val id: Long = 0, val wordContent: String? = null, val wordPhonetic: String? = null,
    val wordParaphrase: String? = null, val wordAudioUrl: String? = null,
    val isStar: String? = null, val createTime: String? = null,
)

// ---- study / material
data class BookInfo(
    val bookCover: String? = null, val bookName: String? = null, val edition: String? = null,
    val educationalSystemType: String? = null, val grade: String? = null,
    val gradeId: Long? = null, val gradeName: String? = null, val id: Long = 0,
    val publisherId: String? = null, val semester: String? = null, val status: Int = 0,
    val termType: Int? = null, val unitCount: Int = 0,
)
data class BookChapterInfo(
    val bookInfoId: Long = 0, val chapterOrder: Int = 0, val chapterTitle: String? = null,
    val chapterType: String? = null, val chapterWordExamNumAll: Int? = null,
    val id: Long = 0, val parentId: Long? = null,
)
data class ChapterInfo(
    val bookChapterInfoRespVOList: List<ChapterInfo>? = null, val bookInfoId: Long = 0,
    val chapterOrder: Int? = null, val chapterTitle: String? = null,
    val chapterType: String? = null, val id: Long = 0, val parentId: Long? = null,
)
data class WordInfo(
    val bookInfoId: Long? = null, val id: Long = 0, val isMastered: Boolean = false,
    val isStar: String? = null, val unitId: Long? = null, val wordAudioUrl: String? = null,
    val wordContent: String? = null, val wordParaphrase: String? = null, val wordPhonetic: String? = null,
)
data class SearchWordDetail(
    val bookInfoId: Long? = null, val chapterInfoId: Long? = null, val id: Long = 0,
    val isStar: String? = null, val overallWordInfoId: Long = 0, val wordAudioUrl: String? = null,
    val wordContent: String? = null, val wordParaphrase: String? = null, val wordPhonetic: String? = null,
)
data class TextInfo(
    val bookInfoId: Long? = null, val difficulty: Int? = null, val id: Long = 0,
    val textAudioUrl: String? = null, val textContent: String? = null,
    val textTitle: String? = null, val unitId: Long? = null,
)
data class UnitInfo(val bookInfoId: Long = 0, val id: Long = 0, val textCount: Int = 0, val unitName: String? = null, val unitOrder: Int = 0, val wordCount: Int = 0)
data class StudyProgress(
    val bookInfoId: Long = 0, val completedTexts: Int = 0, val masteredWords: Int = 0,
    val progressPercent: Float = 0f, val studyDays: Int = 0,
    val totalTexts: Int = 0, val totalWords: Int = 0,
)
data class DailyData(val date: String? = null, val studyTime: Long = 0, val textCount: Int = 0, val wordCount: Int = 0)
data class StudyStatistics(
    val continuousDays: Int = 0, val todayStudyTime: Long = 0, val totalStudyTime: Long = 0,
    val totalTexts: Int = 0, val totalWords: Int = 0, val weeklyData: List<DailyData>? = null,
)
data class StudySituationBook(
    val id: Long = 0, val bookInfoId: Long = 0, val studentId: Long = 0,
    val masteredWords: Int = 0, val studiedTexts: Int = 0,
)
data class SearchRecord(val id: Long = 0, val keyword: String? = null, val createTime: String? = null)

// reading
data class BookTextSentence(
    val bookTextInfoId: Long? = null, val id: Long = 0, val sentenceAudio: String? = null,
    val sentenceContent: String? = null, val sentenceContentChs: String? = null,
    val sentenceImage: String? = null, val sentenceOrder: Int = 0,
    val sentenceParaphrase: String? = null, val threeParties: String? = null,
)
data class BookTextInfo(
    val bookInfoId: Long? = null, val bookTextSentenceDOList: List<BookTextSentence>? = null,
    val chapterInfoId: Long? = null, val id: Long = 0, val textAudio: String? = null,
    val textContent: String? = null, val textDescription: String? = null, val textTitle: String? = null,
)
data class BookWordExampleSentence(
    val id: Long = 0, val bookWordInfoId: Long = 0, val exampleSentence: String? = null,
    val exampleSentenceChs: String? = null, val exampleSentenceAudio: String? = null,
)
data class BookWordInfo(
    val audioUrl: String? = null, val bookInfoId: Long? = null, val chapterInfoId: Long? = null,
    val exercisesQuestionId: Long? = null, val id: Long = 0, val isFavorite: Boolean = false,
    val isImportWord: String? = null, val isKnow: Int? = null, val isLearned: Boolean = false,
    val isStar: String? = null, val wordBookOrder: Int = 0, val wordContent: String? = null,
    val wordImage: String? = null, val wordParaphrase: String? = null, val wordPhonetic: String? = null,
)
data class ReadingArticle(
    val id: Long = 0, val title: String? = null, val category: String? = null,
    val content: String? = null, val coverImage: String? = null, val difficulty: String? = null,
)
data class CurrentTextReadInfo(
    val coverImage: String? = null, val description: String? = null, val difficultyLevel: String? = null,
    val grade: String? = null, val id: String? = null, val includeArticleNum: Int = 0,
    val star: Int = 0, val title: String? = null,
)
data class CurrentTextReadCatalogue(val id: String? = null, val title: String? = null, val showNum: Int = 0)
data class CurrentTextReadArticle(
    val id: String? = null, val paragraphOrder: Int = 0, val content: String? = null, val audioUrl: String? = null,
)
data class BookFunDubbingInfo(
    val id: Long = 0, val chapterInfoId: Long? = null, val bookInfoId: Long? = null,
    val title: String? = null, val coverUrl: String? = null, val sentenceCount: Int = 0,
)
data class BookFunDubbingSentence(
    val id: Long = 0, val sentenceContent: String? = null, val sentenceAudio: String? = null,
    val sentenceContentChs: String? = null,
)
data class BookFunDubbingKeyWord(val id: Long = 0, val keyword: String? = null)
data class AlphabetInfo(
    val id: Long = 0, val alphabetType: String? = null, val alphabetContent: String? = null,
    val phoneticAlphabetClassifyOne: String? = null, val phoneticAlphabetClassifyTwo: String? = null,
    val phoneticAlphabetClassifyThree: String? = null, val audioUrl: String? = null,
)
data class AudioResource(
    val audioUrl: String? = null, val duration: Int? = null, val fileSize: Long? = null,
    val format: String? = null, val id: Long = 0, val resourceId: Long = 0, val resourceType: Int = 0,
)

// ---- question engine (listening / speaking / unified)
data class QuestionSpecialAudio(
    val audioControlType: String? = null, val audioOrder: Int? = null, val audioTitle: String? = null,
    val audioUrl: String? = null, val controlDuration: Int? = null, val filePath: String? = null,
    val id: String? = null, val playCount: Int? = null, val title: String? = null,
)
data class QuestionSpecialOption(val code: String? = null, val content: String? = null)
data class QuestionSpecialItem(
    val analysis: String? = null, val audioControlType: String? = null, val audioUrl: String? = null,
    val childList: List<QuestionSpecialItem>? = null, val controlDuration: Int? = null,
    val correctAnswer: String? = null, val exercisesQuestionAudioList: List<QuestionSpecialAudio>? = null,
    val options: List<QuestionSpecialOption>? = null, val playCount: Int? = null,
    val questionContent: String? = null, val questionId: String? = null,
    val questionNumber: Int = 0, val questionTitle: String? = null,
    val referenceText: String? = null, val score: Int = 0, val showDesc: Boolean = false,
    val title: String? = null,
)
data class QuestionSpecialExercise(
    val exerciseId: String? = null, val exerciseTitle: String? = null, val questionType: String? = null,
    val questions: List<QuestionSpecialItem>? = null, val recordId: String? = null,
    val timeLimit: Int? = null, val totalCount: Int = 0, val unitName: String? = null,
)
data class ExercisesSpecialCatalogue(
    val id: Long = 0, val exercisesSpecialName: String? = null, val exercisesSpecialType: String? = null,
    val exercisesSpecialClass: String? = null, val coverUrl: String? = null, val description: String? = null,
)
data class ExercisesSpecialCatalogueDetail(
    val id: String? = null, val title: String? = null, val description: String? = null,
    val questionCount: Int = 0, val coverUrl: String? = null,
)
data class SaveProgressRequest(
    val answers: List<String>? = null, val currentQuestionIndex: Int = 0,
    val currentTime: Long = 0, val exerciseId: String? = null, val recordId: String? = null,
)
data class AnswerItem(
    val isCorrect: Boolean = false, val questionId: String? = null,
    val timeSpent: Long = 0, val userAnswer: String? = null,
)
data class SpeakingAnswerItem(
    val audioUrl: String? = null, val duration: Long = 0, val evaluation: String? = null,
    val questionId: String? = null, val score: Int = 0, val timeSpent: Long = 0,
)
data class SubmitListeningAnswersRequest(
    val answers: List<AnswerItem>? = null, val exerciseId: String? = null,
    val finishTime: String? = null, val recordId: String? = null, val totalTime: Long = 0,
)
data class SubmitSpeakingAnswersRequest(
    val answers: List<SpeakingAnswerItem>? = null, val exerciseId: String? = null,
    val finishTime: String? = null, val recordId: String? = null, val totalTime: Long = 0,
)
data class SubmitAnswersResponse(
    val accuracy: Float = 0f, val correctCount: Int = 0, val rank: String? = null,
    val recordId: String? = null, val totalCount: Int = 0, val totalScore: Int = 0, val userScore: Int = 0,
)
data class CreateSelfPracticeRequest(
    val bookInfoId: Long = 0, val chapterInfoId: Long = 0, val practiceType: String? = null,
    val questionCount: Int = 0,
)
data class ExercisesInfo(
    val checkFinished: Boolean? = null, val exercisesStudentRecordInfo: Any? = null,
    val generatedFinished: Boolean = false, val homeworkAnswerTimes: String? = null,
)
data class CatalogueDetailQuestionInfoResponse(
    val total: Int = 0, val list: List<QuestionSpecialItem>? = null,
)

// ---- order / buy
data class ProductSpu(
    val actualPrice: Int = 0, val duration: String? = null, val id: Long = 0,
    val name: String? = null, val originalPrice: Int = 0, val tips: String? = null,
)
data class ProductSpuPage(val list: List<ProductSpu>? = null, val total: Long = 0)
data class ProductSpuShareInfo(
    val id: Long = 0, val name: String? = null, val actualPrice: Int = 0,
    val originalPrice: Int = 0, val duration: String? = null, val shareUrl: String? = null,
)
data class OrderItem(val spuId: Long = 0, val spuName: String? = null)
data class OrderInfo(
    val createTime: Long = 0, val id: Long = 0, val items: List<OrderItem>? = null,
    val orderNo: String? = null, val payPrice: Int = 0, val productCount: Int = 0, val status: Int = 0,
)
data class OrderDetail(
    val createTime: Long = 0, val id: Long = 0, val items: List<OrderItem>? = null,
    val orderNo: String? = null, val payPrice: Int = 0, val payTime: String? = null,
    val productCount: Int = 0, val receiverMobile: String? = null, val receiverName: String? = null,
    val status: Int = 0, val transactionId: String? = null,
)
