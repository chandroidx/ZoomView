package ai.deepfine.zoomviewdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import ai.deepfine.ycpark.ZoomView
import com.github.zoomviewdemo.R

class MainActivity : AppCompatActivity() {
    lateinit var zoomView: ZoomView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        zoomView = findViewById(R.id.zoomView)
    }


    fun zoomIn(view: View) {
        zoomView.smoothZoomTo(2.0F)
    }

    fun zoomOut(view: View) {
        zoomView.smoothZoomTo(1.0F)
    }
}