import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.velomap.R
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация MapView
        mapView = findViewById(R.id.mapView)

        // Загрузка карты с указанным стилем
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


}
