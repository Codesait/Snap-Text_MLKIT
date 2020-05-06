package app.tcodes.scantext.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import app.tcodes.scantext.processor.OcrBitmapProcessor
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class MainViewModel(application: Application) : AndroidViewModel(application) {

   // private val snapchatifyBitmapProcessor  = SnapchatifyBitmapProcessor(application)

   // private val barcodeBitmapProcessor = BarcodeBitmapProcessor()

   // private val landmarkBitmapProcessor = LandmarkBitmapProcessor()

    var processedBitmap : MutableLiveData<Bitmap> = MutableLiveData()

    val textResult = MutableLiveData<String>()


    fun ocrDetection(bitmap: Bitmap?){
        bitmap?.let {
            doOcrDetection(bitmap)
        }
    }

    private val ocrBitmapProcessor = OcrBitmapProcessor()



    private fun doOcrDetection(bitmap: Bitmap){
        //<editor-fold desc="OcrDetection">
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        val firebaseImage = FirebaseVisionImage.fromBitmap(bitmap)

        detector.processImage(firebaseImage)
            .addOnSuccessListener {
                processedBitmap.postValue(ocrBitmapProcessor.drawBoundingBoxes(bitmap, it))
                var result = String()
                if (it.textBlocks.size == 0){
                    Toast.makeText(getApplication(), "No Text detected", Toast.LENGTH_LONG).show()
                    //progressBar.=View.INVISIBLE
                }
                it.textBlocks.forEach {
                    result += " " + it.text
                    textResult.postValue(result)
                    Log.d("Landmark", result)
                }
            }
            .addOnFailureListener{
                Toast.makeText(getApplication(), "Error detecting Text $it", Toast.LENGTH_LONG).show()
            }
        //</editor-fold>

    }

}
