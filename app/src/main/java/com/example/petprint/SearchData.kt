package com.example.petprint

data class SearchData(val parkName: String, val parkType: String, val parkLat: String, val parkLng: String)

//SearchData 객체를 리스트에 삽입한다.
//SearchData의 매개변수는 firestore에서 필드가 된다.