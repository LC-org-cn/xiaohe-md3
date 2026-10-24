package com.xiaohe.english.network.model

data class BaseResponse<T>(
    val code: Int = 0,
    val data: T? = null,
    val msg: String? = null,
)

data class PageResponse<T>(
    val list: List<T>? = null,
    val pageNo: Int = 0,
    val pageSize: Int = 0,
    val total: Long = 0,
)

class ApiException(val code: Int, message: String) : Exception(message)

data class TokenInfo(
    val accessToken: String? = null,
    val expiresTime: Long = 0,
    val refreshToken: String? = null,
)

data class LoginRequest(val username: String, val password: String)
data class PhoneLoginRequest(val mobile: String, val code: String)

data class TeacherInfo(val id: String? = null, val name: String? = null)
data class ClassInfo(
    val className: String? = null,
    val headTeacher: TeacherInfo? = null,
    val id: String? = null,
)

data class AppStudentInfo(
    val areaId: String? = null, val areaName: String? = null, val avatar: String? = null,
    val cityId: String? = null, val classId: String? = null, val classList: List<ClassInfo>? = null,
    val className: String? = null, val countyId: String? = null,
    val currentBookChapterId: String? = null, val currentBookChapterName: String? = null,
    val currentBookInfoId: String? = null, val currentBookInfoName: String? = null,
    val currentBookRevision: Int = 0, val currentEducationalSystemType: String? = null,
    val currentEducationalSystemTypeName: String? = null, val currentGrade: String? = null,
    val currentGradeName: String? = null, val currentMaterialType: String? = null,
    val currentMaterialTypeName: String? = null, val currentSchoolId: String? = null,
    val currentSchoolName: String? = null, val currentSemester: String? = null,
    val currentSemesterName: String? = null, val experienceEndTime: String? = null,
    val gender: String? = null, val genderName: String? = null, val gradeId: String? = null,
    val gradeName: String? = null, val id: String? = null, val isBindToWechat: String? = null,
    val isExperience: String? = null, val isVip: String? = null, val memberId: String? = null,
    val name: String? = null, val nickname: String? = null, val provinceId: String? = null,
    val schoolId: String? = null, val schoolName: String? = null, val username: String? = null,
    val vipEndTime: String? = null,
)

data class StudentInfo(
    val areaId: String? = null, val areaName: String? = null, val avatar: String? = null,
    val cityId: String? = null, val classId: String? = null, val className: String? = null,
    val countyId: String? = null, val currentBookInfoId: String? = null,
    val currentBookInfoName: String? = null, val currentGrade: String? = null,
    val currentGradeName: String? = null, val currentSchoolId: String? = null,
    val currentSchoolName: String? = null, val currentSemester: String? = null,
    val currentSemesterName: String? = null, val gender: String? = null,
    val genderName: String? = null, val gradeId: String? = null, val gradeName: String? = null,
    val id: String? = null, val isVip: String? = null, val memberId: String? = null,
    val name: String? = null, val nickname: String? = null, val provinceId: String? = null,
    val schoolId: String? = null, val schoolName: String? = null, val username: String? = null,
    val vipEndTime: String? = null,
)

data class AppVersionInfo(
    val createTime: String? = null, val description: String? = null, val id: String? = null,
    val url: String? = null, val versionName: String? = null, val versionNum: Int = 0,
)

data class ChapterTreeItem(
    val bookInfoId: String? = null, val chapterNumber: Int = 0, val chapterOrder: Int = 0,
    val chapterTitle: String? = null, val chapterType: String? = null,
    val chapterWordExamNumAll: Int = 0, val chapterWordNum: Int = 0, val chapterWordNumAll: Int = 0,
    val createTime: String? = null, val id: String? = null,
    val isLeafageChapter: String? = null, val isRootChapter: String? = null,
)

data class ExaminationItem(
    val areaId: String? = null, val bookInfoId: String? = null, val createTime: String? = null,
    val exercisesCopiesNum: Int = 0, val exercisesSpecialClass: String? = null,
    val exercisesSpecialName: String? = null, val exercisesSpecialType: String? = null,
    val id: String? = null,
)

data class ExaminationPageData(
    val list: List<ExaminationItem>? = null, val pageNum: Int = 0,
    val pageSize: Int = 0, val pages: Int = 0, val total: Int = 0,
)

data class NotifyMessage(
    val content: String? = null, val createTime: Long? = null, val id: Long = 0,
    val readStatus: Boolean = false, val templateContent: String? = null,
    val templateNickname: String? = null, val templateTitle: String? = null,
    val templateType: Int? = null, val title: String? = null, val type: Int = 0,
)

data class NotifyPageData(
    val list: List<Any>? = null, val pageNum: Int = 0, val pageSize: Int = 0,
    val pages: Int = 0, val total: Int = 0,
)

data class IndexDataResponse(
    val appNotifyMessage: NotifyPageData? = null,
    val appStudentInfo: AppStudentInfo? = null,
    val appTypeRes: AppVersionInfo? = null,
    val chapterTreeByTypeData: List<ChapterTreeItem>? = null,
    val examinationSpecialData: ExaminationPageData? = null,
    val examinationZoneData: ExaminationPageData? = null,
    val homeworkTotal: Int = 0,
    val unreadMessage: Boolean = false,
)
