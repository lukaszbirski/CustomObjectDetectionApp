package pl.birski.customobjectdetectionapp

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import pl.birski.customobjectdetectionapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.selectImageBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        setContentView(binding.root)
    }

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let {
            binding.imageView.setImageBitmap(getBitmap(it))
        }
    }

    private fun getBitmap(uri: Uri) = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
}
