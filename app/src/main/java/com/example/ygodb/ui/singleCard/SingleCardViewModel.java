package com.example.ygodb.ui.singleCard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SingleCardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SingleCardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("defaultText");
    }

    public void setText(String in) {
        mText.setValue(in);
    }

    public LiveData<String> getText() {
        return mText;
    }
}