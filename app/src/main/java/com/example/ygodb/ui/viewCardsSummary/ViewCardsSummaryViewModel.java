package com.example.ygodb.ui.viewCardsSummary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewCardsSummaryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ViewCardsSummaryViewModel() {
        mText = new MutableLiveData<>();

    }

    public LiveData<String> getText() {
        return mText;
    }
}