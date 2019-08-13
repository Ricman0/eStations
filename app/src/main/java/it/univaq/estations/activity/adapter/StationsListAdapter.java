package it.univaq.estations.activity.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.univaq.estations.R;

public class StationsListAdapter extends RecyclerView.Adapter<StationsListAdapter.ViewHolder> {
    private String[] mDataset;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_stations_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StationsListAdapter.ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
