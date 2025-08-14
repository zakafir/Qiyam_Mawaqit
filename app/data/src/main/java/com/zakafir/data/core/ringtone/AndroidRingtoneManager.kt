package com.zakafir.data.core.ringtone

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.zakafir.data.core.domain.ringtone.NameAndUri
import com.zakafir.data.core.domain.ringtone.RingtoneManager as MyRingtoneManager
import com.zakafir.data.core.domain.ringtone.SILENT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidRingtoneManager(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): MyRingtoneManager {

    private var mediaPlayer: MediaPlayer? = null

    override suspend fun getAvailableRingtones(): List<NameAndUri> = withContext(ioDispatcher) {
        val ringtoneManager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_ALARM)
        }

        val defaultRingtoneUri = RingtoneManager
            .getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            .buildUpon()
            .clearQuery()
            .build()
        var defaultRingtoneName = ""

        val cursor = ringtoneManager.cursor

        val ringtones = mutableListOf<NameAndUri>()
        ringtones.add(Pair("Silent", SILENT))

        while (cursor.moveToNext()) {
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
            val fullUri = "$uri/$id"

            if (fullUri != defaultRingtoneUri.toString()) {
                ringtones.add(Pair(title, fullUri))
            } else {
                defaultRingtoneName = title
            }
        }

        if (ringtones.size >= 2) {
            ringtones.add(1, Pair("Default (${defaultRingtoneName})", defaultRingtoneUri.toString()))
        }

        return@withContext ringtones
    }

    override fun play(uri: String, isLooping: Boolean, volume: Float) {
        val fullUri: Uri = try {
            if (uri == SILENT) {
                null
            } else {
                Uri.parse(uri)
            }
        } catch (e: Exception) {
            null
        } ?: return

        if (isPlaying()) {
            stop()
        }

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_ALARM)
            setDataSource(context, fullUri)
            setVolume(volume, volume)
            prepare()
            start()
            this.isLooping = isLooping
        }
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun isPlaying(): Boolean {
        return try {
             mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            stop()
            false
        }
    }
}