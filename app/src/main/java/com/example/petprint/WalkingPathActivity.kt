package com.example.petprint


import android.Manifest
import android.R
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*


/**
 * An activity that displays a map showing the place at the device's current location.
 */
class WalkingPathActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mCameraPosition: CameraPosition? = null

    // The entry point to the Places API.
    private var mPlacesClient: PlacesClient? = null

    // The entry point to the Fused Location Provider.
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation =
        LatLng(-33.8523341, 151.2106085)
    private var mLocationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: Location? = null
    private var mLikelyPlaceNames: Array<String?>
    private var mLikelyPlaceAddresses: Array<String?>
    private var mLikelyPlaceAttributions: Array<List<*>?>
    private var mLikelyPlaceLatLngs: Array<LatLng?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation =
                savedInstanceState.getParcelable(KEY_LOCATION)
            mCameraPosition =
                savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps)

        // Construct a PlacesClient
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        mPlacesClient = Places.createClient(this)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Build the map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        if (mMap != null) {
            outState.putParcelable(
                KEY_CAMERA_POSITION,
                mMap!!.cameraPosition
            )
            outState.putParcelable(
                KEY_LOCATION,
                mLastKnownLocation
            )
            super.onSaveInstanceState(outState)
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            showCurrentPlace()
        }
        return true
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap!!.setInfoWindowAdapter(object : InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow: View = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    findViewById<View>(R.id.map) as FrameLayout, false
                )
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        deviceLocation
    }// Set the map's camera position to the current location of the device./*
    * Get the best and most recent location of the device, which may be null in rare
    * cases when a location is not available.
    */

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    val deviceLocation: Unit
        private get() {
            /*
             * Get the best and most recent location of the device, which may be null in rare
             * cases when a location is not available.
             */
            try {
                if (mLocationPermissionGranted) {
                    val locationResult =
                        mFusedLocationProviderClient!!.lastLocation
                    locationResult.addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.result
                            if (mLastKnownLocation != null) {
                                mMap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            mLastKnownLocation!!.latitude,
                                            mLastKnownLocation!!.longitude
                                        ),
                                        DEFAULT_ZOOM.toFloat()
                                    )
                                )
                            }
                        } else {
                            Log.d(
                                TAG,
                                "Current location is null. Using defaults."
                            )
                            Log.e(
                                TAG,
                                "Exception: %s",
                                task.exception
                            )
                            mMap!!.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        mDefaultLocation,
                                        DEFAULT_ZOOM.toFloat()
                                    )
                            )
                            mMap!!.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message)
            }
        }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private fun showCurrentPlace() {
        if (mMap == null) {
            return
        }
        if (mLocationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields: List<Place.Field> =
                Arrays.asList<Place.Field>(
                    Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )

            // Use the builder to create a FindCurrentPlaceRequest.
            val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult: Task<FindCurrentPlaceResponse> =
                mPlacesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener(object :
                OnCompleteListener<FindCurrentPlaceResponse?> {
                override fun onComplete(task: Task<FindCurrentPlaceResponse?>) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        val likelyPlaces: FindCurrentPlaceResponse? = task.getResult()

                        // Set the count, handling cases where less than 5 entries are returned.
                        val count: Int
                        count = if (likelyPlaces.getPlaceLikelihoods()
                                .size() < M_MAX_ENTRIES
                        ) {
                            likelyPlaces.getPlaceLikelihoods().size()
                        } else {
                            M_MAX_ENTRIES
                        }
                        var i = 0
                        mLikelyPlaceNames = arrayOfNulls(count)
                        mLikelyPlaceAddresses = arrayOfNulls(count)
                        mLikelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                        mLikelyPlaceLatLngs =
                            arrayOfNulls(count)
                        for (placeLikelihood in likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            mLikelyPlaceNames[i] = placeLikelihood.getPlace().getName()
                            mLikelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress()
                            mLikelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                .getAttributions()
                            mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng()
                            i++
                            if (i > count - 1) {
                                break
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        openPlacesDialog()
                    } else {
                        Log.e(
                            TAG,
                            "Exception: %s",
                            task.getException()
                        )
                    }
                }
            })
        } else {
            // The user has not granted permission.
            Log.i(
                TAG,
                "The user did not grant location permission."
            )

            // Add a default marker, because the user hasn't selected a place.
            mMap!!.addMarker(
                MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet))
            )

            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private fun openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        val listener =
            DialogInterface.OnClickListener { dialog, which -> // The "which" argument contains the position of the selected item.
                val markerLatLng =
                    mLikelyPlaceLatLngs[which]
                var markerSnippet = mLikelyPlaceAddresses[which]
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = """
                        $markerSnippet
                        ${mLikelyPlaceAttributions[which]}
                        """.trimIndent()
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap!!.addMarker(
                    MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng!!)
                        .snippet(markerSnippet)
                )

                // Position the map's camera at the location of the marker.
                mMap!!.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        markerLatLng,
                        DEFAULT_ZOOM.toFloat()
                    )
                )
            }

        // Display the dialog.
        val dialog: AlertDialog =
            AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show()
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (mLocationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    companion object {
        private val TAG = WalkingPathActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }
}
