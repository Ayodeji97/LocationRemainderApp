package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map : GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var poi: PointOfInterest


    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this




        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)



        var  mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }


    override fun onMapReady(googleMap : GoogleMap) {

        map = googleMap

        setMapStyle(map)

        val latitude =  6.690697
        val longitude = 3.245417

        val zoomLevel = 18f
        val overlaySize = 100f

        val homeLatLong = LatLng(latitude, longitude)

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLong, zoomLevel))

        // Adding overlay

        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.android_img))
            .position(homeLatLong, overlaySize)

        map.addMarker(MarkerOptions().position(homeLatLong))
        map.addGroundOverlay(androidOverlay)

//        TODO: zoom to the user location after taking his permission
            enableMyLocation()
//        TODO: add style to the map
            setMapStyle(map)
//        TODO: put a marker to location that the user selected
            setMapLongClick(map)
            setPoiClick(map)




    }



    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        binding.positionClick.setOnClickListener {

            if (this::poi.isInitialized) {

                _viewModel.latitude.value = poi.latLng.latitude
                _viewModel.longitude.value = poi.latLng.longitude
                _viewModel.reminderSelectedLocationStr.value = poi.name
                _viewModel.selectedPOI.value = poi
               _viewModel.navigationCommand.value = NavigationCommand.Back
            } else {
                Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show()
            }

        }



        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }



    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }

        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }


    private fun setPoiClick (map : GoogleMap) {

        map.setOnPoiClickListener {pointOfInterest ->
            map.clear()

            poi = pointOfInterest
            Log.i("PIONTTTT", "$poi")
            Log.i("SWWWWWWW", "${pointOfInterest.latLng}")
            val poiMaker = map.addMarker(
                MarkerOptions().position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
            )

            poiMaker.showInfoWindow()
        }
    }


    private fun setMapLongClick (map : GoogleMap) {

        map.setOnMapLongClickListener { latLng ->

            val snippet = String.format(
                Locale.getDefault(),
                "Lat : %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions().position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("ERR", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Resource_Error", "Can't find style. Error: ", e)
        }
    }

}
