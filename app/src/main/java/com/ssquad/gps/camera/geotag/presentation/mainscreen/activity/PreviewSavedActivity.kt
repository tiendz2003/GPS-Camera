package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.util.Util
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.Photo
import com.ssquad.gps.camera.geotag.presentation.hometab.dialog.InfoBottomSheet
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityPreviewSavedBinding
import com.ssquad.gps.camera.geotag.databinding.InforImgDialogBinding
import com.ssquad.gps.camera.geotag.utils.formatCapturedTime
import com.ssquad.gps.camera.geotag.utils.formatDuration
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.kbToMb
import com.ssquad.gps.camera.geotag.utils.loadImageIcon
import com.ssquad.gps.camera.geotag.utils.parcelable
import com.ssquad.gps.camera.geotag.utils.visible
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreviewSavedActivity : BaseActivity<ActivityPreviewSavedBinding>(ActivityPreviewSavedBinding::inflate) {

    private lateinit var photo: Photo
    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var updateSeekBarRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bottomSheet: InfoBottomSheet

    companion object {
        private const val EXTRA_PHOTO = "extra_photo"
        private const val DELETE_REQUEST_CODE = 101

        fun getIntent(context: Context, photo: Photo): Intent {
            return Intent(context, PreviewSavedActivity::class.java).apply {
                putExtra(EXTRA_PHOTO, photo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            playbackPosition = it.getLong("playback_position", 0L)
            playWhenReady = it.getBoolean("play_when_ready", true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let {
            outState.putLong("playback_position", it.currentPosition)
            outState.putBoolean("play_when_ready", it.playWhenReady)
        }
    }

    override fun initData() {
        intent.parcelable<Photo>(EXTRA_PHOTO)?.let {
            photo = it
        } ?: run {
            finish()
        }
    }

    override fun initView() {
        with(binding) {
            txtVideoTitle.apply {
                isSelected = true
                ellipsize = TextUtils.TruncateAt.MARQUEE
                marqueeRepeatLimit = -1
                isSingleLine = true
                text = photo.name
            }

            if (photo.isVideo) {
                controlsContainer.visible()
                playerView.visible()
                imageView.gone()
            } else {
                controlsContainer.gone()
                playerView.gone()
                imageView.visible()
                imageView.loadImageIcon(photo.path)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initActionView() {
        with(binding) {
            btnPlayPause.setOnClickListener { togglePlayPause() }
            btnForward.setOnClickListener { seekBy(5000L) }
            btnRewind.setOnClickListener { seekBy(-5000L) }
            btnBack.setOnClickListener { finish() }
            btnMore.setOnClickListener { showBottomSheet() }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        player?.seekTo(progress.toLong())
                        binding.txtCurrentTime.text = progress.formatDuration()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopSeekBar()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (player?.isPlaying == true) {
                        updateSeekBar()
                    }
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23 && photo.isVideo) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 && photo.isVideo) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        if (!photo.isVideo) return

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            exoPlayer.setSeekParameters(SeekParameters.CLOSEST_SYNC)
            binding.playerView.player = exoPlayer

            val mediaItem = MediaItem.fromUri(photo.path)
            exoPlayer.setMediaItem(mediaItem)

            exoPlayer.playWhenReady = playWhenReady
            exoPlayer.seekTo(playbackPosition)
            exoPlayer.prepare()

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            binding.seekBar.max = exoPlayer.duration.toInt()
                            binding.txtDuration.text = exoPlayer.duration.formatDuration()
                            updateSeekBar()
                        }
                        Player.STATE_ENDED -> {
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
                            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                            binding.seekBar.progress = 0
                            binding.txtCurrentTime.text = 0.formatDuration()
                            stopSeekBar()
                        }
                        else -> {}
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    binding.btnPlayPause.setImageResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                    if (isPlaying) {
                        updateSeekBar()
                    } else {
                        stopSeekBar()
                    }
                }
            })
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
        stopSeekBar()
    }

    private fun togglePlayPause() {
        player?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        }
    }

    private fun seekBy(millis: Long) {
        player?.let { exoPlayer ->
            val newPosition = exoPlayer.currentPosition + millis
            exoPlayer.seekTo(newPosition.coerceIn(0, exoPlayer.duration))
            updateSeekBar()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showBottomSheet() {
        bottomSheet = InfoBottomSheet(
            onShareClick = {
                shareFileVideo(this@PreviewSavedActivity, photo.path, photo.isVideo)
            },
            onInfoClick = {
                setupInfoDialog(photo)
            },
            onDeleteClick = {
                deletePhoto()
            }
        )
        bottomSheet.show(supportFragmentManager, "InfoBottomSheet")
    }

    private fun updateSeekBar() {
        player?.let { exoPlayer ->
            binding.seekBar.progress = exoPlayer.currentPosition.toInt()
            binding.txtCurrentTime.text = exoPlayer.currentPosition.formatDuration()

            updateSeekBarRunnable?.let { handler.removeCallbacks(it) }
            updateSeekBarRunnable = Runnable {
                if (exoPlayer.isPlaying) {
                    updateSeekBar()
                }
            }
            handler.postDelayed(updateSeekBarRunnable!!, 300)
        }
    }

    private fun stopSeekBar() {
        updateSeekBarRunnable?.let { handler.removeCallbacks(it) }
        updateSeekBarRunnable = null
    }

    private fun setupInfoDialog(photo: Photo) {
        val infoDialog = Dialog(this)
        val dialogBinding = InforImgDialogBinding.inflate(layoutInflater)
        infoDialog.setContentView(dialogBinding.root)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.format(Date(photo.dateAdded * 1000))
        with(dialogBinding) {
            tvName.apply {
                isSelected = true
                ellipsize = TextUtils.TruncateAt.MARQUEE
                marqueeRepeatLimit = -1
                isSingleLine = true
                text = photo.name
            }

            tvPath.apply {
                isSelected = true
                ellipsize = TextUtils.TruncateAt.MARQUEE
                marqueeRepeatLimit = -1
                isSingleLine = true
                text = "${photo.path}"
            }
            tvLocation.text = photo.locationAddress
            tvSize.text = photo.size.kbToMb()
            tvDate.text = photo.dateAdded.formatCapturedTime()
            btnClose.setOnClickListener {
                infoDialog.dismiss()
            }
            btnOk.setOnClickListener {
                infoDialog.dismiss()
            }
        }
        infoDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        infoDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun deletePhoto() {
        val dialogView = layoutInflater.inflate(R.layout.delete_dialog, null)

        val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.btnClCancel)
        val btnDelete = dialogView.findViewById<AppCompatButton>(R.id.btnClStop)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseDialog)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { alertDialog.dismiss() }
        btnClose.setOnClickListener { alertDialog.dismiss() }

        btnDelete.setOnClickListener {
            try {
                val photoUri = photo.path // Đảm bảo đây là content:// URI, không phải file://
                val rowsDeleted = contentResolver.delete(photoUri, null, null)
                if (rowsDeleted > 0) {
                    Toast.makeText(
                        this,
                        getString(R.string.image_deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.failed_to_delete_image),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: SecurityException) {
                val pendingIntent =
                    MediaStore.createDeleteRequest(contentResolver, listOf(photo.path)).intentSender
                startIntentSenderForResult(
                    pendingIntent,
                    DELETE_REQUEST_CODE,
                    null,
                    0, 0, 0
                )
            } catch (e: Exception) {
                Log.e("PreviewSavedActivity", "Delete error: ${e.message}")
                Toast.makeText(this, getString(R.string.failed_to_delete_image), Toast.LENGTH_SHORT)
                    .show()
            }
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DELETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    getString(R.string.image_deleted_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.failed_to_delete_image), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun shareFileVideo(context: Context, uri: Uri, isVideo: Boolean) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (isVideo) "video/*" else "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.getString(R.string.app_name)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekBar()
        releasePlayer()
    }

}