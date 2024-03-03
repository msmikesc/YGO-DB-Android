package com.example.ygodb.ui.fullscreendetails;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.ygodb.databinding.FragmentCardimagefullscreenBinding;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.constant.Const;

import java.io.InputStream;

public class ViewCardImageFullScreenFragment extends Fragment {

	private FragmentCardimagefullscreenBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentCardimagefullscreenBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		Bundle input = getArguments();

		int passcode = 0;
		if (input != null) {
			passcode = input.getInt(Const.PASSCODE);
		}

		if(passcode > 0) {
			ImageView imageView = binding.cardFullSize;

			try {
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("images/loading_icon.png");
				Drawable d = Drawable.createFromStream(ims, null);

				InputStream imsMissing = AndroidUtil.getAppContext().getAssets().open("pics/0.jpg");
				Drawable dMissing = Drawable.createFromStream(imsMissing, null);

				String imageUrl = Const.YGOPRO_API_IMAGES_FULLSIZE_BASE_URL + passcode + ".jpg";
				Glide.with(this).load(imageUrl).placeholder(d).error(dMissing).into(imageView);
			} catch (Exception ex) {
				binding.cardFullSize.setImageDrawable(null);
			}
		}
		else{
			try {
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/" + passcode + ".jpg");
				Drawable d = Drawable.createFromStream(ims, null);
				binding.cardFullSize.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardFullSize.setImageDrawable(null);
			}
		}

		return root;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}