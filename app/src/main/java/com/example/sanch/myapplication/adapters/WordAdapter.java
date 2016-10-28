package com.example.sanch.myapplication.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.sanch.myapplication.R;
import com.example.sanch.myapplication.Utils.Utility;
import java.util.ArrayList;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder>  {
    private final Context mContext;
    private ArrayList<String> mWordData = new ArrayList<>();

    public WordAdapter(Context mContext, ArrayList<String> wordData) {
        this.mContext = mContext;
        this.mWordData = wordData;
    }

    public void setWordData(String[] wordData) {
        if (wordData != null) {
            for (String word : wordData) {
                add(word);
            }
        }
    }

    public void setWordData(ArrayList<String> wordData) {
        if (wordData != null) {
            for (String word : wordData) {
                add(word);
            }
        }
    }

    public void add(String object) {
        mWordData.add(object);
        notifyDataSetChanged();
    }

    public void delete(int index) {
        mWordData.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.txtView.setText(mWordData.get(position));

        //set on click listener for each element
        final String theWord = new StringBuilder(holder.txtView.getText()).toString();
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement onClick

            }
        };

        holder.imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utility.DataRetriever dataRetriever = new Utility().new DataRetriever();
                String queryURL = Utility.NetworkConn(theWord, mContext);
                dataRetriever.execute(queryURL);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mWordData.size());
    }

    public String getItem(int position) {
        return (mWordData.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_flash_card, parent, false);
        final ViewHolder holder = new ViewHolder(row);
        return holder;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtView;
        private CardView cardView;
        private ImageButton imgButton;
        private ImageView favView;

        public ViewHolder(View view) {
            super(view);
            txtView = (TextView) view.findViewById(R.id.TextView_word_flash_card);
            imgButton = (ImageButton) view.findViewById(R.id.audioButton);
            cardView = (CardView) view.findViewById(R.id.card_view);
            favView = (ImageView) view.findViewById(R.id.favStar);
            favView.setVisibility(View.INVISIBLE);
        }
    }
}
