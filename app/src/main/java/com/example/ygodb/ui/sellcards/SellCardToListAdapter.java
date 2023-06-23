package com.example.ygodb.ui.sellcards;

import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class SellCardToListAdapter extends RecyclerView.Adapter<SellCardToListAdapter.ItemViewHolder> {
    private final List<OwnedCard> sellingOwnedCards;
    private final SellCardsViewModel sellCardsViewModel;
    private Drawable firstDrawableSmall;

    public SellCardToListAdapter(List<OwnedCard> ownedCards, SellCardsViewModel sellCardsViewModel) {
        this.sellingOwnedCards = ownedCards;
        this.sellCardsViewModel = sellCardsViewModel;

        try {
            InputStream firstInputStreamSmall = AndroidUtil.getAppContext().getAssets().open("images/1st.png");
            firstDrawableSmall = Drawable.createFromStream(firstInputStreamSmall, null);
        }
        catch (Exception e){
            YGOLogger.logException(e);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fragment_sellsinglecard, parent, false);

        return new ItemViewHolder(view);
    }

    public void onPlusButtonClick(ItemViewHolder viewHolder, OwnedCard current) {

        if(current.getSellQuantity() >= current.getQuantity()){
            current.setSellQuantity(current.getQuantity());
        }
        else {
            current.setSellQuantity(current.getSellQuantity() + 1);
        }
        viewHolder.cardQuantity.setText(String.valueOf(current.getSellQuantity()));
    }

    public void onMinusButtonClick(OwnedCard current) {
        current.setSellQuantity(current.getSellQuantity() - 1);

        if(current.getSellQuantity() < 1){
            sellCardsViewModel.removeNewFromOwnedCard(current);
        }

        this.notifyDataSetChanged();
    }

    public void onUpdatePrice(CharSequence priceBox, OwnedCard current, int position) {
        String newPrice = priceBox.toString();

        try{
            newPrice = Util.normalizePrice(newPrice);

            if(current.getPriceSold() != null && current.getPriceSold().equals(newPrice)){
                return;
            }

            current.setPriceSold(newPrice);
        }
        catch (Exception e){
            //price typed in is junk

        }
        try {
            notifyItemChanged(position);
        }
        catch (Exception e){
            //View disappeared or something with attempting to update
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder viewHolder, int position) {

        OwnedCard current = sellingOwnedCards.get(position);

        ImageButton button = viewHolder.itemView.findViewById(R.id.plusButton);
        button.setOnClickListener(view -> onPlusButtonClick(viewHolder, current));

        ImageButton button2 = viewHolder.itemView.findViewById(R.id.minusButton);
        button2.setOnClickListener(view -> onMinusButtonClick(current));

        viewHolder.title.setText(current.getCardName());

        String[] setNumbers = current.getSetNumber().split(", ");

        if(setNumbers.length == 1){
            viewHolder.setCode.setText(current.getSetNumber());
            viewHolder.setCode.setVisibility(View.VISIBLE);
            viewHolder.cardSetCodeDropdown.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.cardSetCodeDropdown.setVisibility(View.VISIBLE);
            viewHolder.setCode.setVisibility(View.INVISIBLE);

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>
					(viewHolder.itemView.getContext(),
							R.layout.spinner_text_red, setNumbers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewHolder.cardSetCodeDropdown.setAdapter(adapter);

            viewHolder.cardSetCodeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    current.setDropdownSelectedSetNumber(setNumbers[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(current.getDropdownSelectedSetNumber() == null){
                current.setDropdownSelectedSetNumber(setNumbers[0]);
            }
            else{
                boolean done = false;
                for(int i = 0; i < setNumbers.length; i++){
                    if(current.getDropdownSelectedSetNumber().equals(setNumbers[i])) {
                        viewHolder.cardSetCodeDropdown.setSelection(i);
                        done = true;
                        break;
                    }
                }
                if(!done){
                    current.setDropdownSelectedSetNumber(setNumbers[0]);
                }
            }

        }

        viewHolder.setCode.setText(current.getSetNumber());
        viewHolder.setName.setText(current.getSetName());

        String[] rarities = current.getSetRarity().split(", ");

        if(rarities.length == 1){
            viewHolder.cardRarity.setText(current.getSetRarity());
            viewHolder.cardRarity.setVisibility(View.VISIBLE);
            viewHolder.cardRarityDropdown.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.cardRarityDropdown.setVisibility(View.VISIBLE);
            viewHolder.cardRarity.setVisibility(View.INVISIBLE);

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>
					(viewHolder.itemView.getContext(),
							R.layout.spinner_text, rarities);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewHolder.cardRarityDropdown.setAdapter(adapter);

            viewHolder.cardRarityDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    current.setDropdownSelectedRarity(rarities[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(current.getDropdownSelectedRarity() == null){
                current.setDropdownSelectedRarity(rarities[0]);
            }
            else{
                boolean done = false;
                for(int i = 0; i < rarities.length; i++){
                    if(current.getDropdownSelectedRarity().equals(rarities[i])) {
                        viewHolder.cardRarityDropdown.setSelection(i);
                        done = true;
                        break;
                    }
                }
                if(!done){
                    current.setDropdownSelectedRarity(rarities[0]);
                }
            }
        }

        if(current.getPriceSold() != null) {
            double price = Double.parseDouble(current.getPriceSold());
            viewHolder.cardPrice.setText("$");
            viewHolder.cardPriceTextBox.setText(String.format(Locale.ROOT,"%.2f", price));
        }
        else{
            viewHolder.cardPrice.setText("$");
            viewHolder.cardPriceTextBox.setText(Const.ZERO_PRICE_STRING);
        }

        viewHolder.cardPriceTextBox.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                    || (actionId == EditorInfo.IME_ACTION_DONE)) {
                onUpdatePrice(v.getText(), current, viewHolder.getAdapterPosition());
            }
            return false;
        });

        viewHolder.cardPriceTextBox.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && v instanceof TextView textView) {
                onUpdatePrice(textView.getText(), current, viewHolder.getAdapterPosition());
            }
        });


        viewHolder.cardDate.setText(current.getDateBought());
        viewHolder.cardQuantity.setText(String.valueOf(current.getSellQuantity()));

        if(current.getEditionPrinting().contains("1st")){
            // set image to ImageView
            viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
        }
        else{
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
        return sellingOwnedCards.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView setCode;
        final TextView setName;
        final TextView cardRarity;
        final TextView cardPrice;
        final TextView cardDate;
        final TextView cardQuantity;
        final ImageView cardImage;
        final ImageView firstEdition;
        final EditText cardPriceTextBox;
        final Spinner cardRarityDropdown;
        final Spinner cardSetCodeDropdown;

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
            cardPriceTextBox = view.findViewById(R.id.cardPriceTextBox);
            cardRarityDropdown = view.findViewById(R.id.cardRarityDropdown);
            cardSetCodeDropdown = view.findViewById(R.id.cardSetCodeDropdown);

        }

    }

}
