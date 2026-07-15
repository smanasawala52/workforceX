package com.workforcex.worker.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.workforcex.shared.models.LoginRequest
import com.workforcex.shared.models.LoginResponse
import com.workforcex.shared.Result
import com.workforcex.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(mobile: String, password: String, countryCode: String) {
        _loginResult.value = Result.Loading
        RetrofitClient.get().login(LoginRequest(mobile, password, countryCode))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _loginResult.value = Result.Success(response.body()!!)
                    } else {
                        _loginResult.value = Result.Error("Invalid mobile number or password")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    _loginResult.value = Result.Error("Network error: " + t.message)
                }
            })
    }
}