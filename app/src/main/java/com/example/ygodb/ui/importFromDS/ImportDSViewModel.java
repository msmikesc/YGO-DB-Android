package com.example.ygodb.ui.importFromDS;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ImportDSViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ImportDSViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is ImportDSViewModel fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}