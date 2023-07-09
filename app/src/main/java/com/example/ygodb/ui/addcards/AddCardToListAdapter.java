package com.example.ygodb.ui.addcards;

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
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class AddCardToListAdapter extends RecyclerView.Adapter<AddCardToListAdapter.ItemViewHolder> {
    private final List<OwnedCard> addingOwnedCards;

    private final AddCardsViewModel addCardsViewModel;

    private Drawable firstDrawable;
    private Drawable firstDrawableSmall;
    private Drawable limitedDrawableSmall;

    public AddCardToListAdapter(List<OwnedCard> ownedCards, AddCardsViewModel addCardsViewModel) {
        this.addingOwnedCards = ownedCards;
        this.addCardsViewModel = addCardsViewModel;

        try {
            // get input stream
            InputStream firstInputStream = AndroidUtil.getAppContext().getAssets().open("images/1st.png");
            // load image as Drawable
            firstDrawable = Drawable.createFromStream(firstInputStream, null);

            InputStream firstInputStreamSmall = AndroidUtil.getAppContext().getAssets().open("images/1st.png");
            firstDrawableSmall = Drawable.createFromStream(firstInputStreamSmall, null);

            InputStream limitedInputStreamSmall = AndroidUtil.getAppContext().getAssets().open("images/Limited.png");
            limitedDrawableSmall = Drawable.createFromStream(limitedInputStreamSmall, null);
        }
        catch (Exception e){
            YGOLogger.logException(e);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fragment_addsinglecard, parent, false);

        return new ItemViewHolder(view);
    }

    public void onPlusButtonClick(ItemViewHolder viewHolder, OwnedCard current) {
        current.setQuantity(current.getQuantity() + 1);
        viewHolder.cardQuantity.setText(String.valueOf(current.getQuantity()));
    }

    public void onMinusButtonClick(OwnedCard current) {
        current.setQuantity(current.getQuantity() - 1);

        if(current.getQuantity() < 1){
            addCardsViewModel.removeNewFromOwnedCard(current);
        }

        this.notifyDataSetChanged();
    }

    public void onFirstButtonClick(ItemViewHolder viewHolder, OwnedCard current) {

        if(current.getEditionPrinting() != null && current.getEditionPrinting().contains("1st")){
            current.setEditionPrinting(Const.CARD_PRINTING_LIMITED);
            viewHolder.firstEdition.setImageDrawable(limitedDrawableSmall);
        }
        else if(current.getEditionPrinting() != null
                && current.getEditionPrinting().equalsIgnoreCase(Const.CARD_PRINTING_LIMITED)) {
            current.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
            viewHolder.firstEdition.setImageDrawable(null);
        }
        else{
            current.setEditionPrinting(Const.CARD_PRINTING_FIRST_EDITION);
            viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
        }

        viewHolder.cardQuantity.setText(String.valueOf(current.getQuantity()));
    }

    public void onUpdatePrice(CharSequence priceBox, OwnedCard current, int position) {
        String newPrice = priceBox.toString();

        try{
            newPrice = Util.normalizePrice(newPrice);

            if(current.getPriceBought() != null && current.getPriceBought().equals(newPrice)){
                return;
            }

            current.setPriceBought(newPrice);
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

        OwnedCard current = addingOwnedCards.get(position);

        ImageButton button = viewHolder.itemView.findViewById(R.id.plusButton);
        button.setOnClickListener(view -> onPlusButtonClick(viewHolder, current));

        ImageButton button2 = viewHolder.itemView.findViewById(R.id.minusButton);
        button2.setOnClickListener(view -> onMinusButtonClick(current));

        ImageButton button3 = viewHolder.itemView.findViewById(R.id.firstButton);
        button3.setOnClickListener(view -> onFirstButtonClick(viewHolder, current));

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

        List<String> setNames = current.getSetNamesOptions();

        if(setNames == null){
            viewHolder.setCode.setText(current.getSetNumber());
            viewHolder.setName.setText(current.getSetName());
            viewHolder.cardSetNameDropdown.setVisibility(View.INVISIBLE);
            viewHolder.setName.setVisibility(View.VISIBLE);
        }
        else if(setNames.size() == 1){
            viewHolder.setCode.setText(current.getSetNumber());
            viewHolder.setName.setText(setNames.get(0));
            viewHolder.cardSetNameDropdown.setVisibility(View.INVISIBLE);
            viewHolder.setName.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.cardSetNameDropdown.setVisibility(View.VISIBLE);
            viewHolder.setName.setVisibility(View.INVISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>
                    (viewHolder.itemView.getContext(),
                            R.layout.spinner_text, setNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewHolder.cardSetNameDropdown.setAdapter(adapter);

            viewHolder.cardSetNameDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    current.setDropdownSelectedSetName(setNames.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(current.getDropdownSelectedSetName() == null){
                current.setDropdownSelectedSetName(setNames.get(0));
            }
            else{
                boolean done = false;
                for(int i = 0; i < setNames.size(); i++){
                    if(current.getDropdownSelectedSetName().equals(setNames.get(i))) {
                        viewHolder.cardSetNameDropdown.setSelection(i);
                        done = true;
                        break;
                    }
                }
                if(!done){
                    current.setDropdownSelectedSetName(setNames.get(0));
                }
            }
        }

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

        if(current.getPriceBought() != null) {
            double price = Double.parseDouble(current.getPriceBought());
            viewHolder.cardPrice.setText("$");
            viewHolder.cardPriceTextBox.setText(String.format(Locale.ROOT, "%.2f", price));
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
        viewHolder.cardQuantity.setText(String.valueOf(current.getQuantity()));

        button3.setImageDrawable(firstDrawable);

        if(current.getEditionPrinting().contains("1st")){
            // set image to ImageView
            viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
        }
        else if(current.getEditionPrinting().equalsIgnoreCase(Const.CARD_PRINTING_LIMITED)){
            viewHolder.firstEdition.setImageDrawable(limitedDrawableSmall);
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
        return addingOwnedCards.size();
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
        final Spinner cardSetNameDropdown;

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
            cardSetNameDropdown = view.findViewById(R.id.cardSetNameDropdown);

        }

    }

}
