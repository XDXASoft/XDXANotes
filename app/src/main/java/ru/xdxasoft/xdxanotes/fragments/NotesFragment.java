package ru.xdxasoft.xdxanotes.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ru.xdxasoft.xdxanotes.utils.notes.Adapter.NotesListAdapter;
import ru.xdxasoft.xdxanotes.utils.notes.DataBase.RoomDB;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;
import ru.xdxasoft.xdxanotes.utils.notes.NotesClickListener;
import ru.xdxasoft.xdxanotes.utils.notes.NotesTakerActivity;
import ru.xdxasoft.xdxanotes.R;

public class NotesFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private NotesListAdapter notesListAdapter;
    private RoomDB database;
    private List<Notes> notes = new ArrayList<>();
    private SearchView searchViewHome;
    private Notes selectedNote;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recycler_home);
        fabAdd = view.findViewById(R.id.fab_add);
        searchViewHome = view.findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(requireContext());
        notes = database.mainDao().getAll();

        updateRecycler(notes);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotesTakerActivity.class);
            startActivityForResult(intent, 101);
        });

        searchViewHome.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        return view;
    }

    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) ||
                    singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            Notes newNote = (Notes) data.getSerializableExtra("note");
            database.mainDao().insert(newNote);
            notes.clear();
            notes.addAll(database.mainDao().getAll());
            notesListAdapter.notifyDataSetChanged();
        } else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            Notes updatedNote = (Notes) data.getSerializableExtra("note");
            database.mainDao().update(updatedNote.getID(), updatedNote.getTitle(), updatedNote.getNotes());
            notes.clear();
            notes.addAll(database.mainDao().getAll());
            notesListAdapter.notifyDataSetChanged();
        }
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(requireContext(), notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(getActivity(), NotesTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongCLick(Notes notes, CardView cardView) {
            selectedNote = notes;
            showPopup(cardView);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNote = notes;
            showPopup(cardView);
        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.pin) {
            if (selectedNote.isPinned()) {
                database.mainDao().pin(selectedNote.getID(), false);
                Toast.makeText(requireContext(), "Unpinned", Toast.LENGTH_SHORT).show();
            } else {
                database.mainDao().pin(selectedNote.getID(), true);
                Toast.makeText(requireContext(), "Pinned", Toast.LENGTH_SHORT).show();
            }
            notes.clear();
            notes.addAll(database.mainDao().getAll());
            notesListAdapter.notifyDataSetChanged();
            return true;

        } else if (item.getItemId() == R.id.delete) {
            database.mainDao().delete(selectedNote);
            notes.remove(selectedNote);
            notesListAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Note removed", Toast.LENGTH_SHORT).show();
            return true;

        } else {
            return false;
        }

    }
}