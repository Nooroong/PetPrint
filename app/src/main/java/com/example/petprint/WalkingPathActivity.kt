package com.example.petprint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

//경로 그리기

class WalkingPathActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walking_path)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


        //일정 시간 단위로 위치 정보를 받아오고
        //그걸 polyline으로 그리면??


        //Polyline은 일련의 연결된 선분입니다.
        //다중선은 지도에서 경로 또는 기타 위치 간 연결을 나타내는 데 유용합니다.
        val polyline1 = googleMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    LatLng(-35.016, 143.321),
                    LatLng(-34.747, 145.592),
                    LatLng(-34.364, 147.891),
                    LatLng(-33.501, 150.217),
                    LatLng(-32.306, 149.248),
                    LatLng(-32.491, 147.309)
                )
        )

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(-23.684, 133.903), 4f
            )
        )

        googleMap.setOnPolylineClickListener(this)
        googleMap.setOnPolygonClickListener(this)
    }

    override fun onPolylineClick(p0: Polyline?) {

    }

    override fun onPolygonClick(p0: Polygon?) {

    }


//    다중선에 맞춤 스타일 추가
    private val COLOR_BLACK_ARGB = -0x1000000
    private val POLYLINE_STROKE_WIDTH_PX = 12

    private fun stylePolyline(polyline: Polyline) {
        var type = ""
        // Get the data object stored with the polyline.
        if (polyline.tag != null) {
            type = polyline.tag.toString()
        }
        when (type) {
//            "A" ->             // Use a custom bitmap as the cap at the start of the line.
//                polyline.startCap = CustomCap(
//                    BitmapDescriptorFactory.fromResource(android.R.drawable.ic_arrow), 10
//                )
            "B" ->             // Use a round cap at the start of the line.
                polyline.startCap = RoundCap()
        }
        polyline.endCap = RoundCap()
        polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
        polyline.color = COLOR_BLACK_ARGB
        polyline.jointType = JointType.ROUND
    }
}