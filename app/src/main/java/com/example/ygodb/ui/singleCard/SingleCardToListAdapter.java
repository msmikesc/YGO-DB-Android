package com.example.ygodb.ui.singleCard;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonLibrary.bean.OwnedCard;
import com.example.ygodb.ui.addCards.AddCardsViewModel;
import com.example.ygodb.ui.sellCards.SellCardsViewModel;

import java.io.InputStream;
import java.util.ArrayList;

public class SingleCardToListAdapter extends RecyclerView.Adapter<SingleCardToListAdapter.ItemViewHolder> {
    private ArrayList<OwnedCard> ownedCards;

    private AddCardsViewModel addCardsViewModel;
    private SellCardsViewModel sellCardsViewModel;

    public SingleCardToListAdapter(ArrayList<OwnedCard> ownedCards,
           AddCardsViewModel addCardsViewModel, SellCardsViewModel sellCardsViewModel) {
        this.ownedCards = ownedCards;
        this.addCardsViewModel = addCardsViewModel;
        this.sellCardsViewModel = sellCardsViewModel;
    }

    public void setOwnedCards(ArrayList<OwnedCard> ownedCards) {
        this.ownedCards = ownedCards;
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

    public void onSellButtonClick(View view, OwnedCard current) {
        if(sellCardsViewModel != null) {
            sellCardsViewModel.addNewFromOwnedCard(current);
        }
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

        if(sellCardsViewModel != null){
            ImageButton sellButton = viewHolder.itemView.findViewById(R.id.sellButton);
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSellButtonClick(view, current);
                }
            });
        }

        viewHolder.title.setText(current.cardName);

        if(current.setNumber == null || current.setNumber.trim().equals("")) {
            viewHolder.setCode.setVisibility(View.GONE);
            viewHolder.plusButton.setVisibility(View.GONE);
            viewHolder.cardRarity.setVisibility(View.GONE);
            viewHolder.setName.setMaxLines(3);
        }
        else{

            String setRarityText = current.setRarity;

            if(current.colorVariant != null && !current.colorVariant.equals("") && !current.colorVariant.equals("-1")){
                if(current.colorVariant.equalsIgnoreCase("a")){
                    setRarityText = "Alt Art " + setRarityText;
                    viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Gold));
                }
                else{
                    setRarityText = current.colorVariant.toUpperCase() + " " + setRarityText;
                    switch(current.colorVariant.toUpperCase()){
                        case "R":
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Crimson));
                            break;
                        case "G":
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.LimeGreen));
                            break;
                        case "B":
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.DeepSkyBlue));
                            break;
                        case "P":
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.BlueViolet));
                            break;
                        default:
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White));
                    }
                }
            }
            else{
                viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White));
            }

            viewHolder.setCode.setText(current.setNumber);
            viewHolder.cardRarity.setText(setRarityText);
            viewHolder.setCode.setVisibility(View.VISIBLE);
            viewHolder.cardRarity.setVisibility(View.VISIBLE);
            viewHolder.plusButton.setVisibility(View.VISIBLE);
            viewHolder.setName.setMaxLines(2);
        }

        if(sellCardsViewModel == null){
            viewHolder.sellButton.setVisibility(View.GONE);
        }
        else{
            viewHolder.sellButton.setVisibility(View.VISIBLE);
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
                InputStream ims = AndroidUtil.getAppContext().getAssets().open("images/1st.png");
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
            InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/"+current.id+ ".jpg");
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

        ImageButton sellButton;

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
            sellButton = view.findViewById(R.id.sellButton);

        }

    }

}
