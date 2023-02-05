package com.example.ygodb.ui.viewCardSet;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.abs.OwnedCardNameComparator;
import com.example.ygodb.abs.OwnedCardPriceComparator;
import com.example.ygodb.abs.OwnedCardQuantityComparator;
import com.example.ygodb.abs.OwnedCardSetNumberComparator;
import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.backend.analyze.AnalyzeCardsInSet;
import com.example.ygodb.backend.bean.AnalyzeData;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.databinding.FragmentViewcardsBinding;
import com.example.ygodb.databinding.FragmentViewcardsetBinding;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewCardSetFragment extends Fragment {

    private FragmentViewcardsetBinding binding;

    private ArrayList<OwnedCard> cardsList;

    private ArrayList<OwnedCard> filteredCardsList;

    private static String sortOption = null;
    private static String cardNameSearch = null;
    private static String setNameSearch = null;

    private static String[] setNamesDropdownList = null;

    private static Comparator<OwnedCard> currentComparator = null;

    private static LinearLayoutManager layout = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewCardSetViewModel viewCardSetViewModel =
                new ViewModelProvider(this).get(ViewCardSetViewModel.class);

        binding = FragmentViewcardsetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        sortOption = "Quantity";
        currentComparator = new OwnedCardQuantityComparator();

        try {
            cardsList = loadInitialData(setNameSearch);
            filteredCardsList = new ArrayList<>();
            filteredCardsList.addAll(cardsList);

            ArrayList<String> setNamesArrayList = SQLiteConnection.getObj().getDistinctSetNames();

            setNamesDropdownList = new String[setNamesArrayList.size()];

            setNamesArrayList.toArray(setNamesDropdownList);

        } catch (Exception e) {
            e.printStackTrace();
            cardsList = new ArrayList<>();
            filteredCardsList = new ArrayList<>();
            setNamesDropdownList = new String[0];
        }

        ArrayAdapter<String> autoCompleteAdapter=
                new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line,
                        setNamesDropdownList);
        AutoCompleteTextView textView=(AutoCompleteTextView)root.findViewById(R.id.setSearch);
        textView.setThreshold(3);
        textView.setAdapter(autoCompleteAdapter);

        SingleCardToListAdapter adapter = new SingleCardToListAdapter(getContext(), filteredCardsList);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(getContext(), binding.fab);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.sort_menu_set_list, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        if(!sortOption.equals(menuItem.getTitle())) {
                            sortOption = (String)menuItem.getTitle();

                            if(sortOption.equals("Quantity")){
                                currentComparator = new OwnedCardQuantityComparator();
                            }
                            else if(sortOption.equals("Card Name")){
                                currentComparator = new OwnedCardNameComparator();
                            }
                            else if(sortOption.equals("Set Number")){
                                currentComparator = new OwnedCardSetNumberComparator();
                            }
                            else if(sortOption.equals("Price")){
                                currentComparator = new OwnedCardPriceComparator();
                            }

                            sortData(filteredCardsList, currentComparator);

                            layout.scrollToPositionWithOffset(0, 0);

                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                        return true;
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });

        binding.cardSearch.addTextChangedListener(new TextChangedListener<EditText>(binding.cardSearch) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                cardNameSearch = s.toString().toUpperCase();

                filteredCardsList.clear();

                for(OwnedCard current: cardsList){
                    if(cardNameSearch.equals("") || current.cardName.toUpperCase().contains(cardNameSearch)){
                        filteredCardsList.add(current);
                    }
                }

                sortData(filteredCardsList, currentComparator);

                layout.scrollToPositionWithOffset(0, 0);

                adapter.notifyDataSetChanged();
            }
        });

        binding.setSearch.addTextChangedListener(new TextChangedListener<EditText>(binding.setSearch) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                setNameSearch = s.toString();

                cardsList.clear();
                filteredCardsList.clear();

                cardsList.addAll(loadInitialData(setNameSearch));

                for(OwnedCard current: cardsList){
                    if(cardNameSearch == null ||cardNameSearch.equals("") || current.cardName.toUpperCase().contains(cardNameSearch.toUpperCase())){
                        filteredCardsList.add(current);
                    }
                }

                layout.scrollToPositionWithOffset(0, 0);

                adapter.notifyDataSetChanged();
            }
        });

        return root;
    }

    public void sortData(ArrayList<OwnedCard> cardsList, Comparator<OwnedCard> currentComparator){
        Collections.sort(cardsList, currentComparator);
    }

    public ArrayList<OwnedCard> loadInitialData(String setName) {

        AnalyzeCardsInSet runner = new AnalyzeCardsInSet();

        ArrayList<AnalyzeData> results = null;
        ArrayList<OwnedCard> newList = new ArrayList<>();

        if(setName == null || setName.equals("")){
            return newList;
        }

        try {
             results = runner.runFor(setName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(AnalyzeData current: results){
            OwnedCard currentCard = new OwnedCard();
            currentCard.cardName = current.cardName;
            currentCard.setRarity = current.getStringOfMainRarities();
            currentCard.id = current.id;
            currentCard.setName = current.getStringOfSetNames();
            currentCard.quantity = current.quantity;
            currentCard.setNumber = current.getStringOfMainSetNumbers();
            currentCard.priceBought = current.getAveragePrice();
            newList.add(currentCard);

        }

        sortData(newList, currentComparator);

        return newList;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}