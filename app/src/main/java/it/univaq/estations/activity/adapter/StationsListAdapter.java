package it.univaq.estations.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.Station;

public class StationsListAdapter extends RecyclerView.Adapter<StationsListAdapter.ItemListViewHolder> {

    private List<Station> mDataset;
    private LayoutInflater mInflater;

    //inner class
    class ItemListViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView town;
        public TextView km;
        public ItemListViewHolder(@NonNull View itemView) {

            super(itemView);
            title = itemView.findViewById(R.id.stationName);
            town = itemView.findViewById(R.id.stationTown);
            km = itemView.findViewById(R.id.distanceFromStation);

            // Define the click event on item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Open another Activity and pass to it the right city

                    /**
                    City city = mDataset.get(getAdapterPosition());
                    Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                    intent.putExtra("cityName", city.getName());
                    intent.putExtra("regionName", city.getRegion());
                    intent.putExtra("latitude", city.getLatitude());
                    intent.putExtra("longitude", city.getLongitude());
                    v.getContext().startActivity(intent);
                     */
                }
            });
        }
    }


    public StationsListAdapter(Context context, List<Station> myDataset) {

        mDataset = myDataset;
        this.mInflater = LayoutInflater.from(context);
    }


    // Create new views (invoked by the layout manager)
    //inflates the row layout when needed
    @NonNull
    @Override
    public ItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ItemListViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    //come parametro prende in ingresso un oggetto di tipo View, ossia il layout list_item
    //binds the daa to the textview in each row
    @Override
    public void onBindViewHolder(@NonNull StationsListAdapter.ItemListViewHolder holder, int position) {

        //Station station = mDataset.get(position);  // superfluo??

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(getItemCount()!=0) {
            holder.title.setText(mDataset.get(position).getName());
            holder.town.setText(mDataset.get(position).getTown());
            holder.km.setText("10km");


        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
