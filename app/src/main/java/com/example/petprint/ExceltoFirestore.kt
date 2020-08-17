package com.example.petprint

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.InputStream

//엑셀 파일의 데이터를 추출해서 firestore에 저장하는 코드
//excel -> mutableList -> mutableMap -> firestore
//https://hamzzibari.tistory.com/120 (이곳의 코드를 참고하였습니다.)

class ExceltoFirestore : AppCompatActivity() {
    //cloud firestore 초기화, 콜렉션은 여기서 해도 되고 아래에서 정의해도 됩니다.
    private val quizDb = FirebaseFirestore.getInstance().collection("Jungnang") //Seoul 컬렉션과 연결(?)
    private val dataToSave = mutableMapOf<String, String>() //각 다큐먼트의 필드
    var items: MutableList<SearchData> = mutableListOf() //엑셀 파일의 내용을 저장하는 리스트
    
    fun main(args: Array<String>) {
        readExcelFileFromAssets()


        //저장할 데이터를 만들어줍니다. (dataToSave는 mutableMapOf로 정의해줬습니다)
        for (i in 0 until items.size) {
            dataToSave["snippet"] = items[i].parkType
            dataToSave["address(road)"] = items[i].parkAddress1
            dataToSave["address(lot)"] = items[i].parkAddress2
            dataToSave["lat"] = items[i].parkLat
            dataToSave["lng"] = items[i].parkLng
            dataToSave["phoneNumber"] = items[i].parkPhone
            dataToSave["Equipment"] = items[i].parkEquip

            //저는 여러개의 다큐먼트가 필요해서 다큐먼트도 유동적으로 생성되게 했습니다.
            //아래 코드는 dataToSave를 필드로 하여 다큐먼트를 새로 생성한다.
            quizDb.document(items[i].parkName) //매개변수: 다큐먼트의 이름이 된다.
                //set("저장할 데이터")
                .set(dataToSave) //dataToSave가 생성된 다큐먼트의 필드로 저장된다.
                .addOnSuccessListener { documentReference ->
                    Log.d("asdf", "저장 성공")
                }
                .addOnFailureListener { e ->
                    Log.w("asdf", "Error adding document", e)
                }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exceltofirestore)

        //엑셀 파일을 읽어 리스트에 저장하는 함수


    }


    private fun readExcelFileFromAssets() {
        try {
            val myInput: InputStream
            // assetManager 초기 설정
            val assetManager = assets
            //  엑셀 시트 열기
            myInput = assetManager.open("서울특별시_중랑구_도시공원정보_20190924.xls")
            // POI File System 객체 만들기
            val myFileSystem = POIFSFileSystem(myInput)
            //워크 북
            val myWorkBook = HSSFWorkbook(myFileSystem)
            // 워크북에서 시트 가져오기
            val sheet = myWorkBook.getSheetAt(0)

            //행을 반복할 변수 만들어주기
            val rowIter = sheet.rowIterator()
            //행 넘버 변수 만들기
            var rowno = 0
            //MutableList 생성 은 맨 위에 전역 변수로 빼놓음


            //행 반복문
            while (rowIter.hasNext()) {
                val myRow = rowIter.next() as HSSFRow
                if (rowno != 0) {
                    //열을 반복할 변수 만들어주기
                    val cellIter = myRow.cellIterator()
                    //열 넘버 변수 만들기
                    var colno = 0

                    //건드릴 부분. 빼내고자 하는 열의 수에 맞춰 변수를 선언한다.
                    //공원명, 공원구분, 주소(도로명, 지번), 위도, 경도, 공원보유시설, 전화번호
                    var parkName = ""
                    var parkType = ""
                    var parkAddress1 = "" //도로명 주소
                    var parkAddress2 = "" //지번 주소
                    var parkLat = "" //firestore에는 Double 타입이 없어서 그냥 String으로 넣고 꺼낼 때 캐스팅하면 될듯
                    var parkLng = ""
                    var parkPhone = ""
                    var parkEquip = ""


                    //열 반복문
                    while (cellIter.hasNext()) {
                        val myCell = cellIter.next() as HSSFCell

                        if(colno === 1) //2번째 열이라면,
                            parkName = myCell.toString() //대충 셀의 내용을 꺼내와 String으로 저장한다는 의미인듯
                        else if (colno === 2)
                            parkType = myCell.toString()
                        else if (colno === 3)
                            parkAddress1 = myCell.toString()
                        else if (colno === 4)
                            parkAddress2 = myCell.toString()
                        else if (colno === 5)
                            parkLat = myCell.toString()
                        else if (colno === 6)
                            parkLng = myCell.toString()
                        else if (colno === 15)
                            parkPhone = myCell.toString()
                        else if (colno === 17)
                            parkEquip = myCell.toString()

                        Log.e("colnoNum", "" + colno + "\n")
                        colno++
                    }

                    //한 행의 데이터가 리스트에 추가된다.
                    items.add(SearchData(parkName, parkType, parkAddress1, parkAddress2, parkLat, parkLng, parkPhone, parkEquip))

                    //저장할 데이터를 만들어줍니다. (dataToSave는 mutableMapOf로 정의해줬습니다)
//                    dataToSave.put("no", items[rowno].parkName)

                }
                rowno++
            }

            Log.e("checking", " items: $items")

        } catch (e: Exception) {
            Toast.makeText(this, "에러 발생", Toast.LENGTH_LONG).show()
            Log.d("errorrr", e.toString())
            //예외가 발생하면 꼭 로그를 찍어 어떤 종류의 예외가 발생했는지 알아내자.
       }
    }
}
