package com.example.ygodb.ui.addCards;

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
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.utility.Util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class AddCardToListAdapter extends RecyclerView.Adapter<AddCardToListAdapter.ItemViewHolder> {
    private final ArrayList<OwnedCard> addingOwnedCards;

    private final AddCardsViewModel addCardsViewModel;

    private Drawable firstDrawable;
    private Drawable firstDrawableSmall;

    public AddCardToListAdapter(ArrayList<OwnedCard> ownedCards, AddCardsViewModel addCardsViewModel) {
        this.addingOwnedCards = ownedCards;
        this.addCardsViewModel = addCardsViewModel;

        try {
            // get input stream
            InputStream firstInputStream = AndroidUtil.getAppContext().getAssets().open("images/1st.png");
            // load image as Drawable
            firstDrawable = Drawable.createFromStream(firstInputStream, null);

            InputStream firstInputStreamSmall = AndroidUtil.getAppContext().getAssets().open("images/1st.png");
            firstDrawableSmall = Drawable.createFromStream(firstInputStreamSmall, null);
        }
        catch (Exception e){
            e.printStackTrace();
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
        current.quantity++;
        viewHolder.cardQuantity.setText(String.valueOf(current.quantity));
    }

    public void onMinusButtonClick(ItemViewHolder viewHolder, OwnedCard current) {
        current.quantity--;

        if(current.quantity < 1){
            addCardsViewModel.removeNewFromOwnedCard(current);
        }

        this.notifyDataSetChanged();
    }

    public void onFirstButtonClick(ItemViewHolder viewHolder, OwnedCard current) {

        if(current.editionPrinting!= null && current.editionPrinting.contains("1st")){
            current.editionPrinting = "Unlimited";
            viewHolder.firstEdition.setImageDrawable(null);
        }
        else{
            current.editionPrinting = "1st Edition";
            viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
        }

        viewHolder.cardQuantity.setText(String.valueOf(current.quantity));
    }

    public void onUpdatePrice(CharSequence priceBox, OwnedCard current, int position) {
        String newPrice = priceBox.toString();

        try{
            newPrice = Util.normalizePrice(newPrice);

            if(current.priceBought != null && current.priceBought.equals(newPrice)){
                return;
            }

            current.priceBought = newPrice;
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlusButtonClick(viewHolder, current);
            }
        });

        ImageButton button2 = viewHolder.itemView.findViewById(R.id.minusButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMinusButtonClick(viewHolder, current);
            }
        });

        ImageButton button3 = viewHolder.itemView.findViewById(R.id.firstButton);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFirstButtonClick(viewHolder, current);
            }
        });

        viewHolder.title.setText(current.cardName);

        String[] setNumbers = current.setNumber.split(", ");

        if(setNumbers.length == 1){
            viewHolder.setCode.setText(current.setNumber);
            viewHolder.setCode.setVisibility(View.VISIBLE);
            viewHolder.cardSetCodeDropdown.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.cardSetCodeDropdown.setVisibility(View.VISIBLE);
            viewHolder.setCode.setVisibility(View.INVISIBLE);

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>
                    (viewHolder.itemView.getContext(),
                            R.layout.spinner_text_red, setNumbers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewHolder.cardSetCodeDropdown.setAdapter(adapter);

            viewHolder.cardSetCodeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    current.dropdownSelectedSetNumber = setNumbers[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(current.dropdownSelectedSetNumber == null){
                current.dropdownSelectedSetNumber = setNumbers[0];
            }
            else{
                boolean done = false;
                for(int i = 0; i < setNumbers.length; i++){
                    if(current.dropdownSelectedSetNumber.equals(setNumbers[i])) {
                        viewHolder.cardSetCodeDropdown.setSelection(i);
                        done = true;
                        break;
                    }
                }
                if(!done){
                    current.dropdownSelectedSetNumber = setNumbers[0];
                }
            }

        }

        viewHolder.setCode.setText(current.setNumber);
        viewHolder.setName.setText(current.setName);

        String[] rarities = current.setRarity.split(", ");

        if(rarities.length == 1){
            viewHolder.cardRarity.setText(current.setRarity);
            viewHolder.cardRarity.setVisibility(View.VISIBLE);
            viewHolder.cardRarityDropdown.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.cardRarityDropdown.setVisibility(View.VISIBLE);
            viewHolder.cardRarity.setVisibility(View.INVISIBLE);

            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>
                    (viewHolder.itemView.getContext(),
                            R.layout.spinner_text, rarities);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            viewHolder.cardRarityDropdown.setAdapter(adapter);

            viewHolder.cardRarityDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    current.dropdownSelectedRarity = rarities[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(current.dropdownSelectedRarity == null){
                current.dropdownSelectedRarity = rarities[0];
            }
            else{
                boolean done = false;
                for(int i = 0; i < rarities.length; i++){
                    if(current.dropdownSelectedRarity.equals(rarities[i])) {
                        viewHolder.cardRarityDropdown.setSelection(i);
                        done = true;
                        break;
                    }
                }
                if(!done){
                    current.dropdownSelectedRarity = rarities[0];
                }
            }
        }

        if(current.priceBought != null) {
            double price = Double.parseDouble(current.priceBought);
            viewHolder.cardPrice.setText("$");
            viewHolder.cardPriceTextBox.setText(String.format(Locale.ROOT, "%.2f", price));
        }
        else{
            viewHolder.cardPrice.setText("$");
            viewHolder.cardPriceTextBox.setText("0.00");
        }

        viewHolder.cardPriceTextBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onUpdatePrice(v.getText(), current, viewHolder.getAdapterPosition());
                }
                return false;
            }
        });

        viewHolder.cardPriceTextBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if(v instanceof TextView textView) {
                        onUpdatePrice(textView.getText(), current, viewHolder.getAdapterPosition());
                    }
                }
            }
        });


        viewHolder.cardDate.setText(current.dateBought);
        viewHolder.cardQuantity.setText(String.valueOf(current.quantity));

        button3.setImageDrawable(firstDrawable);

        if(current.editionPrinting.contains("1st")){
            // set image to ImageView
            viewHolder.firstEdition.setImageDrawable(firstDrawableSmall);
        }
        else{
            viewHolder.firstEdition.setImageDrawable(null);
        }

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
        return addingOwnedCards.size();
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
        EditText cardPriceTextBox;
        Spinner cardRarityDropdown;
        Spinner cardSetCodeDropdown;

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
