package com.example.sanch.myapplication.adapters;

import android.content.Context;
import android.database.Cursor;
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
import com.example.sanch.myapplication.data.WordListDbContract;

public class WordCursorAdapter extends RecyclerView.Adapter<WordCursorAdapter.ViewHolder> {
    private Context mContext;
    Cursor mCursor;

    public WordCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong( WordListDbContract.WordEntry.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_flash_card, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        holder.txtView.setText(mCursor.getString(mCursor.getColumnIndex(WordListDbContract.WordEntry.COLUMN_WORD)));
        final String theWord = new StringBuilder(holder.txtView.getText()).toString();

        // final String theWord = new StringBuilder(holder.txtView.getText()).toString();
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //implement onClick

            }
        };

         holder.imgButton.setOnClickListener(new View.OnClickListener() {
          @Override
              public void onClick(View v) {
              Utility.DataRetriever dataRetriever = new Utility(mContext).new DataRetriever();
              String queryURL = Utility.NetworkConn(theWord, mContext);
              dataRetriever.execute(queryURL);
          }
         });
    }

    @Override public int getItemCount() {
      return mCursor.getCount();
  }

    public static class ViewHolder extends  RecyclerView.ViewHolder {
        TextView txtView;
        private CardView cardView;
        private ImageButton imgButton;
        private ImageView favView;

        public ViewHolder(View view) {
            super(view);
            txtView = (TextView) view.findViewById(R.id.TextView_word_flash_card);
            imgButton = (ImageButton) view.findViewById(R.id.audioButton);
            cardView = (CardView) view.findViewById(R.id.card_view);
            favView = (ImageView) view.findViewById(R.id.favStar);
            favView.setVisibility(View.VISIBLE);
        }
    }
}
