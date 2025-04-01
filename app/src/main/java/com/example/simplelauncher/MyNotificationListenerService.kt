package com.example.simplelauncher

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.os.IBinder
import android.util.Log
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.activity.result.launch
import androidx.compose.foundation.layout.add
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.key.Key
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.contains
import kotlin.collections.remove

import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import androidx.core.content.ContextCompat.getSystemService
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.drawable.toDrawable

data class NotificationData(
    val key: String,
    val packageName: String,
    val title: CharSequence?,
    val text: CharSequence?,
    val subText: CharSequence?,
    val contentIntent: PendingIntent?
)

data class MediaData (
    val packageName: String,
    var title: String?,
    var artist: String?,
    var isPlaying: Boolean,
    var albumArt: Drawable? = null
)

class MyNotificationListener : NotificationListenerService() {
    companion object {
        const val TAG = "SimpleLauncherNotificationListener"
        val currentNotifications = mutableStateListOf<NotificationData>()
        var currentMedia = mutableStateListOf<MediaData>()
        var currentPlayback = mutableStateOf<MediaData>(MediaData("", null, null, false))
        var mediaSessionManager: MediaSessionManager? = null

        fun isEnabled(context: Context): Boolean {
            val listeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return listeners?.contains(
                ComponentName(context, MyNotificationListener::class.java).flattenToString()
            ) ?: false
        }

        fun openNotificationAccessSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            context.startActivity(intent)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val mediaControllers = mutableMapOf<String, MediaController>()
    private val mediaCallbacks = mutableMapOf<String, MediaController.Callback>()
    private val mediaListener = MediaSessionManager
        .OnActiveSessionsChangedListener { controllers ->
            CoroutineScope(Dispatchers.Main).launch {
                val newMediaPackages = mutableSetOf<String>()
                val updatedMediaList = mutableListOf<MediaData>()
                if (controllers != null) {
                    for (controller in controllers) {
                        newMediaPackages.add(controller.packageName)
                        if (!mediaControllers.containsKey(controller.packageName)) {
                            // new media session
                            val mediaCallback = object : MediaController.Callback() {
                                override fun onMetadataChanged(metadata: MediaMetadata?) {
                                    val packageName = controller.packageName
                                    val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                                    val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                                    var albumArt: Drawable? = null
                                    val bitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                                    if (bitmap != null) {
                                        albumArt = bitmap.toDrawable(resources)
                                    }
                                    val mediaCheck = currentMedia.find{it.packageName == controller.packageName }
                                    if (packageName == currentPlayback.value.packageName) {
                                        currentPlayback.value = MediaData(packageName, title, artist, currentPlayback.value.isPlaying, albumArt)
                                    }
//                                    if (mediaCheck != null) {
//                                        mediaCheck.title = title
//                                        mediaCheck.artist = artist
//                                    }
//                                    updateMediaData(packageName, title, artist, newMediaList = updatedMediaList)
//                                    currentMedia.add(MediaData(packageName, title, artist, true))
                                    Log.d(TAG, "onMetadataChanged: $packageName, $title, $artist")
                                }

                                override fun onPlaybackStateChanged(state: PlaybackState?) {
                                    val packageName = controller.packageName
                                    val isPlaying = state?.state == PlaybackState.STATE_PLAYING
                                    val mediaCheck = currentMedia.find{it.packageName == controller.packageName }
                                    if(isPlaying) {
                                        currentPlayback.value = MediaData(packageName, currentPlayback.value.title, currentPlayback.value.artist, isPlaying, currentPlayback.value.albumArt)
                                    } else if (currentPlayback.value.packageName == packageName) {
                                        currentPlayback.value = MediaData(packageName, currentPlayback.value.title, currentPlayback.value.artist, isPlaying, currentPlayback.value.albumArt)
                                    }
//                                    if (mediaCheck != null) {
//                                        mediaCheck.isPlaying = isPlaying
//                                    }
//                                    updateMediaData(packageName, null, null, isPlaying, updatedMediaList)
                                    Log.d(TAG, "onPlaybackStateChanged: $packageName, isPlaying = $isPlaying")

                                }
                            }
                            controller.registerCallback(mediaCallback, handler)
                            mediaControllers[controller.packageName] = controller // Track controller
                            mediaCallbacks[controller.packageName] = mediaCallback // Track callback
                            // Update media info immediately after callback registration
                            val metadata = controller.metadata
                            val playbackState = controller.playbackState
                            val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
                            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                            var albumArt: Drawable? = null
                            val bitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                            if (bitmap != null) {
                                albumArt = bitmap.toDrawable(resources)
                            }
                            val mediaData = MediaData(controller.packageName, title, artist, isPlaying, albumArt)
                            val mediaCheck = currentMedia.find{it.packageName == controller.packageName }
                            if(isPlaying) {
                                currentPlayback.value = mediaData
                            }
//                            if (mediaCheck != null) {
//                                mediaCheck.title = title
//                                mediaCheck.artist = artist
//                                mediaCheck.isPlaying = isPlaying
//                            } else {
//                                currentMedia.add(mediaData)
//                            }
//                            updatedMediaList.add(mediaData)
                            Log.d(TAG, "Media changed: ${controller.packageName}, $title, $artist, $isPlaying")
                        } else {
                            val metadata = controller.metadata
                            val playbackState = controller.playbackState
                            val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
                            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                            val mediaData = MediaData(controller.packageName, title, artist, isPlaying)
                            Log.d(TAG, "Media changed Not List: ${controller.packageName}, $title, $artist, $isPlaying")
                            updatedMediaList.add(mediaData)
                        }
                    }
                }
                mediaControllers.keys.minus(newMediaPackages).forEach { oldPackageName ->
                    //Removed a session
                    mediaControllers[oldPackageName]?.let { mediaController ->
                        mediaCallbacks[oldPackageName]?.let { mediaCallback -> mediaController.unregisterCallback(mediaCallback) }
                         // Unregister old callback
                    }
                    mediaControllers.remove(oldPackageName) // Remove controller from map
                    currentMedia.clear()
                    currentMedia.addAll(updatedMediaList)
                }
            }
        }
    private fun updateMediaData(packageName: String, title: String? = null, artist: String? = null, isPlaying: Boolean? = null, newMediaList: MutableList<MediaData>) {
        val mediaData = newMediaList.firstOrNull { it.packageName == packageName }
        if (mediaData == null) {
            val oldMedia = currentMedia.firstOrNull { it.packageName == packageName }
            if(oldMedia != null)
                newMediaList.add(oldMedia)
        }

        val mediaDataToUpdate = newMediaList.firstOrNull { it.packageName == packageName }
        mediaDataToUpdate?.let {
            if (title != null) {
                it.title = title
            }
            if (artist != null) {
                it.artist = artist
            }
            if (isPlaying != null) {
                it.isPlaying = isPlaying
            }
        }
        val updatedMedia = currentMedia.toMutableList()
        currentMedia.clear()
        currentMedia.addAll(updatedMedia)
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager =
            getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSessionManager?.addOnActiveSessionsChangedListener(
            mediaListener,
            ComponentName(this, MyNotificationListener::class.java),
            handler
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionManager?.removeOnActiveSessionsChangedListener(mediaListener)
        mediaControllers.forEach { (packageName, controller) ->
            mediaCallbacks[packageName]?.let { mediaCallback -> controller.unregisterCallback(mediaCallback) }
//            controller.unregisterCallback(controller.callback)
        }
        mediaControllers.clear()
        mediaCallbacks.clear()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val key = sbn.key
        val notification: Notification = sbn.notification
        val packageName = sbn.packageName
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        val contentIntent = sbn.notification.contentIntent
        val mediaSession = Notification.EXTRA_MEDIA_SESSION
        val mediaTransport = Notification.CATEGORY_TRANSPORT
        val category = notification.category
        Log.d(TAG, "onNotificationPosted: $packageName, $title, $text, $subText, $category")
        val notificationData = NotificationData(key, packageName, title, text, subText, contentIntent)
        if (category != mediaTransport && text != null && title != null) {
            CoroutineScope(Dispatchers.Main).launch {
//                Log.d(TAG, "onNotificationPosted: $packageName, $title, $text, $subText, $mediaTransport")
                currentNotifications.add(notificationData)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        val key = sbn.key
        val notification: Notification = sbn.notification
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        CoroutineScope(Dispatchers.Main).launch {
            Log.d(TAG, "onNotificationRemoved: $packageName, $title, $text, $subText")
            currentNotifications.removeAll {it.key == key}
        }
    }
}