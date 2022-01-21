package com.example.quizapp.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.quizapp.extensions.dataStore
import com.example.quizapp.extensions.dataflow
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.datastore.datawrappers.*
import com.example.quizapp.utils.EncryptionUtil.decrypt
import com.example.quizapp.utils.EncryptionUtil.encrypt
import io.ktor.util.date.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PreferencesRepository @Inject constructor(
    context: Context
) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object PreferencesKeys {
        val LANGUAGE_KEY = stringPreferencesKey("languageKey")
        private val THEME_KEY = stringPreferencesKey("themeKey")
        private val SHUFFLE_TYPE_KEY = stringPreferencesKey("shuffleTypePreference")
        private val SHUFFLE_SEED_KEY = longPreferencesKey("shuffleSeedKey")

        private val MANAGE_USER_ORDER_BY_KEY = stringPreferencesKey("userOrderByKey")
        private val MANAGE_USER_ASCENDING_ORDER_KEY = booleanPreferencesKey("userOrderAscendingKey")

        private val BROWSABLE_ORDER_BY_KEY = stringPreferencesKey("sortByPreferenceKey")
        private val BROWSABLE_ASCENDING_ORDER_KEY = booleanPreferencesKey("browsableAscendingKey")
        private val BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS = stringSetPreferencesKey("browsableQuestionnairesFilterCosIdsKey")
        private val BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS = stringSetPreferencesKey("browsableQuestionnairesFilterFacultyIdsKey")

        private val LOCAL_QUESTIONNAIRE_ORDER_BY_KEY = stringPreferencesKey("localQuestionnaireOrderByKey")
        private val LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY = booleanPreferencesKey("localQuestionnaireAscendingOrderKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS = stringSetPreferencesKey("localQuestionnairesFilterAuthorIdsKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_COS_IDS = stringSetPreferencesKey("localQuestionnairesFilterCosIdsKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS = stringSetPreferencesKey("localQuestionnairesFilterFacultyIdsKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED = booleanPreferencesKey("localQuestionnairesFilterHideCompletedKey")

        private val PREFERRED_COURSE_OF_STUDIES_ID_KEY = stringSetPreferencesKey("preferredCosKey")
        private val PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH = booleanPreferencesKey("preferredCosFprQuestionnaireKey")

        private val JWT_TOKEN_KEY = stringPreferencesKey("jwtTokenKey")
        private val USER_ID_KEY = stringPreferencesKey("userIdKey")
        private val USER_NAME_KEY = stringPreferencesKey("userNameKey")
        private val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
        private val USER_ROLE_KEY = stringPreferencesKey("userRoleKey")
        private val USER_LAST_MODIFIED_TIMESTAMP_KEY = longPreferencesKey("userLastModifiedTimeStampKey")

        const val EMPTY_STRING = ""
        const val UNKNOWN_TIMESTAMP = -1L
        val DEFAULT_ROLE = Role.USER
    }

    private val dataFlow = dataStore.dataflow

    suspend fun clearPreferenceDataOnLogout() {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = EMPTY_STRING
            preferences[USER_PASSWORD_KEY] = EMPTY_STRING
            preferences[USER_ROLE_KEY] = EMPTY_STRING
            preferences[JWT_TOKEN_KEY] = EMPTY_STRING
        }
    }

    suspend fun wipePreferenceData() {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = EMPTY_STRING
            preferences[USER_PASSWORD_KEY] = EMPTY_STRING
            preferences[USER_ROLE_KEY] = EMPTY_STRING
            preferences[JWT_TOKEN_KEY] = EMPTY_STRING
            preferences[LANGUAGE_KEY] = QuizAppLanguage.ENGLISH.name
            preferences[THEME_KEY] = QuizAppTheme.LIGHT.name
            preferences[SHUFFLE_TYPE_KEY] = QuestionnaireShuffleType.NONE.name
            preferences[BROWSABLE_ORDER_BY_KEY] = RemoteQuestionnaireOrderBy.TITLE.name
            preferences[BROWSABLE_ASCENDING_ORDER_KEY] = true
            preferences[MANAGE_USER_ORDER_BY_KEY] = ManageUsersOrderBy.USER_NAME.name
            preferences[MANAGE_USER_ASCENDING_ORDER_KEY] = true
            preferences[LOCAL_QUESTIONNAIRE_ORDER_BY_KEY] = LocalQuestionnaireOrderBy.TITLE.name
            preferences[LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY] = true
            preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] = emptySet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_COS_IDS] = emptySet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS] = emptySet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED] = false
            preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] = emptySet()
            preferences[PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH] = true
        }
    }


    val themeFlow = dataFlow.map { preferences ->
        preferences[THEME_KEY]?.let { QuizAppTheme.valueOf(it) } ?: QuizAppTheme.LIGHT
    }

    suspend fun getTheme(): QuizAppTheme = themeFlow.first()

    suspend fun updateTheme(newTheme: QuizAppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = newTheme.name
        }
    }


    val languageFlow = dataFlow.map { preferences ->
        preferences[LANGUAGE_KEY]?.let { QuizAppLanguage.valueOf(it) } ?: QuizAppLanguage.ENGLISH
    }

    suspend fun getLanguage(): QuizAppLanguage = languageFlow.first()

    suspend fun updateLanguage(quizAppLanguage: QuizAppLanguage) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = quizAppLanguage.name
        }
    }


    val shuffleTypeFlow = dataFlow.map { preferences ->
        preferences[SHUFFLE_TYPE_KEY]?.let { QuestionnaireShuffleType.valueOf(it) } ?: QuestionnaireShuffleType.NONE
    }

    suspend fun getShuffleType(): QuestionnaireShuffleType = shuffleTypeFlow.first()

    suspend fun updateShuffleType(shuffleType: QuestionnaireShuffleType) {
        dataStore.edit { preferences ->
            preferences[SHUFFLE_TYPE_KEY] = shuffleType.name
        }
    }


    val shuffleSeedFlow = dataFlow.map { preferences ->
        preferences[SHUFFLE_SEED_KEY] ?: getTimeMillis()
    }

    suspend fun getShuffleSeed() = shuffleSeedFlow.first()

    suspend fun updateShuffleSeed(newSeed: Long = Random.nextLong(Long.MAX_VALUE)) {
        dataStore.edit { preferences ->
            preferences[SHUFFLE_SEED_KEY] = newSeed
        }
    }


    //BROWSE QUESTIONNAIRE FILTERS
    val browsableOrderByFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_ORDER_BY_KEY]?.let { RemoteQuestionnaireOrderBy.valueOf(it) } ?: RemoteQuestionnaireOrderBy.TITLE
    }

    suspend fun getBrowsableOrderBy() = browsableOrderByFlow.first()


    val browsableAscendingOrderFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_ASCENDING_ORDER_KEY] ?: true
    }

    suspend fun getBrowsableAscendingOrder() = browsableAscendingOrderFlow.first()


    val browsableCosIdsFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS] ?: preferredCourseOfStudiesIdFlow.first()
    }

    suspend fun getBrowsableCosIds() = browsableCosIdsFlow.first()


    val browsableFacultyIdsFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS] ?: emptySet()
    }

    suspend fun getBrowsableFacultyIds() = browsableFacultyIdsFlow.first()


    suspend fun updateRemoteFilters(
        remoteQuestionnaireOrderBy: RemoteQuestionnaireOrderBy,
        ascending: Boolean,
        cosIds: Collection<String>,
        facultyIds: Collection<String>,

        ) {
        dataStore.edit { preferences ->
            preferences[BROWSABLE_ORDER_BY_KEY] = remoteQuestionnaireOrderBy.name
            preferences[BROWSABLE_ASCENDING_ORDER_KEY] = ascending
            preferences[BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS] = cosIds.toSet()
            preferences[BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS] = facultyIds.toSet()
        }
    }


    //LOCAL QUESTIONNAIRE FILTERS
    val localOrderByFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_ORDER_BY_KEY]?.let { LocalQuestionnaireOrderBy.valueOf(it) } ?: LocalQuestionnaireOrderBy.TITLE
    }

    suspend fun getLocalQuestionnaireOrderBy() = localOrderByFlow.first()

    val localAscendingOrderFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY] ?: true
    }

    suspend fun getLocalAscendingOrder() = localAscendingOrderFlow.first()

    val localFilteredAuthorIdsFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] ?: emptySet()
    }

    suspend fun getLocalFilteredAuthorIds() = localFilteredAuthorIdsFlow.first()

    val localFilterHideCompletedFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED] ?: false
    }

    suspend fun updateLocalFilteredAuthorIds(authorIds: Collection<String>) {
        dataStore.edit { preferences ->
            preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] = authorIds.toSet()
        }
    }

    suspend fun getLocalFilterHideCompleted() = localFilterHideCompletedFlow.first()

    val localFilteredCosIdsFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_COS_IDS] ?: emptySet()
    }

    suspend fun getLocalFilteredCosIds() = localFilteredCosIdsFlow.first()

    val localFilteredFacultyIdsFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS] ?: emptySet()
    }

    suspend fun getLocalFilteredFacultyIds() = localFilteredFacultyIdsFlow.first()


    suspend fun updateLocalFilters(
        localOrderBy: LocalQuestionnaireOrderBy,
        ascending: Boolean,
        authorIds: Collection<String>,
        cosIds: Collection<String>,
        facultyIds: Collection<String>,
        hideCompleted: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[LOCAL_QUESTIONNAIRE_ORDER_BY_KEY] = localOrderBy.name
            preferences[LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY] = ascending
            preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] = authorIds.toSet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_COS_IDS] = cosIds.toSet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS] = facultyIds.toSet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED] = hideCompleted
        }
    }


    //USER FILTERS
    val manageUsersOrderByFlow = dataFlow.map { preferences ->
        preferences[MANAGE_USER_ORDER_BY_KEY]?.let { ManageUsersOrderBy.valueOf(it) } ?: ManageUsersOrderBy.USER_NAME
    }

    suspend fun getManageUsersOrderBy() = manageUsersOrderByFlow.first()

    suspend fun updateManageUsersOrderBy(manageUsersOrderBy: ManageUsersOrderBy) {
        dataStore.edit { preferences ->
            preferences[MANAGE_USER_ORDER_BY_KEY] = manageUsersOrderBy.name
        }
    }


    val manageUsersAscendingOrderFlow = dataFlow.map { preferences ->
        preferences[MANAGE_USER_ASCENDING_ORDER_KEY] ?: true
    }

    suspend fun getManageUsersAscendingOrder() = manageUsersAscendingOrderFlow.first()

    suspend fun updateManageUsersAscendingOrder(ascending: Boolean) {
        dataStore.edit { preferences ->
            preferences[MANAGE_USER_ASCENDING_ORDER_KEY] = ascending
        }
    }


    val preferredCourseOfStudiesIdFlow = dataFlow.map { preferences ->
        preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] ?: emptySet()
    }

    suspend fun getPreferredCourseOfStudiesId() = preferredCourseOfStudiesIdFlow.first()

    suspend fun updatePreferredCourseOfStudiesIds(courseOfStudiesIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] = courseOfStudiesIds.toSet()
        }
    }


    val usePreferredCourseOfStudiesForSearchFlow = dataFlow.map { preferences ->
        preferences[PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH] ?: false
    }

    suspend fun usePreferredCourseOfStudiesForSearch() = usePreferredCourseOfStudiesForSearchFlow.first()

    suspend fun updateUsePreferredCosForSearch(newValue: Boolean) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH] = newValue
        }
    }


    private val jwtTokenFlow = dataFlow.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    suspend fun getJwtToken() = jwtTokenFlow.first()?.let {
        if (it.isEmpty()) null else it
    }

    suspend fun updateJwtToken(token: String?) {
        dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token ?: EMPTY_STRING
        }
    }


    val userIdFlow = dataFlow.map { preferences ->
        preferences[USER_ID_KEY]?.let {
            if (it.isEmpty()) EMPTY_STRING else it.decrypt()
        } ?: EMPTY_STRING
    }

    val userNameFlow = dataFlow.map { preferences ->
        preferences[USER_NAME_KEY]?.let {
            if (it.isEmpty()) EMPTY_STRING else it.decrypt()
        } ?: EMPTY_STRING
    }

    val userPasswordFlow = dataFlow.map { preferences ->
        preferences[USER_PASSWORD_KEY]?.let {
            if (it.isEmpty()) EMPTY_STRING else it.decrypt()
        } ?: EMPTY_STRING
    }

    val userRoleFlow = dataFlow.map { preferences ->
        preferences[USER_ROLE_KEY]?.let {
            if (it.isEmpty()) DEFAULT_ROLE else Role.valueOf(it.decrypt())
        } ?: DEFAULT_ROLE
    }

    val userLastModifiedTimestampFlow = dataFlow.map { preferences ->
        preferences[USER_LAST_MODIFIED_TIMESTAMP_KEY] ?: UNKNOWN_TIMESTAMP
    }

    val userFlow = combine(
        userIdFlow,
        userNameFlow,
        userPasswordFlow,
        userRoleFlow,
        userLastModifiedTimestampFlow
    ) { id, name, password, role, timestamp ->
        User(
            id = id,
            userName = name,
            password = password,
            role = role,
            lastModifiedTimestamp = timestamp
        )
    }

    suspend fun getUserId() = userIdFlow.first()

    suspend fun getUserName() = userNameFlow.first()

    suspend fun getUserPassword() = userPasswordFlow.first()

    suspend fun getOwnAuthorInfo() = userFlow.first().asAuthorInfo

    suspend fun isUserLoggedIn() = userFlow.first().isNotEmpty



    suspend fun updateUserCredentials(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = if (user.id.isEmpty()) EMPTY_STRING else user.id.encrypt()
            preferences[USER_NAME_KEY] = if (user.userName.isEmpty()) EMPTY_STRING else user.userName.encrypt()
            preferences[USER_PASSWORD_KEY] = if (user.password.isEmpty()) EMPTY_STRING else user.password.encrypt()
            preferences[USER_ROLE_KEY] = user.role.name.encrypt()
            preferences[USER_LAST_MODIFIED_TIMESTAMP_KEY] = user.lastModifiedTimestamp
        }
    }

    suspend fun updateUserRole(newRole: Role) {
        dataStore.edit { preferences ->
            preferences[USER_ROLE_KEY] = newRole.name.encrypt()
        }
    }

    suspend fun updateUserPassword(newPassword: String) {
        dataStore.edit { preferences ->
            preferences[USER_PASSWORD_KEY] = newPassword.encrypt()
        }
    }
}