package com.example.helloworld

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button //baru
import android.widget.EditText //baru
import android.content.Intent //baru
import android.provider.MediaStore
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Marker
import android.location.Location
import android.location.LocationManager
import android.content.Context
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {

    private fun ambilLokasiGps(mapView: MapView){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val sharedPref = getSharedPreferences("DataUser", Context.MODE_PRIVATE)
        val dbHelper = DatabaseHelper(this)//call database helper

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        location?.let {
            val userLocation = GeoPoint(it.latitude, it.latitude)
            val markerBaru = Marker(mapView)
            markerBaru.position = userLocation
            markerBaru.title = "Lokasi Foto"
            markerBaru.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            mapView.overlays.add(markerBaru)

            mapView.controller.animateTo(userLocation)
            mapView.controller.setZoom(18.0)
            mapView.invalidate()

            val namaUser =sharedPref.getString("KEY_NAMA", "Anonim") ?: "Anonim"
            val lat = it.latitude.toString()
            val lon = it.longitude.toString()

            val hasilSimpan = dbHelper.simpanRiwayat(namaUser, lat, lon)

            if (hasilSimpan != -1L){
                android.widget.Toast.makeText(this, "Data Tersimpan Ke Database!", android.widget.Toast.LENGTH_SHORT).show()
            }

            android.widget.Toast.makeText(
                this,
                "Lokasi Terdeteksi!",
                android.widget.Toast.LENGTH_SHORT).show()
        }?: run {
            android.widget.Toast.makeText(
                this,
                "Gps Belum Siap",
                android.widget.Toast.LENGTH_LONG).show()
        }
    }


    //api camera
    private val ambilFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == RESULT_OK){
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            val ivFoto = findViewById<ImageView>(R.id.ivFoto)
            ivFoto.setImageBitmap(imageBitmap)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //konfigurasi osm
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //API MAP
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        //pengarahan map awal
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(-8.6703, 115.2125) //koordinat awal
        mapController.setCenter(startPoint)


        val inputNama =findViewById<EditText>(R.id.inputNama)
        val btnNext = findViewById<Button>(R.id.btnNext)

        //SharedPreferences
        val sharedPref = getSharedPreferences("DataUser", Context.MODE_PRIVATE)
        val namaTersimpan = sharedPref.getString("KEY_NAMA", "")
        if (!namaTersimpan.isNullOrEmpty()){
            inputNama.setText(namaTersimpan)
        }



        btnNext.setOnClickListener {
            val nama = inputNama.text.toString()

            val editor = sharedPref.edit()
            editor.putString("KEY_NAMA", nama)
            editor.apply()

            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("EXTRA_NAMA", nama)
            startActivity(intent)
        }

        //button input nama
        val btnKamera =findViewById<Button>(R.id.btnKamera)
        btnKamera.setOnClickListener {
            val intent = Intent (MediaStore.ACTION_IMAGE_CAPTURE)
            ambilFoto.launch(intent)
        }
    }
}
