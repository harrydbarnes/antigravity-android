package com.google.antigravity.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.antigravity.mobile.data.model.PlanTier
import com.google.antigravity.mobile.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    private val _isBiometricLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_LOCK, false))
    val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    private val _activeInstanceId = MutableStateFlow(prefs.getString(KEY_ACTIVE_INSTANCE_ID, null))
    val activeInstanceId: StateFlow<String?> = _activeInstanceId.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _activeProjectId = MutableStateFlow(prefs.getString(KEY_ACTIVE_PROJECT_ID, null))
    val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

    private fun loadUser(): UserAccount {
        // Browser authentication is isolated from this local preferences store.
        // Ignore legacy demo identities, including previously persisted ones.
        return UserAccount(isSignedIn = false)
    }

    fun saveUser(user: UserAccount) {
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, user.isSignedIn)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.displayName)
            .putString(KEY_PLAN_TIER, user.planTier.name)
            .apply()
        _currentUser.value = user
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        _isBiometricLockEnabled.value = enabled
    }

    fun setActiveInstanceId(instanceId: String?) {
        prefs.edit().putString(KEY_ACTIVE_INSTANCE_ID, instanceId).apply()
        _activeInstanceId.value = instanceId
    }

    fun setActiveProjectId(projectId: String?) {
        prefs.edit().putString(KEY_ACTIVE_PROJECT_ID, projectId).apply()
        _activeProjectId.value = projectId
    }

    fun setHasCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    fun resetOnboarding() {
        setHasCompletedOnboarding(false)
    }

    fun getSessionCookies(): String? {
        return prefs.getString(KEY_SESSION_COOKIES, null)
    }

    fun saveSessionCookies(cookies: String) {
        prefs.edit().putString(KEY_SESSION_COOKIES, cookies).apply()
    }

    fun signOut() {
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, false)
            .remove(KEY_SESSION_COOKIES)
            .apply()
        _currentUser.value = UserAccount(isSignedIn = false)
    }

    companion object {
        private const val PREFS_NAME = "antigravity_prefs"
        private const val KEY_IS_SIGNED_IN = "key_is_signed_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_PLAN_TIER = "key_plan_tier"
        private const val KEY_BIOMETRIC_LOCK = "key_biometric_lock"
        private const val KEY_ACTIVE_INSTANCE_ID = "key_active_instance_id"
        private const val KEY_ACTIVE_PROJECT_ID = "key_active_project_id"
        private const val KEY_SESSION_COOKIES = "key_session_cookies"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "key_has_completed_onboarding"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
