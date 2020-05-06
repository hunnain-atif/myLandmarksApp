package com.hunnainatif.myLandmarks

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback, OnMapLongClickListener {
    private var mMap: GoogleMap? = null
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, locationListener)
                val lastKnownLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                centerMap(lastKnownLocation, "Your Location")
            }
        }
    }

    fun centerMap(location: Location, title: String) {
        val currentLocation = LatLng(location.latitude, location.longitude)
        mMap?.clear()
        if (title !== "Your Location") {
            mMap?.addMarker(MarkerOptions().position(currentLocation).title(title))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 9f))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setOnMapLongClickListener(this)
        val intent = intent
        if (intent.getIntExtra("placeNumber", 0) == 0) {
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    centerMap(location, "Your Location")
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            if (Build.VERSION.SDK_INT < 23) {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, locationListener)
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, locationListener)
                    val lastKnownLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    centerMap(lastKnownLocation, "Your Location")
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }
        } else {
            val placeLocation = Location(LocationManager.GPS_PROVIDER)
            placeLocation.latitude = MainActivity.locations[intent.getIntExtra("placeNumber", 0)].latitude
            placeLocation.longitude = MainActivity.locations[intent.getIntExtra("placeNumber", 0)].longitude
            centerMap(placeLocation, MainActivity.places[intent.getIntExtra("placeNumber", 0)]!!)
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        var address = ""
        try {
            val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addressList != null && addressList.size > 0) {
                if (addressList[0].thoroughfare != null) {
                    if (addressList[0].subThoroughfare != null) {
                        address += addressList[0].subThoroughfare + " "
                    }
                    address += addressList[0].thoroughfare
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (address === "") {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
            address = simpleDateFormat.format(Date())
        }
        mMap?.clear()
        mMap?.addMarker(MarkerOptions().position(latLng).title(address))
        MainActivity.places.add(address)
        MainActivity.locations.add(latLng)
        MainActivity.arrayAdapter?.notifyDataSetChanged()
        val sharedPreferences = getSharedPreferences("com.hunnainatif.memorableplaces", Context.MODE_PRIVATE)
        try {
            val latitudes = ArrayList<String>()
            val longitudes = ArrayList<String>()
            for (coordinates in MainActivity.locations) {
                latitudes.add(java.lang.Double.toString(coordinates.latitude))
                longitudes.add(java.lang.Double.toString(coordinates.longitude))
            }
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply()
            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply()
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(this, "location saved", Toast.LENGTH_SHORT).show()
    }
}