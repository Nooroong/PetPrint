package com.example.petprint

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.cardview.*

/*
DB의 정보를 이용해 핀 그리기 + 세부정보 표시 + 현재 위치 표시 및 이동

<현재 위치 표시 및 이동>
https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial (이곳을 참고함)
WalkingPathActivity.kt의 코드에서 필요한 부분만 가져왔습니다.
자세한 부분은 WalkingPathActivity.kt를 참고하시면 됩니다.
 */


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val db = FirebaseFirestore.getInstance()
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private var currentMarker: Marker? = null
    private var nameMarker: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        setContentView(R.layout.activity_main)

        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        zoominBtn.setOnClickListener {
            map?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        zoomoutBtn.setOnClickListener {
            map?.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }



    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
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
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        var i = 0
        val collection = arrayOf<String>("Gangbuk", "Jungnang", "Nowon", "Seongbuk")

        //for문: DB에 저장된 정보를 바탕으로 핀을 그립니다.
        for (path in collection) {
            db.collection(path)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        i++
//                        Log.d("mapPoint", "${document.id} => ${document.data}")

                        // Add a marker in Sydney and move the camera
                        //아니 이게 왜 되는거야
                        var location = LatLng(
                            document.data["lat"].toString().toDouble(),
                            document.data["lng"].toString().toDouble()
                        ) //매개변수: 위도, 경도

                        val markerOptions = MarkerOptions() //핀
                        markerOptions.title(document.id) //공원 이름

                        //val snippet_t = TextView(applicationContext)
//                        val markerSnippet =
//                            "공원 종류: " + document.data["snippet"].toString() + "\n" + "전화번호: " + document.data["snippet"].toString() +"\n"+"보유시설: "+document.data["Equipment"].toString()
//                        val markerSnippet =
//                            "이거되나 : " + "전화번호: " +"\n"+"보유시설: "
//                          이거 어떻게 넣어도 맨 앞 줄만 나와서 일단 패스. 공원 종류... 이거되나... 이렇게 뒤에 다 줄여짐 xml바꿔봐도 똑같네

                        markerOptions.snippet(document.data["snippet"] as String) //공원 종류
//                        markerOptions.snippet(markerSnippet)
                        markerOptions.position(location) //위치(위도, 경도 값)
                        markerOptions.icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_AZURE
                            )
                        )
                        googleMap.addMarker(markerOptions) //핀 추가

                        //해당 핀으로 카메라 이동
                        //아래 코드가 isSBSettingEnabled false 오류를 일으킴
                        //for문을 통해 여러번 실행하면 안 되는 듯
                        if (result.size() <= i) //마지막 핀으로 카메라 이동
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16F))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("mapPoint", "Error getting documents.", exception)
                }
        }

        //마커 클릭 리스너-마커 클릭하면 카드뷰 나와야 함
        googleMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
//                Log.d("aaaa","클릭됨")

                return false
            }
        })

        //맵 클릭 리스너-맵 클릭하면 카드뷰 없어져야 함
        googleMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                currentMarker = null
            }
        })

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()


//        val uiSettings: UiSettings = googleMap.uiSettings
//        uiSettings.isZoomControlsEnabled = true //확대, 축소 버튼

    }


    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }


    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    companion object {
        private val TAG = WalkingPathActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }


}

