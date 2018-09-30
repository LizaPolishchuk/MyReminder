package com.example.android.sqlite2018;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> {

    private Context context;
    private List<Note> notesList;



    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView dot;
        public TextView note;
        public TextView timestamp;

        public MyViewHolder(View  view){
            super(view);
            note = view.findViewById(R.id.note);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
        }
    }

    public NotesAdapter (Context context, List<Note> notesList){
        this.context = context;
        this.notesList = notesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View itemView = LayoutInflater.from(parent.getContext()) .inflate(R.layout.note_list_item, parent, false);

       return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Note note = notesList.get(position);

        holder.note.setText(note.getNote());

        holder.dot.setText(Html.fromHtml("&#8226;"));

        holder.timestamp.setText(formatDate(note.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }


    private String formatDate(String dateStr){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateStr);
            SimpleDateFormat sdfOut = new SimpleDateFormat("dd MMM yyyy");
            return sdfOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }
}
