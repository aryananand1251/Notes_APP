package com.example.notes;

import static java.util.Locale.filter;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.notes.Adapter.NotesAdapter;
import com.example.notes.Database.RoomDB;
import com.example.notes.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener{

    RecyclerView recyclerView;
    NotesAdapter notesAdapter;
    List<Notes> notes=new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;

    SearchView searchView_home;
    Notes selectedNote;
    DrawerLayout drawerLayout;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=findViewById(R.id.recycler_home);
        fab_add=findViewById(R.id.fab_add);
        searchView_home=findViewById(R.id.searchView_home);
        database=RoomDB.getInstance(this);
        notes=database.mAinDAO().getAll();

        updateRecycler(notes);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent=new Intent(MainActivity.this,NotesTaker.class);
                startActivityForResult(intent,101);

            }
        });
        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText) {
        List<Notes> filteredList=new ArrayList<>();
        for(Notes singleNote : notes){
            if(singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
            || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNote);
            }
        }
        notesAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==101){
            if(resultCode== Activity.RESULT_OK){
                Notes new_notes=(Notes) data.getSerializableExtra("note");
                database.mAinDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mAinDAO().getAll());
                notesAdapter.notifyDataSetChanged();
            }
        } else if (requestCode==102) {

            if(resultCode==Activity.RESULT_OK){
                Notes new_notes=(Notes) data.getSerializableExtra("note");
                database.mAinDAO().update(new_notes.getID(),new_notes.getTitle(),new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mAinDAO().getAll());
                notesAdapter.notifyDataSetChanged();
            }

        }
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesAdapter=new NotesAdapter(MainActivity.this,notes,notesClickListener);
        recyclerView.setAdapter(notesAdapter);
    }

    private  final NotesClickListener notesClickListener=new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {

            Intent intent=new Intent(MainActivity.this,NotesTaker.class);
            intent.putExtra("old_notes",notes);
            startActivityForResult(intent,102);

        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            
            selectedNote=new Notes();
            selectedNote=notes;
            showPopUp(cardView);
            

        }
    };

    private void showPopUp(CardView cardView) {

        PopupMenu popupMenu=new PopupMenu(this,cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        int itemId=item.getItemId();
        if(itemId==R.id.pin){
            if(selectedNote.isPinned()){
                database.mAinDAO().pin(selectedNote.getID(),false);
                Toast.makeText(this, "Unpinned", Toast.LENGTH_SHORT).show();
            }
            else{

                database.mAinDAO().pin(selectedNote.getID(),true);
                Toast.makeText(this, "Pinned", Toast.LENGTH_SHORT).show();
            }
            notes.clear();
            notes.addAll(database.mAinDAO().getAll());
            notesAdapter.notifyDataSetChanged();

        } else if (itemId==R.id.delete) {
            database.mAinDAO().delete(selectedNote);
            notes.remove(selectedNote);
            notesAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Notes Deleted", Toast.LENGTH_SHORT).show();
        }

        return true;

        }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId=item.getItemId();

        return false;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}
