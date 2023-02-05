package com.example.ygodb.ui.singleCard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;

import com.example.ygodb.backend.bean.OwnedCard;

import java.io.InputStream;
import java.util.ArrayList;

public class SingleCardToListAdapter extends RecyclerView.Adapter<SingleCardToListAdapter.ItemViewHolder> {
    Context context;

    ArrayList<OwnedCard> ownedCards;

    LayoutInflater inflater;

    public SingleCardToListAdapter(Context applicationContext, ArrayList<OwnedCard> ownedCards) {
        this.context = applicationContext;
        this.ownedCards = ownedCards;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fragment_singlecard, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

        OwnedCard current = ownedCards.get(position);

        viewHolder.title.setText(current.cardName);
        viewHolder.setCode.setText(current.setNumber);
        viewHolder.setName.setText(current.setName);
        viewHolder.cardRarity.setText(current.setRarity);
        if(current.priceBought != null) {
            double price = Double.parseDouble(current.priceBought);
            viewHolder.cardPrice.setText("$" + String.format("%.2f", price));
        }
        viewHolder.cardDate.setText(current.dateBought);
        viewHolder.cardQuantity.setText(current.quantity + "");

        try {
            // get input stream
            InputStream ims = context.getAssets().open("pics/"+current.id+ ".jpg");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView

            viewHolder.cardImage.setImageDrawable(d);
        }
        catch(Exception ex) {
            viewHolder.cardImage.setImageDrawable(null);

            return;
        }

    }

    @Override
    public int getItemCount() {
        return ownedCards.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView setCode;
        TextView setName;
        TextView cardRarity;
        TextView cardPrice;
        TextView cardDate;
        TextView cardQuantity;

        ImageView cardImage;

        public ItemViewHolder(@NonNull View view) {
            super(view);

            title = view.findViewById(R.id.cardTitle);
            setCode = view.findViewById(R.id.cardSetCode);
            setName = view.findViewById(R.id.cardSetName);
            cardRarity = view.findViewById(R.id.cardRarity);
            cardPrice = view.findViewById(R.id.cardPrice);
            cardDate = view.findViewById(R.id.cardDateBought);
            cardQuantity = view.findViewById(R.id.cardQuantity);
            cardImage = view.findViewById(R.id.cardImage);

        }

    }

}
