package com.example.sabi.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class SessionPreferences(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val EMAIL_KEY = stringPreferencesKey("email")

    val email: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[EMAIL_KEY]
        }

    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
