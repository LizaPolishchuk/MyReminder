package com.example.android.sqlite2018;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private TextView noNotesTextView;
    private RecyclerView recyclerView;

    private DatabaseHelper databaseHelper;

    private static final String channelID = "ch1";
    private static final String channelName = "Уведомления напоминаний";
    private static final int notificationID = 1;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        noNotesTextView = (TextView) findViewById(R.id.empty_notes_view);

        databaseHelper = new DatabaseHelper(this);
        notesList.addAll(databaseHelper.getAllNotes());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showNoteDialog(false, null, -1);
            }
        });

        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }
            @Override
            public void onLongClick(View view, int position) {
                showActionDialog(position);
            }
        }));
    }

    private void createNote(String note){
        long id = databaseHelper.insertNote(note);


        Note n = databaseHelper.getNote(id);

        if(n != null){
            notesList.add(0, n);
            mAdapter.notifyDataSetChanged();

            toggleEmptyNotes();
        }
    }

    private void updateNote(String note, int position){
        Note n = notesList.get(position);

        n.setNote(note);
        databaseHelper.updateNote(n);

        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    private void deleteNote(int position){
        databaseHelper.deleteNote(notesList.get(position));

        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    private void showActionDialog(final int position){
            CharSequence colors[] = new CharSequence[]{"Изменить", "Удалить"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите опцию");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(which==0){
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    closeNotification();
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }

    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position){
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View view = layoutInflater.inflate(R.layout.note_dialog, null);


        AlertDialog.Builder alertDialogBuilderInput = new AlertDialog.Builder(MainActivity.this);
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
                    Toast.makeText(MainActivity.this, "Введите текст!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    createNotification(inputNote.getText().toString());
                    alertDialog.dismiss();
                }

                if(shouldUpdate && note!=null){
                    updateNote(inputNote.getText().toString(), position);
                } else {
                    createNote(inputNote.getText().toString());
                }
            }
        });
    }

    private void createNotification(String text){
        Log.d("myLogs", "createNotification<0");


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Log.d("myLogs", "createNotification");

            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(MainActivity.this, channelID);
        }

        builder .setContentTitle("Напоминание!")
                .setContentText(text)
                .setSmallIcon(R.drawable.icon1)
                .setOngoing(true);
        Notification notification = builder.build();

        notificationManager.notify(notificationID, notification);
    }
    private void closeNotification(){
        notificationManager.cancel(notificationID);
    }

    private void toggleEmptyNotes(){
        if(databaseHelper.getNotesCount() > 0){
            noNotesTextView.setVisibility(View.GONE);
        } else {
            noNotesTextView.setVisibility(View.VISIBLE);
        }
    }


}
