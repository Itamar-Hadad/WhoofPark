package com.example.whoofpark.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.whoofpark.R
import com.example.whoofpark.model.DogParkFeature
import com.example.whoofpark.model.DogParkResponse
import com.example.whoofpark.utilities.Constants
import com.example.whoofpark.utilities.SignalManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private var allParks: List<DogParkFeature> = listOf()

    companion object {
        private const val REQ_LOCATION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment

        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction()
                .replace(R.id.map_container, newMapFragment)
                .commit()

            newMapFragment.getMapAsync(this)
        } else {
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true

        googleMap?.setOnMarkerClickListener { marker ->
            val parkId = marker.tag as? String
            val selectedPark = allParks.find { it.attributes.id == parkId }

            if (parkId != null) {
                val bundle = Bundle().apply {

                    putString(Constants.BundleKeys.PARK_ID_KEY, parkId)
                    putString(Constants.BundleKeys.PARK_NAME_KEY, selectedPark?.attributes?.name)
                    putString(Constants.BundleKeys.PARK_HOURS_KEY, selectedPark?.attributes?.hours ?: "24/7")
                    putString(Constants.BundleKeys.PARK_ADDRESS_KEY, selectedPark?.attributes?.address)
                    putDouble(Constants.BundleKeys.PARK_LAT_KEY, selectedPark?.geometry?.y ?: 0.0)
                    putDouble(Constants.BundleKeys.PARK_LON_KEY, selectedPark?.geometry?.x ?: 0.0)

                }

                findNavController()
                    .navigate(
                        R.id.action_mapFragment_to_parkDetailFragment,
                        bundle)
            }
            true
        }

        enableMyLocationAndMoveCamera()
        fetchDogParksFromTelAvivApi()
    }


    private fun enableMyLocationAndMoveCamera() {
        val map = googleMap ?: return

        val hasPermission = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_LOCATION
            )
            return
        }

        map.isMyLocationEnabled = true

        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val latLng = if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                LatLng(32.0853, 34.7818) // תל אביב fallback
            }

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_LOCATION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocationAndMoveCamera()
        }
    }

    private fun fetchDogParksFromTelAvivApi() {
        val url = "https://gisn.tel-aviv.gov.il/arcgis/rest/services/IView2/MapServer/586/query?where=1%3D1&outFields=*&outSR=4326&returnGeometry=true&f=json"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    SignalManager
                        .getInstance()
                        .toast(
                            "Network error",
                            SignalManager.ToastLength.SHORT)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()


                android.util.Log.d("API_RESPONSE", "Full Output: $jsonString")

                if (jsonString != null) {
                    try {
                        val dogParkResponse = Gson().fromJson(jsonString, DogParkResponse::class.java)
                        activity?.runOnUiThread {
                            allParks = dogParkResponse.features
                            displayParksOnMap(dogParkResponse.features)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("API_ERROR", "Parsing failed: ${e.message}")
                    }
                }
            }
        })
    }


    private fun displayParksOnMap(features: List<DogParkFeature>?) {
        val map = googleMap ?: return
        val safeFeatures = features ?: return

        for (park in safeFeatures) {

            if (park.geometry.x != 0.0 && park.geometry.y != 0.0) {
                val latLng = LatLng(park.geometry.y, park.geometry.x)

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(park.attributes.name)
                        .snippet(park.attributes.address)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_area))
                )
                marker?.tag = park.attributes.id

            } else {
                //which garden not come
                android.util.Log.w("MAP_DEBUG", "Skipping park with no location: ${park.attributes.name}")
            }
        }
    }

}