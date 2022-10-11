package pl.birski.customobjectdetectionapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class Classifier(context: Context) {

    private var detector: ObjectDetector

    init {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.5f)
            .build()
        detector = ObjectDetector.createFromFileAndOptions(
            context,
            "model.tflite",
            options
        )
    }

    fun runObjectDetection(bitmap: Bitmap): List<DetectionResult> {
        val image = TensorImage.fromBitmap(bitmap)
        val results = detector.detect(image)
        val resultToDisplay = results.map {
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"

            DetectionResult(it.boundingBox, text)
        }
        return resultToDisplay
    }
}

data class DetectionResult(val boundingBox: RectF, val text: String)
