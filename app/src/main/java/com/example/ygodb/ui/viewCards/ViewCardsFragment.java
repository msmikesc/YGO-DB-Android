package com.example.ygodb.ui.viewCards;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ygodb.R;
import com.example.ygodb.abs.TextChangedListener;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.databinding.FragmentViewcardsBinding;
import com.example.ygodb.abs.EndlessScrollListener;
import com.example.ygodb.ui.singleCard.SingleCardToListAdapter;

import java.util.ArrayList;

public class ViewCardsFragment extends Fragment {

    private FragmentViewcardsBinding binding;

    private ArrayList<OwnedCard> cardsList;

    private static final int loadingLimit = 100;

    private static String sortOrder = null;
    private static String sortOption = null;
    private static String cardNameSearch = null;

    private static LinearLayoutManager layout = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewCardsViewModel viewCardsViewModel =
                new ViewModelProvider(this).get(ViewCardsViewModel.class);

        binding = FragmentViewcardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView cardsListView = binding.viewList;

        sortOrder = "dateBought desc, cardName asc";
        sortOption = "Date Bought";

        try {
            cardsList = loadMoreData(sortOrder,
                    loadingLimit, 0, cardNameSearch);
        } catch (Exception e) {
            e.printStackTrace();
            cardsList = new ArrayList<>();
        }

        SingleCardToListAdapter adapter = new SingleCardToListAdapter(getContext(), cardsList);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        layout = linearLayoutManager;
        cardsListView.setLayoutManager(linearLayoutManager);
        cardsListView.setAdapter(adapter);


        EndlessScrollListener scrollListener = new EndlessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                ArrayList<OwnedCard> moreCards =
                        loadMoreData(sortOrder, loadingLimit, page * loadingLimit, cardNameSearch);
                int curSize = adapter.getItemCount();
                cardsList.addAll(moreCards);

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemRangeInserted(curSize, moreCards.size() - 1);
                    }
                });
            }
        };

        cardsListView.addOnScrollListener(scrollListener);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(getContext(), binding.fab);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        if(!sortOption.equals(menuItem.getTitle())) {
                            sortOption = (String)menuItem.getTitle();

                            if(sortOption.equals("Date Bought")){
                                sortOrder = "dateBought desc, cardName asc";
                            }
                            else if(sortOption.equals("Card Name")){
                                sortOrder = "cardName asc, dateBought desc";
                            }
                            else if(sortOption.equals("Set Number")){
                                sortOrder = "setName asc, setNumber asc";
                            }
                            else if(sortOption.equals("Price")){
                                sortOrder = "priceBought desc, cardName asc";
                            }

                            cardsList.clear();
                            ArrayList<OwnedCard> moreCards =
                                    loadMoreData(sortOrder, loadingLimit, 0, cardNameSearch);
                            int curSize = adapter.getItemCount();
                            cardsList.addAll(moreCards);

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
                cardNameSearch = s.toString();

                cardsList.clear();

                cardsList.addAll(loadMoreData(sortOrder, loadingLimit, 0, cardNameSearch));
                layout.scrollToPositionWithOffset(0, 0);

                adapter.notifyDataSetChanged();
            }
        });

        return root;
    }

    public ArrayList<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
        ArrayList<OwnedCard> newList = SQLiteConnection.getObj().queryOwnedCards(orderBy,
                limit, offset, cardNameSearch);
        return newList;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}