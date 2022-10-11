package pl.birski.customobjectdetectionapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.birski.customobjectdetectionapp.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE: Int = 1
    }

    private lateinit var currentPhotoPath: String

    private lateinit var binding: ActivityMainBinding

    private var requestSinglePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        it.entries.forEachIndexed() { index, _ ->
            PermissionUtil.returnPermissionsArray()[index]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.selectImageBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        binding.captureImageBtn.setOnClickListener {
            dispatchTakePictureIntent()
        }

        askForPermissions()

        setContentView(binding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE &&
            resultCode == Activity.RESULT_OK
        ) {
            setViewAndDetect(getCapturedImage())
        }
    }

    private fun getCapturedImage(): Bitmap {
        val targetW: Int = binding.imageView.width
        val targetH: Int = binding.imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight
            val scaleFactor: Int = max(1, min(photoW / targetW, photoH / targetH))

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inMutable = true
        }
        val exifInterface = ExifInterface(currentPhotoPath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(bitmap, 270f)
            }
            else -> {
                bitmap
            }
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile = try {
                    createImageFile()
                } catch (e: IOException) {
                    Log.e(MainActivity::class.simpleName, e.message.toString())
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "pl.birski.customobjectdetectionap",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let {
            setViewAndDetect(getBitmap(it))
        }
    }

    private fun askForPermissions() {
        requestSinglePermission.launch(
            PermissionUtil.returnPermissionsArray()
        )
    }

    private fun getBitmap(uri: Uri) = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

    private fun runObjectDetection(bitmap: Bitmap) {
        // TODO: Add object detection code here
    }

    private fun setViewAndDetect(bitmap: Bitmap) {
        binding.imageView.setImageBitmap(bitmap)
        binding.tvPlaceholder.visibility = View.INVISIBLE

        lifecycleScope.launch(Dispatchers.Default) { runObjectDetection(bitmap) }
    }
}
