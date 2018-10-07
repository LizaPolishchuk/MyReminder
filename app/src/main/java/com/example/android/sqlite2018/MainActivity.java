package com.example.android.sqlite2018;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sqlite2018.utils.MyDividerItemDecoration;
import com.example.android.sqlite2018.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();

    private TextView noNotesTextView;
    private RecyclerView recyclerView;

    private WorkWithNotification notification;

    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        noNotesTextView = (TextView) findViewById(R.id.empty_notes_view);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new WorkWithNotification(this, manager);

        mAdapter = new NotesAdapter(notesList);
        databaseHelper = App.getInstance().getDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showNoteDialog(false,  null, -1);
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }
            @Override
            public void onLongClick(View view, int position) {
              showActionDialog(position);
            }
        }));

        getNotes();
    }

    public void showActionDialog(final int position){
        CharSequence colors[] = new CharSequence[]{"Изменить", "Удалить"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите опцию");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(which==0){
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    notification.closeNotification();
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }

    public void showNoteDialog(final boolean shouldUpdate, final Note note, final int position){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.note_dialog, null);


        Log.d("myLogs", "myAdapter count: " + mAdapter.getItemCount());

        AlertDialog.Builder alertDialogBuilderInput = new AlertDialog.Builder(this);
        alertDialogBuilderInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.et_note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        dialogTitle.setText(!shouldUpdate ? getString(R.string.new_note_title) : getString(R.string.edit_note_title));

        if (shouldUpdate && note!=null){
            inputNote.setText(note.getNote());
        }

        alertDialogBuilderInput.setCancelable(false)
                .setPositiveButton(shouldUpdate ? "Редактировать" : "Создать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilderInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(inputNote.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Введите текст!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    notification.createNotification(inputNote.getText().toString());
                    alertDialog.dismiss();
                }

                if(shouldUpdate && note!=null){
                   updateNote(inputNote.getText().toString(), position);

                } else {
                    Note note = new Note();
                    note.setNote(inputNote.getText().toString());
                   createNote(note);
                }
            }
        });
    }

    public void getNotes(){
        notesList.clear();
        notesList.addAll(databaseHelper.getDao().getNotes());

        mAdapter.notifyDataSetChanged();
        toggleEmptyNotes();
    }

    public void createNote(Note note){
        databaseHelper.getDao().putNote(note);
        getNotes();
    }

    public void updateNote(String note, int position){
        Note n = notesList.get(position);

        n.setNote(note);
        databaseHelper.getDao().updateNote(n);

        notesList.set(position, n);
        getNotes();
    }

    public void deleteNote(int position){
        databaseHelper.getDao().deleteNote(notesList.get(position));
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);
        toggleEmptyNotes();
    }

    public void toggleEmptyNotes(){
        if(notesList.size() > 0){
            noNotesTextView.setVisibility(View.GONE);
        } else {
            noNotesTextView.setVisibility(View.VISIBLE);
        }
    }


}
