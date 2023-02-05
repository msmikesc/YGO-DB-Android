package com.example.ygodb.ui.viewCards;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewCardsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ViewCardsViewModel() {
        mText = new MutableLiveData<>();

    }

    public LiveData<String> getText() {
        return mText;
    }
}