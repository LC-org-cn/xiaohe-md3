package com.xiaohe.english.network.api

import com.xiaohe.english.network.ApiConstants
import com.xiaohe.english.network.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface AuthApiService {
    @POST("/app-api/member/auth/sms-login")
    suspend fun loginByMobile(@Body request: PhoneLoginRequest, @Header(ApiConstants.HEADER_TENANT_ID) tenantId: String = ApiConstants.TENANT_ID): BaseResponse<TokenInfo>

    @POST("/app-api/member/auth/loginByUsername")
    suspend fun loginByUsername(@Body request: LoginRequest, @Header(ApiConstants.HEADER_TENANT_ID) tenantId: String = ApiConstants.TENANT_ID): BaseResponse<TokenInfo>

    @POST("/app-api/member/auth/logout")
    suspend fun logout(): BaseResponse<Any>

    @POST("/app-api/member/auth/refresh-token")
    suspend fun refreshToken(@Query("refreshToken") refreshToken: String): BaseResponse<TokenInfo>

    @POST("/app-api/member/auth/send-sms-code")
    suspend fun sendSmsCode(@Query("mobile") mobile: String, @Query("scene") scene: Int): BaseResponse<Any>

    @POST("/app-api/member/auth/validate-sms-code")
    suspend fun validateSmsCode(@Body body: Map<String, String>): BaseResponse<Any>
}

interface UserApiService {
    @GET("/app-api/study/student-info/get-qrcode-for-binding")
    suspend fun getQrcodeForBinding(@Query("studentId") studentId: String): BaseResponse<String>

    @GET("/app-api/member/user/get-student-info")
    suspend fun getStudentInfo(): BaseResponse<StudentInfo>

    @PUT("/app-api/member/user/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): BaseResponse<Any>

    @PUT("/app-api/member/user/update-mobile")
    suspend fun updateMobile(@Body body: Map<String, String>): BaseResponse<Any>

    @PUT("/app-api/member/user/update")
    suspend fun updateUserInfo(@Body body: Map<String, Any>): BaseResponse<Any>
}

interface StudyApiService {
    @POST("/app-api/study/study-record/create")
    suspend fun createStudyRecord(@Body body: Map<String, Any>): BaseResponse<Long>

    @DELETE("/app-api/study/search-record/delete")
    suspend fun deleteSearchRecord(@Query("id") id: Long): BaseResponse<Any>

    @DELETE("/app-api/study/search-record/delete-batch")
    suspend fun deleteSearchRecordBatch(@Query("ids") ids: String): BaseResponse<Any>

    @GET("/app-api/material/book-info/get")
    suspend fun getBookInfo(@Query("id") id: Long): BaseResponse<BookInfo>

