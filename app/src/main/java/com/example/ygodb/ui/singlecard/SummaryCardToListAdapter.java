package com.example.ygodb.ui.singlecard;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SummaryCardToListAdapter extends RecyclerView.Adapter<SummaryCardToListAdapter.ItemViewHolder> {
    List<OwnedCard> ownedCards;

    public SummaryCardToListAdapter(List<OwnedCard> ownedCards) {
        this.ownedCards = ownedCards;
    }

    public void setOwnedCards(List<OwnedCard> ownedCards) {
        this.ownedCards = ownedCards;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fragment_summarycard, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

        OwnedCard current = ownedCards.get(position);

        viewHolder.title.setText(current.cardName);
        viewHolder.setName.setText(current.setName);
        viewHolder.cardQuantity.setText(String.valueOf(current.quantity));
        if(current.priceBought != null) {
            double price = Double.parseDouble(current.priceBought);
            viewHolder.cardPrice.setText("$" + String.format(Locale.ROOT,"%.2f", price));
        }
        viewHolder.cardDate.setText(current.dateBought);
        viewHolder.rarity.setText(current.setRarity);

        try {
            // get input stream
            InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/"+current.passcode+ ".jpg");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            viewHolder.cardImage.setImageDrawable(d);
        }
        catch(Exception ex) {
            viewHolder.cardImage.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return ownedCards.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView setName;
        TextView cardQuantity;
        ImageView cardImage;
        TextView cardPrice;
        TextView cardDate;
        TextView rarity;

        public ItemViewHolder(@NonNull View view) {
            super(view);

            title = view.findViewById(R.id.cardTitle);
            setName = view.findViewById(R.id.cardSetName);
            cardQuantity = view.findViewById(R.id.cardQuantity);
            cardPrice = view.findViewById(R.id.cardPrice);
            cardDate = view.findViewById(R.id.cardDateBought);
            cardImage = view.findViewById(R.id.cardImage);
            rarity = view.findViewById(R.id.cardRarity);

        }

    }

}
