package com.example.repository_pattern

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Context 인자가 필요하므로...
class MyViewModel(context: Context) : ViewModel() {

    private val repository = MyRepository(context) // repository 객체 생성
    // context를 주는 것은, DB 생성 시 context가 필수로 필요하기 때문

    val repos = repository.repos
    // repository가 제공하는 repos를 repos 속성에 그대로 연결
    // 라이브 데이터이기 때문에 repos도 똑같은걸 가리키게 됨

    init {
        refreshData()   // 생성자 부를 때 refreshData() 를 한 번은 호출하도록
    }

    // 사용자가 버튼을 누를 때 호출하도록 바꿀 수도 있음
    fun refreshData() {
        viewModelScope.launch {
            try {
                repository.refreshData() // repository 에게 데이터 새로 가져오도록 함
            } catch (e: Exception) {
                Log.e("Network","Failed to connect to the server!")
            }
        }
    }


    // MyViewModel이 생성자 인자가 있기 때문에 필요한 팩토리, context를 인자로 받음
    // ViewModel에 인자를 넣어 만들어 리턴해주는 역할
    // ViewModelProvider().get()을 통해 만들게 되는데
    // 클래스는 지정해주지만, 인자를 주지는 못함. 따라서 Factory 필요
    class Factory(val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // isAssignableFrom의 인자로 사용할 클래스만 지정해주면 됨
            if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyViewModel(context) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
