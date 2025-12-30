package com.example.finalyearproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException

class MapPickerActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var mapView: MapView
    private var selectedPoint: GeoPoint? = null
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSMDroid configuration
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map_picker)

        mapView = findViewById(R.id.map_picker_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        searchView = findViewById(R.id.map_search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val mapController = mapView.controller
        mapController.setZoom(9.5)
        val startPoint = GeoPoint(27.7172, 85.3240) // Kathmandu
        mapController.setCenter(startPoint)

        // Add marker on long press
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                addMarker(p)
                selectedPoint = p
                return true
            }
        })
        mapView.overlays.add(0, mapEventsOverlay)

        requestLocationPermissions()

        val btnSaveLocation = findViewById<Button>(R.id.btn_save_picked_location)
        btnSaveLocation.setOnClickListener {
            if (selectedPoint != null) {
                showNameLocationDialog()
            } else {
                // Handle case where no location is selected
            }
        }
    }

    private fun searchLocation(locationName: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList = geocoder.getFromLocationName(locationName, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val geoPoint = GeoPoint(address.latitude, address.longitude)
                mapView.controller.animateTo(geoPoint)
                addMarker(geoPoint)
                selectedPoint = geoPoint
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun addMarker(p: GeoPoint) {
        mapView.overlays.removeAll { it is Marker }
        val marker = Marker(mapView)
        marker.position = p
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun showNameLocationDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_name_location, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.et_location_name_input)

        with(builder){
            setTitle("Name Your Location")
            setPositiveButton("Save"){
                dialog, which ->
                    val locationName = editText.text.toString()
                    val resultIntent = Intent()
                    resultIntent.putExtra("location_name", locationName)
                    resultIntent.putExtra("latitude", selectedPoint!!.latitude)
                    resultIntent.putExtra("longitude", selectedPoint!!.longitude)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
            }
            setNegativeButton("Cancel"){
                dialog, which ->
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            setupMyLocationOverlay()
        }
    }

    private fun setupMyLocationOverlay() {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMyLocationOverlay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }
}