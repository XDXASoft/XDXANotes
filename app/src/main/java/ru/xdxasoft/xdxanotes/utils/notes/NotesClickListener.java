package ru.xdxasoft.xdxanotes.utils.notes;

import androidx.cardview.widget.CardView;

import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;

public interface NotesClickListener {

    void onClick (Notes notes);
    void onLongCLick (Notes notes, CardView cardView);

    void onLongClick(Notes notes, CardView cardView);
}
