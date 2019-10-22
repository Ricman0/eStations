package it.univaq.estations.activity.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.Station;
import it.univaq.estations.activity.DetailsActivity;

public class StationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Station> mDataset;
    private LayoutInflater mInflater;
    private Activity mactivity;


    // for load more
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;



    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    //inner class
    class ItemListViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView town;
        TextView km;
        ImageView statusIcon;
        ItemListViewHolder(@NonNull View itemView) {

            super(itemView);
            title = itemView.findViewById(R.id.stationName);
            town = itemView.findViewById(R.id.stationTown);
            km = itemView.findViewById(R.id.distanceFromStation);
            statusIcon = itemView.findViewById(R.id.statusIcon);

            // Define the click event on item (creating Anonymous View.OnClickListener)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Open another Activity and pass to it the right station
                    //new Intent object: Il costruttore, in caso di intent esplicito, richiede due parametri: il Context (che, nel nostro caso, è l’activity che vuole chiamare la seconda) e la classe che riceverà l’intent, cioè l’activity che vogliamo richiamare.
                    Intent intent = new Intent(v.getContext(), DetailsActivity.class);

                    //add extras to intent
                    Station station = mDataset.get(getAdapterPosition());
                    intent.putExtra("stationId", station.getId());

                    //Avendo l’intent, per avviare la nuova activity
                    v.getContext().startActivity(intent);
                }
            });
        }
    }


    public StationsListAdapter(Context context, List<Station> myDataset, RecyclerView recyclerView) {

        mDataset = myDataset;
        this.mInflater = LayoutInflater.from(context);

        // load more
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }


    // Create new views (invoked by the layout manager)
    //inflates the row layout when needed
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
            return new ItemListViewHolder(v);
        }
        else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(mactivity).inflate(R.layout.item_progressbar, parent, false);
            return new ViewHolderLoading(view);
        }
        return null;

    }

    private class ViewHolderLoading extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.itemProgressbar);
        }
    }


    // Replace the contents of a view (invoked by the layout manager)
    //come parametro prende in ingresso un oggetto di tipo View, ossia il layout list_item
    //binds the daa to the textview in each row
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //Station station = mDataset.get(position);  // superfluo??
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder instanceof StationsListAdapter.ItemListViewHolder) {

            StationsListAdapter.ItemListViewHolder itemHolder = (ItemListViewHolder) holder;
            if (getItemCount() != 0) {
                itemHolder.title.setText(mDataset.get(position).getName());
                itemHolder.town.setText(mDataset.get(position).getTown());
                if (!mDataset.get(position).isFree())
                    itemHolder.statusIcon.setImageResource(R.drawable.presence_busy);
                float[] dist = new float[1];
                //TODO prendere posizione attuale
                android.location.Location.distanceBetween(42.360205, 13.377868,
                        mDataset.get(position).getPosition().latitude, mDataset.get(position).getPosition().longitude, dist);
                @SuppressWarnings("IntegerDivisionInFloatingPointContext") double km = ((int) dist[0] / 100) / 10.0;
                itemHolder.km.setText(km + " km");
            }
        }else if (holder instanceof StationsListAdapter.ViewHolderLoading) {
            StationsListAdapter.ViewHolderLoading loadingViewHolder = (StationsListAdapter.ViewHolderLoading) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
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
