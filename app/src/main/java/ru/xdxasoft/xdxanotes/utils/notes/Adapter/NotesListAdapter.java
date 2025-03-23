package ru.xdxasoft.xdxanotes.utils.notes.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.utils.notes.Models.Notes;
import ru.xdxasoft.xdxanotes.utils.notes.NotesClickListener;

public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.NotesViewHolder> {

    private static final String TAG = "NotesListAdapter";

    private Context context;
    private List<Notes> list;
    private NotesClickListener listener;

    public NotesListAdapter(Context context, List<Notes> list, NotesClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.notes_list, parent, false);
            return new NotesViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateViewHolder", e);
            View view = new View(context);
            return new NotesViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        try {
            if (position >= 0 && position < list.size()) {
                Notes note = list.get(position);

                if (holder.textView_title != null) {
                    holder.textView_title.setText(note.getTitle());
                    holder.textView_title.setSelected(true);
                }

                if (holder.textView_notes != null) {
                    holder.textView_notes.setText(note.getNotes());
                }

                if (holder.textView_date != null) {
                    holder.textView_date.setText(note.getDate());
                    holder.textView_date.setSelected(true);
                }

                if (holder.imageView_pin != null) {
                    if (note.isPinned()) {
                        holder.imageView_pin.setImageResource(R.drawable.pin_icon);
                    } else {
                        holder.imageView_pin.setImageResource(0);
                    }
                }

                if (holder.itemView != null) {
                    holder.itemView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onClick(note);
                        }
                    });

                    holder.itemView.setOnLongClickListener(v -> {
                        if (listener != null && holder.notes_container != null) {
                            listener.onLongClick(note, holder.notes_container);
                            return true;
                        }
                        return false;
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void filterList(List<Notes> filteredList) {
        try {
            list = filteredList;
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error in filterList", e);
        }
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        CardView notes_container;
        TextView textView_title, textView_notes, textView_date;
        ImageView imageView_pin;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                notes_container = itemView.findViewById(R.id.notes_container);
                if (notes_container == null) {
                    notes_container = itemView.findViewById(R.id.contentFragment);
                }

                textView_title = itemView.findViewById(R.id.textView_title);
                textView_notes = itemView.findViewById(R.id.textView_notes);
                textView_date = itemView.findViewById(R.id.textView_date);
                imageView_pin = itemView.findViewById(R.id.imageView_pin);
            } catch (Exception e) {
                Log.e(TAG, "Error in NotesViewHolder constructor", e);
            }
        }
    }
}
