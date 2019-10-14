package it.univaq.estations.activity.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.univaq.estations.R;
import it.univaq.estations.model.PointOfCharge;

public class PointOfChargeListAdapter extends RecyclerView.Adapter<PointOfChargeListAdapter.ItemPointOfChargeListViewHolder>{
    private List<PointOfCharge> pointsOfCharge;
    private LayoutInflater mInflater;

    //inner class
    class ItemPointOfChargeListViewHolder extends RecyclerView.ViewHolder {

        TextView volt;
        TextView kw;
        TextView status;
        ItemPointOfChargeListViewHolder(@NonNull View itemView) {

            super(itemView);
            volt = itemView.findViewById(R.id.voltPointOfChargesDetails);
            kw = itemView.findViewById(R.id.kwPointOfChargesDetails);
            status = itemView.findViewById(R.id.statusPointOfChargesDetails);
        }

    }

    public PointOfChargeListAdapter(Context context, List<PointOfCharge> myDataset) {

        pointsOfCharge = myDataset;
        this.mInflater = LayoutInflater.from(context);
    }

    // Create new views (invoked by the layout manager)
    //inflates the row layout when needed
    @NonNull
    @Override
    public ItemPointOfChargeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.point_of_charge_item, parent, false);
        return new ItemPointOfChargeListViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    //come parametro prende in ingresso un oggetto di tipo View, ossia il layout list_item
    //binds the data to the textview in each row
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PointOfChargeListAdapter.ItemPointOfChargeListViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(getItemCount()!=0) {
            holder.volt.setText(String.valueOf(pointsOfCharge.get(position).getVoltage()));
            holder.kw.setText(String.valueOf(pointsOfCharge.get(position).getKw()));
            holder.status.setText(String.valueOf(pointsOfCharge.get(position).getStatusTypeId()));
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return pointsOfCharge.size();
    }
}
