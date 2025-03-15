package ru.xdxasoft.xdxanotes.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.models.Password;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    private static final String TAG = "PasswordAdapter";

    private List<Password> passwords;
    private OnCopyClickListener copyListener;
    private OnDeleteClickListener deleteListener;
    private OnEditClickListener editListener;

    public PasswordAdapter(List<Password> passwords,
            OnCopyClickListener copyListener,
            OnDeleteClickListener deleteListener,
            OnEditClickListener editListener) {
        this.passwords = passwords;
        this.copyListener = copyListener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_password, parent, false);
            return new PasswordViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateViewHolder", e);
            
            View view = new View(parent.getContext());
            return new PasswordViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        try {
            if (position >= 0 && position < passwords.size()) {
                Password password = passwords.get(position);

                if (holder.tvTitle != null) {
                    holder.tvTitle.setText(password.getTitle());
                }

                if (holder.tvUsername != null) {
                    holder.tvUsername.setText(password.getUsername());
                }

                
                if (holder.tvPassword != null) {
                    String maskedPassword = "••••••••";
                    holder.tvPassword.setText(maskedPassword);
                }

                if (holder.btnCopy != null) {
                    holder.btnCopy.setOnClickListener(v -> {
                        if (copyListener != null) {
                            copyListener.onCopyClick(password.getPassword());
                        }
                    });
                }

                if (holder.btnDelete != null) {
                    holder.btnDelete.setOnClickListener(v -> {
                        if (deleteListener != null) {
                            deleteListener.onDeleteClick(password.getId());
                        }
                    });
                }

                if (holder.itemView != null) {
                    holder.itemView.setOnClickListener(v -> {
                        if (editListener != null) {
                            editListener.onEditClick(password);
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return passwords != null ? passwords.size() : 0;
    }

    public static class PasswordViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvUsername, tvPassword;
        ImageButton btnCopy, btnDelete;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvPassword = itemView.findViewById(R.id.tvPassword);
                btnCopy = itemView.findViewById(R.id.btnCopy);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            } catch (Exception e) {
                Log.e(TAG, "Error in PasswordViewHolder constructor", e);
            }
        }
    }

    public interface OnCopyClickListener {

        void onCopyClick(String text);
    }

    public interface OnDeleteClickListener {

        void onDeleteClick(String id);
    }

    public interface OnEditClickListener {

        void onEditClick(Password password);
    }
}
