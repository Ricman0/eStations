package it.univaq.estations.activity.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.Station;
import it.univaq.estations.activity.DetailsActivity;

public class StationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Station> mDataset;
    private LayoutInflater mInflater;
    private static Activity activity;

    public StationsListAdapter(Context context, List<Station> myDataset) {

        mDataset = myDataset;
        this.mInflater = LayoutInflater.from(context);
        activity = (Activity) context;
    }

    //inner class //2
    class ItemListViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView town;
        TextView km;
        ImageView statusIcon;

        public ItemListViewHolder(@NonNull View itemView) {

            super(itemView);
            title = itemView.findViewById(R.id.stationName);
            town = itemView.findViewById(R.id.stationTown);
            km = itemView.findViewById(R.id.distanceFromStation);
            statusIcon = itemView.findViewById(R.id.statusIcon);

            // Define the click event on item (creating Anonymous View.OnClickListener)
            itemView.setOnClickListener(v -> {
                // Open another Activity and pass to it the right station
                //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                //add extras to intent
                Station station = mDataset.get(getAdapterPosition());
                intent.putExtra("stationId", station.getId());

                //Avendo l’intent, per avviare la nuova activity
                v.getContext().startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

            });
        }
    }





    //1
    //return the ViewHolder linked to specific layout by LayoutInflater
    // Create new views (invoked by the layout manager)
    //inflates the row layout when needed
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ItemListViewHolder(v);
    }

    //3
    // Replace the contents of a view (invoked by the layout manager)
    //come parametro prende in ingresso un oggetto di tipo View, ossia il layout list_item
    //binds the data to the textview in each row
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //ordina in senso crescente
            mDataset.sort(Comparator.comparingDouble(Station::getDistanceFromUser));
        }

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ItemListViewHolder itemHolder = (ItemListViewHolder) holder;
        if (getItemCount() != 0) {
            itemHolder.title.setText(mDataset.get(position).getName());
            itemHolder.town.setText(mDataset.get(position).getTown());
            if (mDataset.get(position).isFree() == true){
                itemHolder.statusIcon.setColorFilter(ContextCompat.getColor(activity.getApplicationContext(), R.color.green));


            }
            else
            { itemHolder.statusIcon.setColorFilter(ContextCompat.getColor(activity.getApplicationContext(), R.color.red));}

            int permissionFineLocation = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
                // permissions not granted
                itemHolder.km.setText("");
            }
            else {
                itemHolder.km.setText(mDataset.get(position).getDistanceFromUser() + " km");
            }

        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataset.size();
    }

    /**
     * Function to add a station list to RecyclerView.
     *
     * @param stations Stations list to add to RecyclerView
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void add(List<Station> stations)
    {
        System.out.println("add");
        mDataset.addAll(stations);
        notifyDataSetChanged();
    }

    /**
     * Function to clear/remove all items from RecyclerView.
     *
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void clear() {
        int size = mDataset.size();
        mDataset.clear();
        notifyItemRangeRemoved(0, size);
    }


}
