package com.wink.knockmate

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.w3c.dom.Text

class EditProfileActivity : AppCompatActivity(), BottomSheetFragment_settings.OnDataPassListener {

    private val backButton : ImageButton by lazy{
        findViewById(R.id.backButton)
    }

    private val userImage : ImageButton by lazy{
        findViewById(R.id.profileImage)
    }

    private val editNickname : EditText by lazy{
        findViewById(R.id.editNickname)
    }

    private val completeButton : Button by lazy{
        findViewById(R.id.completeButton)
    }

    private val nicknameShort : TextView by lazy{
        findViewById(R.id.nicknameShort)
    }

    private val nicknameLong : TextView by lazy{
        findViewById(R.id.nicknameLong)
    }

    private val nicknameWrong : TextView by lazy{
        findViewById(R.id.nicknameFormWrong)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        backButton.setOnClickListener {
            finish()
        }

        val imageFlag = false

        // TODO 서버에서 기존 프로필 사진 가져오기
        // TODO 프로필 사진 있으면 imageFlag 값을 true로 변경
        // TODO 프로필 사진 없으면 기본 프로필 사진으로

        userImage.setOnClickListener {
            // TODO 프로필 사진 있는 경우 (imageFlag 이용해서 판별) 앨범에서 선택 / 프로필 사진 삭제 선택 기능
            if(imageFlag){
                val bottomSheetFragment = BottomSheetFragment_settings(applicationContext)

                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag) // bottom sheet fragment를 보여준다.
            } else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    when{
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ->{
                            navigatePhotos()
                        }
                        shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),2000)
                        }
                        else -> {
                            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),2000)
                        }
                    }
                }
            }

            // TODO 프로필 사진 없는 경우 바로 앨범으로 전환
            // TODO 선택된 사진을 userImage에 올리기
        }

        // TODO 서버에서 기존 닉네임 가져오기
        // TODO editNickname.text에 기본값으로 기존 닉네임 넣어주기

        editNickname.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                nicknameWrong.isVisible = false
                nicknameLong.isVisible = false
                nicknameShort.isVisible = false
                if(checkNickname() == "OK"){
                    activateButton()
                } else if(checkNickname() == "Wrong"){
                    inactivateButton()
                    nicknameWrong.isVisible = true
                } else if(checkNickname() == "Short"){
                    inactivateButton()
                    nicknameShort.isVisible = true
                } else{
                    inactivateButton()
                    nicknameLong.isVisible = true
                }
            }
        }
    }

    override fun onDataPass(data : Uri?){
        userImage.setImageURI(data)
    }

    private fun navigatePhotos(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent,1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK){
            return
        }

        when(requestCode){
            1000 -> {
                val selectedImageUri : Uri? = data?.data
                if(selectedImageUri != null){
                    userImage.setImageURI(selectedImageUri)
                } else{
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else -> {
            Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            2000 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    navigatePhotos()
                } else{
                    Toast.makeText(this, "권한을 거절하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkNickname() : String { // 닉네임이 유효한지 검사
        val nickname = editNickname.text.toString()
        if(!isValid(nickname)){ // 닉네임 형식이 유효하지 않으면
            return "Wrong"
        } else if(nickname.length < 2){
            return "Short"
        } else if(nickname.length > 12) {
            return "Long"
        } else {
            return "OK"
        }
    }

    private fun isValid(str : String) : Boolean{ // 닉네임이 한글, 영어, 숫자로만 이루어져있는지 확인
        var i = 0
        while(i < str.length){
            val c = str.codePointAt(i)
            if(c in 0xAC00..0xD800 || c in 0x0041..0x005A || c in 0x0061..0x007A || c in 0x0030..0x0039){
                i += Character.charCount(c)
            } else {
                return false
            }
        }
        return true
    }

    private fun inactivateButton(){
        completeButton.background = this.resources.getDrawable(R.drawable.signupbutton_background_gray)
        completeButton.setOnClickListener {  }
    }

    private fun activateButton(){
        completeButton.background = this.resources.getDrawable(R.drawable.signupbutton_background_orange)
        completeButton.setOnClickListener {
            // TODO 수정된 프로필 이미지와 닉네임을 서버로 전송
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean { // 현재 포커스된 뷰의 영역이 아닌 다른 곳을 클릭 시 키보드를 내리고 포커스를 해제한다.
        val focusView = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}