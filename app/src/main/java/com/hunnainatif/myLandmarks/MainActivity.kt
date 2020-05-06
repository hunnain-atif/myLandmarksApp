package com.hunnainatif.myLandmarks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<View>(R.id.listView) as ListView
        val sharedPreferences = getSharedPreferences("com.hunnainatif.memorableplaces", Context.MODE_PRIVATE)
        var latitudes = ArrayList<String>()
        var longitudes = ArrayList<String>()
        places.clear()
        latitudes.clear()
        longitudes.clear()
        locations.clear()
        try {
            places = ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String?>
            latitudes = ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
            longitudes = ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<String>
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (places.size > 0 && latitudes.size > 0 && longitudes.size > 0) {
            if (places.size == latitudes.size && latitudes.size == longitudes.size) {
                for (i in places.indices) {
                    locations.add(LatLng(latitudes[i].toDouble(), longitudes[i].toDouble()))
                }
            }
        } else {
            places.add("Add a new place... ")
            locations.add(LatLng(0.0, 0.0))
        }
        arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, MapsActivity::class.java)
            intent.putExtra("placeNumber", position)
            startActivity(intent)
        }
    }

    companion object {
        @JvmField
        var places = ArrayList<String?>()
        @JvmField
        var locations = ArrayList<LatLng>()
        @JvmField
        var arrayAdapter: ArrayAdapter<*>? = null
    }
}