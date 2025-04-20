package com.example.baseproject.presentation.mainscreen.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.data.models.Photo
import com.example.baseproject.databinding.ActivityPreviewSavedBinding
import com.example.baseproject.databinding.InforImgDialogBinding
import com.example.baseproject.presentation.hometab.dialog.InfoBottomSheet
import com.example.baseproject.presentation.hometab.dialog.SortBottomSheet
import com.example.baseproject.utils.formatCapturedTime
import com.example.baseproject.utils.formatDuration
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.kbToMb
import com.example.baseproject.utils.loadImageIcon
import com.example.baseproject.utils.parcelable
import com.example.baseproject.utils.visible
import java.io.File

class PreviewSavedActivity :
    BaseActivity<ActivityPreviewSavedBinding>(ActivityPreviewSavedBinding::inflate) {
    private lateinit var photo: Photo
    private lateinit var bottomSheet: InfoBottomSheet
    private val handler = Handler(Looper.getMainLooper())
    private var updateSeekBarRunnable: Runnable? = null

    companion object {
        private const val EXTRA_PHOTO = "extra_photo"
        fun getIntent(context: Context, photo: Photo): Intent {
            return Intent(context, PreviewSavedActivity::class.java).apply {
                putExtra(EXTRA_PHOTO, photo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun onPause() {
        super.onPause()
        stopSeekBar()
        binding.videoView.pause()
    }

    override fun initData() {
        intent.parcelable<Photo>(EXTRA_PHOTO)?.let {
            photo = it
        } ?: run {
            finish()
            return
        }
    }

    override fun initView() {

        with(binding) {
            if (photo.isVideo) {
                videoView.visible()
                imageView.gone()
                controlsContainer.visible()
                videoView.setOnPreparedListener { mediaPlayer ->
                    val duration = mediaPlayer.duration
                    seekBar.max = duration
                    txtDuration.text = duration.formatDuration()
                    updateSeekBar()
                    mediaPlayer.isLooping = true
                    videoView.start()
                }
                videoView.setVideoURI(photo.path)
                videoView.setOnCompletionListener {
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                    seekBar.progress = seekBar.max
                    txtCurrentTime.text = txtDuration.text
                    stopSeekBar()
                }
                videoView.setOnErrorListener { _, what, extra ->
                    Log.e("PreviewSavedActivity", "Video error: what=$what, extra=$extra")
                    Toast.makeText(
                        this@PreviewSavedActivity,
                        "Lỗi khi phát video",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }

            } else {
                controlsContainer.gone()
                videoView.gone()
                imageView.visible()

                txtVideoTitle.text = photo.name
                imageView.loadImageIcon(
                    photo.path
                )
            }

        }
    }

    override fun initActionView() {
        with(binding) {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnMore.setOnClickListener {
                bottomSheet = InfoBottomSheet(
                    onShareClick = {
                        shareFileVideo(this@PreviewSavedActivity, photo.path, false)
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
            btnPlayPause.setOnClickListener {
                if (videoView.isPlaying) {
                    videoView.pause()
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                    stopSeekBar()
                } else {
                    videoView.start()
                    btnPlayPause.setImageResource(R.drawable.ic_pause)
                    updateSeekBar()
                }

            }
            btnRewind.setOnClickListener {
                val newPosition = (videoView.currentPosition - 5000).coerceAtLeast(0)
                videoView.seekTo(newPosition)
            }
            btnForward.setOnClickListener {
                val newPosition =
                    (videoView.currentPosition + 5000).coerceAtMost(videoView.duration)
                videoView.seekTo(newPosition)
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        binding.videoView.seekTo(progress)
                        binding.txtCurrentTime.text = progress.formatDuration()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopSeekBar()
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (videoView.isPlaying) {
                        updateSeekBar()
                    }
                }
            })
        }
    }

    private fun updateSeekBar() {
        stopSeekBar()
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                try {
                    if (binding.videoView.isPlaying) {
                        binding.seekBar.progress = binding.videoView.currentPosition
                        binding.txtCurrentTime.text = binding.videoView.currentPosition.formatDuration()
                        handler.postDelayed(this, 100)
                    }
                }catch (e:Exception){
                    Log.e("PreviewSavedActivity", "lOI CAP NHAT: ${e.message}")
                }
            }
        }
        updateSeekBarRunnable?.let {
            handler.post(it)
        }
    }

    private fun stopSeekBar() {
        updateSeekBarRunnable?.let { handler.removeCallbacks(it) }
        updateSeekBarRunnable = null
    }

    fun setupInfoDialog(photo: Photo) {
        val infoDialog = Dialog(this)
        val dialogBinding = InforImgDialogBinding.inflate(layoutInflater)
        infoDialog.setContentView(dialogBinding.root)
        Log.d("setupInfoDialog", "setupInfoDialog: ${photo.dateAdded}")
        with(dialogBinding) {
            tvName.text = photo.name
            tvSize.text = photo.size.kbToMb()
            tvDate.text = photo.dateAdded.formatCapturedTime()
            tvPath.text = "${photo.path}"
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

    private fun deletePhoto() {
        val dialogView = layoutInflater.inflate(R.layout.delete_dialog, null)

        val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.btnClCancel)
        val btnDelete = dialogView.findViewById<AppCompatButton>(R.id.btnClStop)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            try {
                val rowsDeleted = contentResolver.delete(photo.path, null, null)
                if (rowsDeleted > 0) {
                    Toast.makeText(this,
                        getString(R.string.image_deleted_successfully), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this,
                        getString(R.string.failed_to_delete_image), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PreviewSavedActivity", "Lỗi xóa: ${e.message}")
               // Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()

    }

    fun shareFileVideo(context: Context, uri: Uri, isVideo: Boolean) {
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
    }
}
