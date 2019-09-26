package it.univaq.estations.activity.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.Station;

public class StationsListAdapter extends RecyclerView.Adapter<StationsListAdapter.ViewHolder> {

    private ArrayList<Station> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;


        public ViewHolder(@NonNull TextView itemView) {

            super(itemView);
            title = itemView;
        }
    }


    public StationsListAdapter(ArrayList<Station> myDataset) {
        mDataset = myDataset;
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_stations_list, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull StationsListAdapter.ViewHolder holder, int position) {

        //Station station = mDataset.get(position);  // superfluo??

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.title.setText(mDataset.get(position).getTitle());


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
