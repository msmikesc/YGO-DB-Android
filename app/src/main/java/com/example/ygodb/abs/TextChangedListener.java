package com.example.ygodb.abs;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextChangedListener<T> implements TextWatcher {
    private final T target;

    protected TextChangedListener(T target) {
        this.target = target;
    }

    private static final long DELAY = 500; // .5 seconds after user stops typing
    private long lastTextEdit = 0;

    TextChangedListener<T> objRef = null;

    Editable s = null;

    protected Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable inputFinishChecker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (lastTextEdit + DELAY - 500)) {
                objRef.onTextChanged(target, s);
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //You need to remove this to run only once
        handler.removeCallbacks(inputFinishChecker);
    }

    @Override
    public void afterTextChanged(Editable s) {
        lastTextEdit = System.currentTimeMillis();
        this.s = s;
        objRef = this;
        handler.postDelayed(inputFinishChecker, DELAY);
    }

    public abstract void onTextChanged(T target, Editable s);
}