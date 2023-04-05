package com.brav.primisphoto.customClass;

import android.content.Context;
import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brav.primisphoto.R;
import com.brav.primisphoto.customInterface.EventCardSelected;

import java.util.ArrayList;

/**
 * Created by ambra on 26/10/2017.
 */

public class MyRvAdapter extends RecyclerView.Adapter<MyRvAdapter.ViewHolder> {
    private ArrayList<Uri> mDataset;
    private EventCardSelected evtCard;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CheckBox checkBox;
        public ImageView imageView;
        private EventCardSelected evtCard;
        private Uri uri;
        private RelativeLayout view;
        private RelativeLayout viewInternal;

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }



        public ViewHolder(RelativeLayout v, final EventCardSelected evtCard) {
            super(v);
            view = v;
            checkBox = (CheckBox) v.findViewById(R.id.selected);
            imageView = (ImageView) v.findViewById(R.id.preview);
            viewInternal = (RelativeLayout) v.findViewById(R.id.bgview);
            this.evtCard = evtCard;
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(checkBox.isChecked()) {
                        deselectItem();
                    }else
                        selectItem(view.getContext());
                    return true; // return true così non prende il click singolo
                }
            });


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    evtCard.showItem(getUri());
                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(!b)
                        deselectItem();
                }
            });
        }

        private void selectItem(Context context){
            view.setSelected(true);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.general_padding);
            viewInternal.setPadding(padding,padding,padding,padding);
            checkBox.setChecked(true);
            checkBox.setVisibility(View.VISIBLE);
            evtCard.selectedItem();
        }

        private void deselectItem(){
            view.setSelected(false);
            checkBox.setChecked(false);
            viewInternal.setPadding(0,0,0,0);
            checkBox.setVisibility(View.GONE);
            evtCard.deselectedItem();
        }




    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyRvAdapter(ArrayList<Uri> myDataset, EventCardSelected evtCard) {
        mDataset = myDataset;
        this.evtCard = evtCard;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //...
        ViewHolder vh = new ViewHolder(v, evtCard);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mImageView.setText(mDataset[position]);
        holder.checkBox.setVisibility(View.GONE);
        holder.imageView.setImageURI(mDataset.get(position));
        holder.setUri(mDataset.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
