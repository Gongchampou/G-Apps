package com.example.myapplication

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * BACKGROUND PLAYBACK SERVICE
 * This service handles the music playing in the background and lock screen.
 */
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        
        // CYCLE STYLE: We use REPEAT_MODE_ALL to ensure the 'Next' button 
        // is always visible on the lock screen, even on the last song.
        player.repeatMode = Player.REPEAT_MODE_ALL
        
        // AUTO-LOOP: When a song finishes naturally, we manually jump back to 
        // loop it. If the user clicks "Next", this doesn't trigger, allowing manual cycling.
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    val currentIndex = player.currentMediaItemIndex
                    val prevIndex = if (currentIndex == 0) player.mediaItemCount - 1 else currentIndex - 1
                    player.seekTo(prevIndex, 0)
                    player.play()
                }
            }
        })

        // SESSION ACTIVITY: This makes it so clicking the notification opens the app.
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
