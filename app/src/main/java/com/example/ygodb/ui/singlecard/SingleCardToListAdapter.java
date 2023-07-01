package com.example.ygodb.ui.singlecard;

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
import com.example.ygodb.ui.addcards.AddCardsViewModel;
import com.example.ygodb.ui.sellcards.SellCardsViewModel;
import ygodb.commonlibrary.bean.OwnedCard;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SingleCardToListAdapter extends RecyclerView.Adapter<SingleCardToListAdapter.ItemViewHolder> {
    private List<OwnedCard> ownedCards;

    private final AddCardsViewModel addCardsViewModel;
    private final SellCardsViewModel sellCardsViewModel;

    public SingleCardToListAdapter(List<OwnedCard> ownedCards,
           AddCardsViewModel addCardsViewModel, SellCardsViewModel sellCardsViewModel) {
        this.ownedCards = ownedCards;
        this.addCardsViewModel = addCardsViewModel;
        this.sellCardsViewModel = sellCardsViewModel;
    }

    public void setOwnedCards(List<OwnedCard> ownedCards) {
        this.ownedCards = ownedCards;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fragment_singlecard, parent, false);

        return new ItemViewHolder(view);
    }

    public void onPlusButtonClick(OwnedCard current) {
        if(addCardsViewModel != null) {
            addCardsViewModel.addNewFromOwnedCard(current);
        }
    }

    public void onSellButtonClick(OwnedCard current) {
        if(sellCardsViewModel != null) {
            sellCardsViewModel.addNewFromOwnedCard(current);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

        OwnedCard current = ownedCards.get(position);

        if(addCardsViewModel != null) {
            ImageButton button = viewHolder.itemView.findViewById(R.id.plusButton);
            button.setOnClickListener(view -> onPlusButtonClick(current));
        }

        if(sellCardsViewModel != null){
            ImageButton sellButton = viewHolder.itemView.findViewById(R.id.sellButton);
            sellButton.setOnClickListener(view -> onSellButtonClick(current));
        }

        viewHolder.title.setText(current.getCardName());

        String setRarityText = current.getSetRarity();

        if(current.getColorVariant() != null && !current.getColorVariant().equals("") && !current.getColorVariant().equals("-1")){
            if(current.getColorVariant().equalsIgnoreCase("a")){
                setRarityText = "Alt Art " + setRarityText;
                viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Gold));
            }
            else{
                setRarityText = current.getColorVariant().toUpperCase(Locale.ROOT) + " " + setRarityText;
                switch (current.getColorVariant().toUpperCase(Locale.ROOT)) {
                    case "R" ->
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.Crimson));
                    case "G" ->
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.LimeGreen));
                    case "B" ->
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.DeepSkyBlue));
                    case "P" ->
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.BlueViolet));
                    default ->
                            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White));
                }
            }
        }
        else{
            viewHolder.title.setTextColor(ContextCompat.getColor(AndroidUtil.getAppContext(), R.color.White));
        }

        viewHolder.setCode.setText(current.getSetNumber());
        viewHolder.cardRarity.setText(setRarityText);
        viewHolder.setCode.setVisibility(View.VISIBLE);
        viewHolder.cardRarity.setVisibility(View.VISIBLE);
        viewHolder.setName.setMaxLines(2);

        if(addCardsViewModel == null) {
            viewHolder.plusButton.setVisibility(View.GONE);
        }
        else{
            viewHolder.plusButton.setVisibility(View.VISIBLE);
        }

        if(sellCardsViewModel == null){
            viewHolder.sellButton.setVisibility(View.GONE);
        }
        else{
            viewHolder.sellButton.setVisibility(View.VISIBLE);
        }

        if(current.getMultiListSetNames() == null || current.getMultiListSetNames().equals("")){
            viewHolder.setName.setText(current.getSetName());
        }
        else{
            viewHolder.setName.setText(current.getMultiListSetNames());
        }

        if(current.getPriceBought() != null) {
            double price = Double.parseDouble(current.getPriceBought());
            viewHolder.cardPrice.setText("$" + String.format(Locale.ROOT,"%.2f", price));
        }
        viewHolder.cardDate.setText(current.getDateBought());

        viewHolder.cardQuantity.setText(String.valueOf(current.getQuantity()));

        try {
            if(current.getEditionPrinting().contains("1st")){
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
            InputStream ims = AndroidUtil.getAppContext().getAssets().open("pics/"+ current.getPasscode() + ".jpg");
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
