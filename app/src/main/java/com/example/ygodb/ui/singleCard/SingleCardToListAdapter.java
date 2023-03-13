package com.example.ygodb.ui.singleCard;

import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.abs.Util;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.ui.addCards.AddCardsViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;
import java.util.ArrayList;

public class SingleCardToListAdapter extends RecyclerView.Adapter<SingleCardToListAdapter.ItemViewHolder> {
    private ArrayList<OwnedCard> ownedCards;

    private AddCardsViewModel addCardsViewModel;

    public SingleCardToListAdapter(ArrayList<OwnedCard> ownedCards, AddCardsViewModel addCardsViewModel) {
        this.ownedCards = ownedCards;
        this.addCardsViewModel = addCardsViewModel;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fragment_singlecard, parent, false);

        return new ItemViewHolder(view);
    }

    public void onPlusButtonClick(View view, OwnedCard current) {
        addCardsViewModel.addNewFromOwnedCard(current);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

        OwnedCard current = ownedCards.get(position);

        ImageButton button = viewHolder.itemView.findViewById(R.id.plusButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlusButtonClick(view, current);
            }
        });

        viewHolder.title.setText(current.cardName);

        if(current.setNumber == null || current.setNumber.trim().equals("")) {
            viewHolder.setCode.setVisibility(View.GONE);
            viewHolder.plusButton.setVisibility(View.GONE);
            viewHolder.cardRarity.setVisibility(View.GONE);
            viewHolder.setName.setMaxLines(3);
        }
        else{
            viewHolder.setCode.setText(current.setNumber);
            viewHolder.cardRarity.setText(current.setRarity);
            viewHolder.setCode.setVisibility(View.VISIBLE);
            viewHolder.cardRarity.setVisibility(View.VISIBLE);
            viewHolder.plusButton.setVisibility(View.VISIBLE);
            viewHolder.setName.setMaxLines(2);
        }

        if(current.multiListSetNames == null || current.multiListSetNames.equals("")){
            viewHolder.setName.setText(current.setName);
        }
        else{
            viewHolder.setName.setText(current.multiListSetNames);
        }

        if(current.priceBought != null) {
            double price = Double.parseDouble(current.priceBought);
            viewHolder.cardPrice.setText("$" + String.format("%.2f", price));
        }
        viewHolder.cardDate.setText(current.dateBought);

        viewHolder.cardQuantity.setText(current.quantity + "");

        try {
            if(current.editionPrinting.contains("1st")){
                // get input stream
                InputStream ims = Util.getAppContext().getAssets().open("images/1st.png");
                // load image as Drawable
                Drawable d = Drawable.createFromStream(ims, null);
                // set image to ImageView

                viewHolder.firstEdition.setImageDrawable(d);
            }
            else{
                viewHolder.firstEdition.setImageDrawable(null);
            }
        }
        catch(Exception ex) {
            viewHolder.firstEdition.setImageDrawable(null);
        }

        try {
            // get input stream
            InputStream ims = Util.getAppContext().getAssets().open("pics/"+current.id+ ".jpg");
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
        TextView setCode;
        TextView setName;
        TextView cardRarity;
        TextView cardPrice;
        TextView cardDate;
        TextView cardQuantity;
        ImageView cardImage;
        ImageView firstEdition;
        ImageButton plusButton;

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
            firstEdition = view.findViewById(R.id.firststEditionIcon);
            plusButton = view.findViewById(R.id.plusButton);

        }

    }

}
