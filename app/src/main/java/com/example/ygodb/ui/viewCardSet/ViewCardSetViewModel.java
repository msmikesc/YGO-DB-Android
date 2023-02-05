package com.example.ygodb.ui.viewCardSet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewCardSetViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ViewCardSetViewModel() {
        mText = new MutableLiveData<>();

    }

    public LiveData<String> getText() {
        return mText;
    }
}