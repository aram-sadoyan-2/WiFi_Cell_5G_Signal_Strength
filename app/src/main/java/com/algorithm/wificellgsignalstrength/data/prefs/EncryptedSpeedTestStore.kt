package com.algorithm.wificellgsignalstrength.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.speedTestDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "encrypted_speed_test"
)

@Singleton
class EncryptedSpeedTestStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.speedTestDataStore

    suspend fun save(result: SavedSpeedTestResult) {
        val plain = listOf(
            result.downloadMbps.toString(),
            result.uploadMbps.toString(),
            result.pingMs.toString(),
            result.testedAtEpochMs.toString()
        ).joinToString(SEPARATOR)

        val encrypted = runCatching {
            AesGcmCipher.encryptToBase64(plain)
        }.getOrElse { return }

        dataStore.edit { prefs ->
            prefs[KEY_PAYLOAD] = encrypted
        }
    }

    suspend fun load(): SavedSpeedTestResult? {
        val encrypted = dataStore.data
            .map { it[KEY_PAYLOAD] }
            .first()
            ?: return null

        return runCatching {
            val parts = AesGcmCipher.decryptFromBase64(encrypted).split(SEPARATOR)
            if (parts.size != 4) return null
            SavedSpeedTestResult(
                downloadMbps = parts[0].toFloat(),
                uploadMbps = parts[1].toFloat(),
                pingMs = parts[2].toInt(),
                testedAtEpochMs = parts[3].toLong()
            )
        }.getOrNull()
    }

    companion object {
        private val KEY_PAYLOAD = stringPreferencesKey("payload")
        private const val SEPARATOR = "|"
    }
}
