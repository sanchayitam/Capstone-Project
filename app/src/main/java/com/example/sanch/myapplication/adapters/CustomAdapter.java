package com.example.sanch.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.sanch.myapplication.R;
import com.example.sanch.myapplication.model.Definition;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<Definition> definitions;

    private static class ViewHolder {
        TextView numberView;
        TextView wordView;
        TextView partOfSpeechView;
        TextView pronunciationView;
        TextView definitionsView;
    }

    public CustomAdapter(Context context, ArrayList<Definition> definitions) {
        super(context, R.layout.listview, definitions);
        this.context= context;
        this.definitions = definitions;
    }

    public void setDefinitionData( ArrayList<Definition> wordData){
        if(wordData != null){
            for (Definition word : wordData) {
                add(word);
            }
        }
    }

    public void add(Definition object) {
        definitions.add(object);
        //data has been changed and any View reflecting the data set should refresh itself
        notifyDataSetChanged();
    }

    public int getCount() {
        return definitions.size();
    }

    public Definition getItem(int position) {
        return definitions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview, parent, false);

            holder.numberView = (TextView) convertView.findViewById(R.id.entryNumber);
            holder.wordView = (TextView) convertView.findViewById(R.id.word);
            holder.partOfSpeechView = (TextView) convertView.findViewById(R.id.partOfSpeech);
            holder.definitionsView = (TextView) convertView.findViewById(R.id.definitions);
            holder.pronunciationView = (TextView) convertView.findViewById(R.id.pronunciation);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.numberView.setText(definitions.get(position).getEntryNumber());
        holder.wordView.setText(definitions.get(position).getWord());
        holder.pronunciationView.setText(definitions.get(position).getPronunciation());
        holder.partOfSpeechView.setText(definitions.get(position).getPartOfSpeech());
        holder.definitionsView.setText(definitions.get(position).getDefinitions());

        return convertView;
    }
}