package com.example.ygodb.model;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class ItemsListAdapter<T, U extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<U>{

	protected List<T> itemsList;

	protected ItemsListAdapter(List<T> itemsList){
		this.itemsList = itemsList;
	}

	public void setItemsList(List<T> itemsList) {
		this.itemsList = itemsList;
	}

	@Override
	public int getItemCount() {
		return itemsList.size();
	}

}
