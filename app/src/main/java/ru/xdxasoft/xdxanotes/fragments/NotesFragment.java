package ru.xdxasoft.xdxanotes.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.color.ColorRoles;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.MainActivity;
import ru.xdxasoft.xdxanotes.utils.ToastManager;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;
import ru.xdxasoft.xdxanotes.utils.notes.Adapter.NotesListAdapter;
import ru.xdxasoft.xdxanotes.utils.notes.DataBase.RoomDB;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;
import ru.xdxasoft.xdxanotes.utils.notes.NotesClickListener;
import ru.xdxasoft.xdxanotes.utils.notes.NotesTakerActivity;

public class NotesFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "NotesFragment";

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private NotesListAdapter notesListAdapter;
    private RoomDB database;
    private List<Notes> notes = new ArrayList<>();
    private SearchView searchViewHome;
    private Notes selectedNote;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recycler_home);
        fabAdd = view.findViewById(R.id.fab_add);
        searchViewHome = view.findViewById(R.id.searchView_home);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.showCustomToast(
                    "TEST из фрагмента",
                    R.drawable.ic_settings,
                    -5592406,
                    Color.BLACK,
                    Color.BLACK,
                    true
            );
        }

        try {
            database = RoomDB.getInstance(requireContext());
            firebaseManager = FirebaseManager.getInstance(requireContext());

            notes = database.mainDao().getAll();
            updateRecycler(notes);

            if (firebaseManager.isUserLoggedIn()) {
                firebaseManager.syncNotesWithFirebase(success -> {
                    if (success) {
                        try {
                            notes.clear();
                            notes.addAll(database.mainDao().getAll());
                            if (notesListAdapter != null) {
                                notesListAdapter.notifyDataSetChanged();
                            }
                            Log.d(TAG, "Notes synced successfully, count: " + notes.size());
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating notes after sync", e);
                        }
                    } else {
                        Log.e(TAG, "Failed to sync notes with Firebase");
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database or Firebase", e);

            if (mainActivity != null) {
                mainActivity.showCustomToast(
                        getString(R.string.Initialization_error) + e.getMessage(),
                        R.drawable.ic_error_black,
                        Color.RED,
                        Color.BLACK,
                        Color.BLACK,
                        false
                );
            }
        }

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
        try {
            List<Notes> filteredList = new ArrayList<>();
            for (Notes singleNote : notes) {
                if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                        || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                    filteredList.add(singleNote);
                }
            }
            if (notesListAdapter != null) {
                notesListAdapter.filterList(filteredList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering notes", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
                Notes newNote = (Notes) data.getSerializableExtra("note");
                if (newNote != null) {
                    database.mainDao().insert(newNote);
                    Log.d(TAG, "New note saved locally: " + newNote.getTitle());

                    notes.clear();
                    notes.addAll(database.mainDao().getAll());
                    if (notesListAdapter != null) {
                        notesListAdapter.notifyDataSetChanged();
                    }

                    if (firebaseManager.isUserLoggedIn()) {
                        firebaseManager.saveNoteToFirebase(newNote, success -> {
                            if (success) {
                                Log.d(TAG, "Note saved to Firebase: " + newNote.getTitle());
                            } else {
                                Log.e(TAG, "Failed to save note to Firebase: " + newNote.getTitle());
                            }
                        });
                    }
                }
            } else if (requestCode == 102 && resultCode == Activity.RESULT_OK && data != null) {
                Notes updatedNote = (Notes) data.getSerializableExtra("note");
                if (updatedNote != null) {
                    database.mainDao().update(updatedNote.getID(), updatedNote.getTitle(), updatedNote.getNotes());
                    Log.d(TAG, "Note updated locally: " + updatedNote.getTitle());

                    notes.clear();
                    notes.addAll(database.mainDao().getAll());
                    if (notesListAdapter != null) {
                        notesListAdapter.notifyDataSetChanged();
                    }

                    if (firebaseManager.isUserLoggedIn()) {
                        firebaseManager.saveNoteToFirebase(updatedNote, success -> {
                            if (success) {
                                Log.d(TAG, "Note updated in Firebase: " + updatedNote.getTitle());
                            } else {
                                Log.e(TAG, "Failed to update note in Firebase: " + updatedNote.getTitle());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
            MainActivity mainActivity = (MainActivity) getActivity();

            if (mainActivity != null) {
                mainActivity.showCustomToast(
                        getString(R.string.Error_saving_note) + e.getMessage(),
                        R.drawable.ic_error_black,
                        Color.RED,
                        Color.BLACK,
                        Color.BLACK,
                        false
                );
            }
        }
    }

    private void updateRecycler(List<Notes> notes) {
        try {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
            notesListAdapter = new NotesListAdapter(requireContext(), notes, notesClickListener);
            recyclerView.setAdapter(notesListAdapter);
            Log.d(TAG, "RecyclerView updated with " + notes.size() + " notes");
        } catch (Exception e) {
            Log.e(TAG, "Error updating RecyclerView", e);
        }
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            try {
                Intent intent = new Intent(getActivity(), NotesTakerActivity.class);
                intent.putExtra("old_note", notes);
                startActivityForResult(intent, 102);
            } catch (Exception e) {
                Log.e(TAG, "Error opening note for editing", e);
            }
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
        try {
            PopupMenu popupMenu = new PopupMenu(requireContext(), cardView);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing popup menu", e);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            if (item.getItemId() == R.id.pin) {
                if (selectedNote.isPinned()) {
                    database.mainDao().pin(selectedNote.getID(), false);
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null) {
                        mainActivity.showCustomToast(
                                getString(R.string.Unpinned),
                                R.drawable.ic_galohca_black,
                                Color.GREEN,
                                Color.BLACK,
                                Color.BLACK,
                                false
                        );
                    }
                } else {
                    database.mainDao().pin(selectedNote.getID(), true);

                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null) {
                        mainActivity.showCustomToast(
                                getString(R.string.Pinned),
                                R.drawable.ic_galohca_black,
                                Color.GREEN,
                                Color.BLACK,
                                Color.BLACK,
                                false
                        );
                    }
                }

                notes.clear();
                notes.addAll(database.mainDao().getAll());
                if (notesListAdapter != null) {
                    notesListAdapter.notifyDataSetChanged();
                }

                if (firebaseManager.isUserLoggedIn()) {
                    Notes updatedNote = database.mainDao().getById(selectedNote.getID());
                    if (updatedNote != null) {
                        firebaseManager.saveNoteToFirebase(updatedNote, null);
                    }
                }

                return true;

            } else if (item.getItemId() == R.id.delete) {
                if (firebaseManager.isUserLoggedIn()) {
                    firebaseManager.deleteNoteFromFirebase(selectedNote, null);
                }

                database.mainDao().delete(selectedNote);
                notes.remove(selectedNote);
                if (notesListAdapter != null) {
                    notesListAdapter.notifyDataSetChanged();
                }

                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.showCustomToast(
                            getString(R.string.Note_removed),
                            R.drawable.ic_galohca_black,
                            Color.GREEN,
                            Color.BLACK,
                            Color.BLACK,
                            false
                    );
                }

                return true;

            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onMenuItemClick", e);
            return false;
        }
    }
}
