package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

/**
 * Created by Daniel
 * */
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    //Use Koin to get the view model of the SaveReminder
   // override val _viewModel: SaveReminderViewModel by inject()
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map : GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var poi: PointOfInterest
    private lateinit var fromCurrentLocation : List<Address>

    private val zoomLevel = 15f

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var defaultLocation : LatLng
    private var lastKnownLocation : Location? = null
    private var selectedMarker : Marker? = null
  //  private lateinit var locationName : String

    private var isLocationPermissionGranted = false



    // NB : this is hard coded, you should be getting the value instead from the user
    private val GEOFENCE_RADIUS = 50f

    // The entry point to the Fused Location Provider.


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // change soon
        defaultLocation = LatLng(-33.8523341, 151.2106085)



        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)





        var  mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

       // getUserLocation()

        return binding.root
    }


    override fun onMapReady(googleMap : GoogleMap) {

        map = googleMap
        setMapStyle(map)


        enableMyLocation()
        getUserLocation()
        setPoiClick(map)
        map.setOnMapLongClickListener(this)


    }





    override fun onMapLongClick(latLng: LatLng) {
        selectedMarker?.remove()
        selectedMarker?.position = latLng

        map.addMarker(MarkerOptions().position(latLng)).also {
            selectedMarker = it

        }

        map.animateCamera(CameraUpdateFactory.newLatLng(latLng))

        onLocationSelected(latLng)

    }



    /**
 * On point of interest selected navigate back to previous screen
 * */
    private fun onLocationSelected(latLng: LatLng) {
        //        TODO: When the user confirms on the selected location,

       val fromLocation = Geocoder(activity).getFromLocation(latLng.latitude, latLng.longitude, 2)
        binding.positionClick.setOnClickListener {


            if (this::poi.isInitialized) {
                _viewModel.latitude.value = poi.latLng.latitude
                _viewModel.longitude.value = poi.latLng.longitude
                _viewModel.reminderSelectedLocationStr.value = poi.name
                _viewModel.selectedPOI.value = poi
                _viewModel.navigationCommand.value = NavigationCommand.Back

            }

            else {
                _viewModel.latitude.value = latLng.latitude
                _viewModel.longitude.value = latLng.longitude
                _viewModel.reminderSelectedLocationStr.value = fromLocation[0].locality
                _viewModel.navigationCommand.value = NavigationCommand.Back

            }


        }

    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    /**
     *   item menu appear on click of menu icon.
     * */
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


/**
 * Enable location if user grant permission
 * */
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true

        }

        else {
            this.requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }


    /**
     * Check if user grant permission and enable location
     * */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {

            _viewModel.showSnackBar.postValue(getString(R.string.permission_denied_explanation))
        }
    }

    private fun setPoiClick (map : GoogleMap) {
        map.setOnPoiClickListener {pointOfInterest ->
            map.clear()
            poi = pointOfInterest
            val poiMaker = map.addMarker(
                MarkerOptions().position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
            )

            poiMaker.showInfoWindow()
        }
    }


    /**
     * Set long click on map
     * */
//    private fun setMapLongClick (map : GoogleMap) {
//
//        map.setOnMapLongClickListener { latLng ->
//
//            val snippet = String.format(
//                Locale.getDefault(),
//                "Lat : %1$.5f, Long: %2$.5f",
//                latLng.latitude,
//                latLng.longitude
//            )
////            map.clear()
//            map.addMarker(
//                MarkerOptions().position(latLng)
//                    .title(getString(R.string.dropped_pin))
//                    .snippet(snippet)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//            )
//
//            //addCircle(latLng, GEOFENCE_RADIUS)
//        }
//    }


    /**
     * Get user current location
     * */
    @SuppressLint("MissingPermission")
    private fun getUserLocation () {

        // get user location using [FusedLocationClientProvider Api]

        Log.i("CHECK_GRANTED", "$isLocationPermissionGranted")

//        if (isLocationPermissionGranted) {

            try {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) {locationTask ->

                    if (locationTask.isSuccessful) {

                        lastKnownLocation = locationTask.result

                        if (lastKnownLocation != null) {
                            map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                    lastKnownLocation!!.latitude,
                                                    lastKnownLocation!!.longitude
                                            ), zoomLevel
                                    )
                            )
                        }
                    } else {
                        Log.e( "Exception: %s", "${locationTask.exception}")

                        map.moveCamera(
                                CameraUpdateFactory
                                        .newLatLngZoom(defaultLocation, zoomLevel)
                        )
                        map.uiSettings.isMyLocationButtonEnabled = true
                    }
                }
            }  catch (e: SecurityException) {
                Log.e("Exception: %s","${ e.message}")
            }



     //   }

    }

/**
 * Add circle
 * */
    private fun addCircle (latLng: LatLng, radius: Float) {

        val circleOptions = CircleOptions()

        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.GREEN)
        circleOptions.fillColor(Color.alpha(R.color.colorPrimary))
        circleOptions.strokeWidth(4F)

        map.addCircle(circleOptions)
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
