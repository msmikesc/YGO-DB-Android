package com.example.ygodb.abs;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextChangedListener<T> implements TextWatcher {
    private final T target;

    public TextChangedListener(T target) {
        this.target = target;
    }

    long delay = 500; // .5 seconds after user stops typing
    long last_text_edit = 0;

    TextChangedListener<T> objRef = null;

    Editable s = null;

    Handler handler = new Handler();

    private final Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                objRef.onTextChanged(target, s);
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //You need to remove this to run only once
        handler.removeCallbacks(input_finish_checker);
    }

    @Override
    public void afterTextChanged(Editable s) {
        last_text_edit = System.currentTimeMillis();
        this.s = s;
        objRef = this;
        handler.postDelayed(input_finish_checker, delay);
    }

    public abstract void onTextChanged(T target, Editable s);
}