package com.example.ygodb.model.partialscroll;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class PartialScrollToListAdapter<T, U extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<U>{

	protected List<T> cardsList;

	protected PartialScrollToListAdapter(List<T> cardsList){
		this.cardsList = cardsList;
	}

	public void setCardsList(List<T> cardsList) {
		this.cardsList = cardsList;
	}

	@Override
	public int getItemCount() {
		return cardsList.size();
	}

}
