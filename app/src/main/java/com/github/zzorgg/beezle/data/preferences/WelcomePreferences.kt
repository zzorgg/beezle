package com.github.zzorgg.beezle.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WelcomePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "welcome_prefs",
        Context.MODE_PRIVATE
    )

    fun hasShownWelcomeAnimation(): Boolean {
        return prefs.getBoolean(KEY_WELCOME_SHOWN, false)
    }

    fun markWelcomeAnimationShown() {
        prefs.edit().putBoolean(KEY_WELCOME_SHOWN, true).apply()
    }

    fun resetWelcomeAnimation() {
        prefs.edit().putBoolean(KEY_WELCOME_SHOWN, false).apply()
    }

    companion object {
        private const val KEY_WELCOME_SHOWN = "welcome_animation_shown"
    }
}

