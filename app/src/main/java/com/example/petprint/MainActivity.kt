package com.example.petprint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
        var i = 0

        db.collection("Seoul")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    i++
                    Log.d("hangnyang", "${document.id} => ${document.data}")

                    // Add a marker in Sydney and move the camera
                    //아니 이게 왜 되는거야
                    var location = LatLng(document.data["lat"].toString().toDouble(), document.data["lng"].toString().toDouble()) //매개변수: 위도, 경도

                    val markerOptions = MarkerOptions() //핀
                    markerOptions.title(document.id) //주 내용
                    markerOptions.snippet(document.data["snippet"] as String) //세부 내용
                    markerOptions.position(location) //위치(위도, 경도 값)
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    googleMap.addMarker(markerOptions) //핀 추가

                    //해당 핀으로 카메라 이동
                    //아래 코드가 isSBSettingEnabled false 오류를 일으킴
                    //for문을 통해 여러번 실행하면 안 되는 듯
                    if(result.size() <= i) //마지막 핀으로 카메라 이동
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16F))
                }
            }
            .addOnFailureListener { exception ->
                Log.w("hangnyang", "Error getting documents.", exception)
            }

        val uiSettings: UiSettings = googleMap.uiSettings
        uiSettings.isZoomControlsEnabled = true //확대, 축소 버튼
    }

}

