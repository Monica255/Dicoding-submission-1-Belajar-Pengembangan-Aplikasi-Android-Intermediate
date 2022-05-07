package com.example.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.dataclass.RequestLogin
import com.example.storyapp.dataclass.ResponseLogin
import com.example.storyapp.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Response


class MainViewModel : ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _userLogin = MutableLiveData<ResponseLogin>()
    val userlogin: LiveData<ResponseLogin> = _userLogin
    var isError: Boolean = false

    fun getResponseLogin(requestLogin: RequestLogin) {
        _isLoading.value = true
        val api = ApiConfig.getApiService().fetchUser(requestLogin)
        api.enqueue(object : retrofit2.Callback<ResponseLogin> {
            override fun onResponse(call: Call<ResponseLogin>, response: Response<ResponseLogin>) {
                _isLoading.value = false
                val responseBody = response.body()

                if (response.isSuccessful) {
                    isError = false
                    _userLogin.value = responseBody!!
                    _message.value = "Login as ${_userLogin.value!!.loginResult.name}"
                } else {
                    isError = true
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                isError = true
                _isLoading.value = false
                _message.value=t.message.toString()
            }

        })
    }
}