    @GET("/app-api/material/book-word-info/page-with-situation-word")
    suspend fun getBookWordInfoWithSituationWord(@Query("bookInfoId") bookInfoId: Long?, @Query("chapterInfoId") chapterInfoId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<SearchWordDetail>>

    @GET("/app-api/study/question-special/listening/list")
    suspend fun getListeningQuestions(@Query("exerciseId") exerciseId: String, @Query("listeningType") listeningType: String): BaseResponse<QuestionSpecialExercise>

    @GET("/app-api/material/book-text-info/page-with-details")
    suspend fun getMaterialBookTextPage(@Query("chapterInfoId") chapterInfoId: Long?, @Query("bookInfoId") bookInfoId: Long?): BaseResponse<PageResponse<BookTextInfo>>

    @GET("/app-api/material/book-chapter-info/page-tree")
    suspend fun getMaterialUnitTreePage(@Query("bookInfoId") bookInfoId: Long, @Query("chapterType") chapterType: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<ChapterInfo>>

    @GET("/app-api/study/search-record/page-search-record")
    suspend fun getSearchRecordPage(): BaseResponse<PageResponse<SearchRecord>>

    @GET("/app-api/study/question-special/speaking/list")
    suspend fun getSpeakingQuestions(@Query("exerciseId") exerciseId: String, @Query("speakingType") speakingType: String): BaseResponse<QuestionSpecialExercise>

    @GET("/app-api/study/study-record/progress")
    suspend fun getStudyProgress(@Query("bookInfoId") bookInfoId: Long): BaseResponse<StudyProgress>

    @GET("/app-api/study/statistics/get")
    suspend fun getStudyStatistics(@Query("startDate") startDate: String, @Query("endDate") endDate: String): BaseResponse<StudyStatistics>

    @GET("/app-api/material/text-info/get")
    suspend fun getTextInfo(@Query("id") id: Long): BaseResponse<TextInfo>

    @GET("/app-api/material/text-info/page")
    suspend fun getTextPage(@Query("bookInfoId") bookInfoId: Long, @Query("unitId") unitId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<TextInfo>>

    @GET("/app-api/study/search-record/get-word-info")
    suspend fun getWordInfo(@Query("id") id: Long): BaseResponse<WordInfo>

    @GET("/app-api/material/overall-word-info/page")
    suspend fun getWordPage(@Query("wordContent") wordContent: String, @Query("wordParaphrase") wordParaphrase: String, @Query("bookInfoId") bookInfoId: Long, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<WordInfo>>

    @POST("/app-api/study/question-special/listening/save-progress")
    suspend fun saveListeningProgress(@Body body: SaveProgressRequest): BaseResponse<Any>

    @POST("/app-api/study/question-special/speaking/save-progress")
    suspend fun saveSpeakingProgress(@Body body: SaveProgressRequest): BaseResponse<Any>

    @POST("/app-api/study/question-special/listening/submit")
    suspend fun submitListeningAnswers(@Body body: SubmitListeningAnswersRequest): BaseResponse<SubmitAnswersResponse>

    @POST("/app-api/study/question-special/speaking/submit")
    suspend fun submitSpeakingAnswers(@Body body: SubmitSpeakingAnswersRequest): BaseResponse<SubmitAnswersResponse>

    @PUT("/app-api/study/student-info/update-grade")
    suspend fun updateStudentGradeInfo(@Body body: Map<String, Any>): BaseResponse<Any>

    @PUT("/app-api/study/word-master/update")
    suspend fun updateWordMaster(@Body body: Map<String, Any>): BaseResponse<Any>
}

interface WorkApiService {
    @POST("/app-api/study/homework/complete")
    suspend fun completeHomework(@Query("homeworkId") homeworkId: Long): BaseResponse<HomeworkResult>

    @POST("/app-api/study/exercises-info/appCreateStudentPractise")
    suspend fun createPractice(@Body body: Map<String, Any>): BaseResponse<String>

    @POST("/app-api/study/exercises-info/appCreatePractiseSelf")
    suspend fun createSelfPractice(@Body body: CreateSelfPracticeRequest): BaseResponse<String>

    @POST("/app-api/study/situation-word/create")
    suspend fun createSituationWord(@Body body: Map<String, Any>): BaseResponse<Any>

    @DELETE("/app-api/study/situation-word/delete")
    suspend fun deleteSituationWord(@Query("id") id: String): BaseResponse<Any>

    @PUT("/app-api/study/exercises-info/finishExercises")
    suspend fun finishExercises(@Body body: Map<String, Any>): BaseResponse<Any>

    @GET("/app-api/study/exercises-info/getCatalogueDetailQuestionInfoList")
    suspend fun getCatalogueDetailQuestionInfoList(@Query("studentRecordCatalogueId") studentRecordCatalogueId: String): BaseResponse<CatalogueDetailQuestionInfoResponse>

    @GET("/app-api/study/exercises-info/appGetExercisesById")
    suspend fun getExercisesById(@Query("id") id: String): BaseResponse<ExercisesInfo>

    @GET("/app-api/material/exercises-question/get-with-all")
    suspend fun getExercisesQuestionByIdWithAll(@Query("id") id: String): BaseResponse<Any>

    @GET("/app-api/material/exercises-question/get-with-all")
    suspend fun getExercisesQuestionByIdWithOption(@Query("id") id: String): BaseResponse<Any>

    @GET("/app-api/study/exercises-info/pageStudent")
    suspend fun getExercisesRecordPage(@Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int, @Query("studentId") studentId: Long?, @Query("exercisesClass") exercisesClass: String, @Query("exercisesType") exercisesType: String, @Query("startDate") startDate: String, @Query("endDate") endDate: String): BaseResponse<PageResponse<ExercisesRecordItem>>

    @GET("/app-api/study/homework/get")
    suspend fun getHomeworkDetail(@Query("id") id: Long): BaseResponse<HomeworkDetail>

    @GET("/app-api/study/homework/page")
    suspend fun getHomeworkPage(@Query("status") status: Int?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<HomeworkInfo>>

    @GET("/app-api/study/homework-progress/get")
    suspend fun getHomeworkProgress(@Query("homeworkId") homeworkId: Long): BaseResponse<HomeworkProgress>

    @GET("/app-api/study/homework-publish-student/get")
    suspend fun getHomeworkPublishStudent(@Query("id") id: String): BaseResponse<HomeworkPublishStudent>

    @GET("/app-api/study/homework-publish-student/page")
    suspend fun getHomeworkPublishStudentPage(@Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int, @Query("studentId") studentId: Long?, @Query("homeworkStatus") homeworkStatus: String): BaseResponse<PageResponse<HomeworkPublishStudent>>

    @GET("/app-api/study/homework-publish-student/pageList")
    suspend fun getHomeworkPublishStudentPageList(@Query("homeworkId") homeworkId: Long?, @Query("studentId") studentId: Long?, @Query("homeworkStatus") homeworkStatus: String, @Query("pageNum") pageNum: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<HomeworkPublishStudent>>

    @GET("/app-api/study/homework-question/list")
    suspend fun getHomeworkQuestions(@Query("homeworkId") homeworkId: Long): BaseResponse<List<QuestionInfo>>

    @GET("/app-api/study/homework-result/get")
    suspend fun getHomeworkResult(@Query("homeworkId") homeworkId: Long): BaseResponse<HomeworkResult>

    @GET("/app-api/study/vocabulary-notebook-detail/page")
    suspend fun getVocabularyNotebookPage(@Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<VocabularyNotebookItem>>

    @POST("/app-api/study/homework-progress/save")
    suspend fun saveHomeworkProgress(@Body body: Map<String, Any>): BaseResponse<Any>

    @POST("/app-api/study/homework-answer/submit")
    suspend fun submitHomeworkAnswer(@Body body: Map<String, Any>): BaseResponse<Any>
}

interface CommonApiService {
    @POST("/app-api/system/user-feedback/create-app")
    suspend fun createFeedback(@Body body: Map<String, Any>): BaseResponse<Long>

    @GET("/app-api/system/user-agreement/page")
    suspend fun getAgreementByType(@Query("agreementType") agreementType: Int): BaseResponse<PageResponse<AgreementInfo>>

    @GET("/app-api/system/area/tree")
    suspend fun getAreaTree(@Query("parentId") parentId: Long?): BaseResponse<List<AreaInfo>>

    @GET("/app-api/system/captcha/image")
    suspend fun getCaptchaImage(): BaseResponse<CaptchaImage>

    @GET("/app-api/system/dict-data/type")
    suspend fun getDictDataByType(@Query("type") type: String): BaseResponse<List<DictData>>

    @GET("/app-api/system/grade/list")
    suspend fun getGradeList(): BaseResponse<List<GradeInfo>>

    @GET("/app-api/index/data")
    suspend fun getIndexData(@Query("appType") appType: String, @Query("studentId") studentId: Long, @Query("homeworkStatus") homeworkStatus: String): BaseResponse<IndexDataResponse>

    @GET("/app-api/system/introduction-page/page")
    suspend fun getIntroductionPages(@Query("introductionType") introductionType: Int, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<IntroductionPage>>

    @GET("/app-api/system/app-version-file/get-latest")
    suspend fun getLatestVersion(@Query("appType") appType: String): BaseResponse<AppVersion>

    @GET("/app-api/system/notify-message/my-page")
    suspend fun getNotifyMessagePage(@Query("readStatus") readStatus: Boolean?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<NotifyMessage>>

    @POST("/app-api/infra/file/presign-upload-url")
    suspend fun getPresignUploadUrl(@Body body: Map<String, String>): BaseResponse<PresignUploadResponse>

    @GET("/app-api/system/school/list")
    suspend fun getSchoolList(@Query("areaId") areaId: Long, @Query("name") name: String): BaseResponse<List<SchoolInfo>>

    @GET("/app-api/system/notify-message/get-unread-count")
    suspend fun getUnreadMessageCount(): BaseResponse<Int>

    @GET("/app-api/system/notify-message/get-unread-list")
    suspend fun getUnreadMessageList(@Query("size") size: Int): BaseResponse<List<NotifyMessage>>

    @GET("/app-api/system/user-feedback/page")
    suspend fun getUserFeedbackPage(@Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<UserFeedback>>

    @PUT("/app-api/system/notify-message/update-all-read")
    suspend fun markAllMessagesAsRead(): BaseResponse<Any>

    @PUT("/app-api/system/notify-message/update-read")
    suspend fun markMessagesAsRead(@Query("ids") ids: String): BaseResponse<Any>

    @POST("/app-api/system/captcha/verify")
    suspend fun verifyCaptcha(@Body body: Map<String, String>): BaseResponse<Boolean>

    @Multipart
    @POST("/app-api/system/user-feedback/submit-problem-attachment")
    suspend fun uploadFeedbackAttachment(@Part part: MultipartBody.Part): BaseResponse<String>

    @Multipart
    @POST("/app-api/infra/file/upload")
    suspend fun uploadFile(@Part part: MultipartBody.Part, @Query("path") path: String): BaseResponse<String>
}

interface MaterialApiService {
    @GET("/app-api/material/audio-resource/download")
    suspend fun downloadAudio(@Query("audioUrl") audioUrl: String): BaseResponse<String>

    @GET("/app-api/material/alphabet-info/get")
    suspend fun getAlphabetInfoById(@Query("id") id: Long): BaseResponse<AlphabetInfo>

    @GET("/app-api/material/alphabet-info/page")
    suspend fun getAlphabetInfoPage(@Query("alphabetType") alphabetType: String, @Query("phoneticAlphabetClassifyOne") classifyOne: String, @Query("phoneticAlphabetClassifyTwo") classifyTwo: String, @Query("phoneticAlphabetClassifyThree") classifyThree: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<AlphabetInfo>>

    @GET("/app-api/material/alphabet-info/page-with-details")
    suspend fun getAlphabetInfoPageWithDetails(@Query("alphabetType") alphabetType: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<AlphabetInfo>>

    @GET("/app-api/material/audio-resource/get")
    suspend fun getAudioResource(@Query("resourceId") resourceId: Long, @Query("resourceType") resourceType: Int): BaseResponse<AudioResource>

    @GET("/app-api/material/book-chapter-info/page")
    suspend fun getBookChapterPage(@Query("bookInfoId") bookInfoId: Long, @Query("chapterType") chapterType: String, @Query("exercisesType") exercisesType: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<BookChapterInfo>>

    @GET("/app-api/material/book-info/get")
    suspend fun getBookDetail(@Query("id") id: Long): BaseResponse<BookInfo>

    @GET("/app-api/material/book-fun-dubbing-info/page-with-details")
    suspend fun getBookFunDubbingPageWithDetails(@Query("chapterInfoId") chapterInfoId: Long?, @Query("bookInfoId") bookInfoId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<BookFunDubbingInfo>>

    @GET("/app-api/material/book-fun-dubbing-info/get-sentence-by-id")
    suspend fun getBookFunDubbingSentenceById(@Query("id") id: Long): BaseResponse<BookFunDubbingSentence>

    @GET("/app-api/material/book-fun-dubbing-info/get-with-details")
    suspend fun getBookFunDubbingWithDetails(@Query("id") id: Long): BaseResponse<BookFunDubbingInfo>

    @GET("/app-api/material/book-info/list")
    suspend fun getBookList(@Query("gradeId") gradeId: Long?): BaseResponse<List<BookInfo>>

    @GET("/app-api/material/book-info/page-by-localuser")
    suspend fun getBookListByLocalUser(@Query("educationalSystemType") educationalSystemType: String, @Query("grade") grade: String, @Query("publisherId") publisherId: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<BookInfo>>

    @GET("/app-api/material/book-text-info/page-with-details")
    suspend fun getBookTextPageWithDetails(@Query("chapterInfoId") chapterInfoId: Long?, @Query("bookInfoId") bookInfoId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<BookTextInfo>>

    @GET("/app-api/material/book-text-sentence/get")
    suspend fun getBookTextSentence(@Query("id") id: Long): BaseResponse<BookTextSentence>

    @GET("/app-api/material/book-text-info/get-with-details")
    suspend fun getBookTextWithDetails(@Query("id") id: Long): BaseResponse<BookTextInfo>

    @GET("/app-api/material/book-word-example-sentence/page")
    suspend fun getBookWordExampleSentencePage(@Query("bookWordInfoId") bookWordInfoId: Long, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<BookWordExampleSentence>>

    @GET("/app-api/material/book-word-info/get")
    suspend fun getBookWordInfo(@Query("id") id: Long): BaseResponse<BookWordInfo>

    @GET("/app-api/material/book-word-info/get-by-overall-word-id")
    suspend fun getBookWordInfoByOverallWordId(@Query("id") id: Long): BaseResponse<BookWordInfo>

    @GET("/app-api/material/book-word-info/page")
    suspend fun getBookWordPage(@Query("chapterInfoId") chapterInfoId: Long?, @Query("bookInfoId") bookInfoId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int, @Query("showOrder") showOrder: String): BaseResponse<PageResponse<BookWordInfo>>

    @GET("/app-api/material/book-word-info/page-with-question-id")
    suspend fun getBookWordPageWithQuestionId(@Query("chapterInfoId") chapterInfoId: Long?, @Query("exercisesType") exercisesType: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<BookWordInfo>>

    @GET("/app-api/material/book-word-info/page-with-situation-word")
    suspend fun getBookWordPageWithSituationWord(@Query("chapterInfoId") chapterInfoId: Long?, @Query("bookInfoId") bookInfoId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int, @Query("showOrder") showOrder: String): BaseResponse<PageResponse<BookWordInfo>>

    @GET("/app-api/material/exercises-special-catalogue/page")
    suspend fun getExercisesSpecialCatalogue(@Query("exercisesSpecialType") exercisesSpecialType: String, @Query("areaId") areaId: Long?, @Query("bookInfoId") bookInfoId: Long?, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int, @Query("exercisesSpecialClass") exercisesSpecialClass: String): BaseResponse<PageResponse<ExercisesSpecialCatalogue>>

    @GET("/app-api/material/exercises-special-catalogue-details/page")
    suspend fun getExercisesSpecialCatalogueDetail(@Query("specialCatalogueId") specialCatalogueId: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int, @Query("bookInfoId") bookInfoId: Long?, @Query("areaId") areaId: Long?, @Query("grade") grade: String, @Query("semester") semester: String): BaseResponse<PageResponse<ExercisesSpecialCatalogueDetail>>

    @GET("/app-api/material/current-text-read-catalogue/page-order-by-shownum")
    suspend fun getReadingArticleCataloguePage(@Query("currentTextReadInfoId") currentTextReadInfoId: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<CurrentTextReadCatalogue>>

    @GET("/app-api/material/current-text-read-article/page-by-paragraph-order")
    suspend fun getReadingArticleContentByParagraph(@Query("currentTextReadCatalogueId") currentTextReadCatalogueId: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<CurrentTextReadArticle>>

    @GET("/app-api/material/reading-article/get")
    suspend fun getReadingArticleDetail(@Query("id") id: Long): BaseResponse<ReadingArticle>

    @GET("/app-api/material/reading-article/page")
    suspend fun getReadingArticlePage(@Query("category") category: String, @Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<ReadingArticle>>

    @GET("/app-api/material/current-text-read-info/page-difficulty")
    suspend fun getReadingArticlePageDifficulty(@Query("selectValue") selectValue: String, @Query("isNewset") isNewset: Int): BaseResponse<PageResponse<CurrentTextReadInfo>>

    @GET("/app-api/material/current-text-read-info/page-grade")
    suspend fun getReadingArticlePageGrade(@Query("selectValue") selectValue: String, @Query("isNewset") isNewset: Int): BaseResponse<PageResponse<CurrentTextReadInfo>>

    @GET("/app-api/material/current-text-read-info/page-recommend")
    suspend fun getReadingArticlePageRecommend(@Query("isNewest") isNewest: Int): BaseResponse<PageResponse<CurrentTextReadInfo>>

    @GET("/app-api/study/situation-book/getByStudentIdAndBookId")
    suspend fun getStudySituationBook(@Query("bookId") bookId: Long): BaseResponse<StudySituationBook>

    @GET("/app-api/material/unit-info/list")
    suspend fun getUnitList(@Query("bookInfoId") bookInfoId: Long): BaseResponse<List<UnitInfo>>
}

interface OrderApiService {
    @GET("/app-api/trade/order/get-detail")
    suspend fun getOrderDetail(@Query("id") id: Long): BaseResponse<OrderDetail>

    @GET("/app-api/trade/order/page")
    suspend fun getOrderPage(@Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<PageResponse<OrderInfo>>
}

interface BuyApiService {
    @GET("/app-api/material/product-spu/page")
    suspend fun getProductSpuPage(@Query("pageNo") pageNo: Int, @Query("pageSize") pageSize: Int): BaseResponse<ProductSpuPage>

    @GET("/app-api/material/product-spu/get-by-student-id")
    suspend fun getProductSpuShareInfo(@Query("studentId") studentId: Long?, @Query("id") id: Long): BaseResponse<ProductSpuShareInfo>
}
