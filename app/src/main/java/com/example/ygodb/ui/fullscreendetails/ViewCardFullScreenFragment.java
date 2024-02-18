package com.example.ygodb.ui.fullscreendetails;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ygodb.R;
import com.example.ygodb.databinding.FragmentCardfullscreenBinding;
import com.example.ygodb.databinding.FragmentViewcardsSummaryBinding;
import com.example.ygodb.model.EndlessScrollListener;
import com.example.ygodb.model.partialscroll.PartialScrollEndlessScrollListener;
import com.example.ygodb.model.partialscroll.PartialScrollSearchBarChangedListener;
import com.example.ygodb.model.partialscroll.PartialScrollSortButtonOnClickListener;
import com.example.ygodb.ui.viewcards.ViewCardsViewModel;
import com.example.ygodb.ui.viewcardssummary.SummaryCardToListAdapter;
import com.example.ygodb.ui.viewcardssummary.ViewCardsSummaryViewModel;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ViewCardFullScreenFragment extends Fragment {

	private FragmentCardfullscreenBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentCardfullscreenBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		Bundle input = getArguments();

		OwnedCard current = (OwnedCard) input.getSerializable("OwnedCard");

		if(current != null) {

			binding.cardTitle.setText(current.getCardName());
			//binding.cardPasscode.setText(current.getPasscode());

			int imagePasscode = current.getPasscode();

			if (current.getAltArtPasscode() != null && current.getAltArtPasscode() != 0) {
				imagePasscode = current.getAltArtPasscode();
			}

			try {
				// get input stream
				InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/" + imagePasscode + ".jpg");
				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);
				// set image to ImageView
				binding.cardImage.setImageDrawable(d);
			} catch (Exception ex) {
				binding.cardImage.setImageDrawable(null);
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