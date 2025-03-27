package ru.xdxasoft.xdxanotes.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.models.Password;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    private static final String TAG = "PasswordAdapter";

    private List<Password> passwords;
    private OnCopyClickListener copyListener;
    private OnDeleteClickListener deleteListener;
    private OnEditClickListener editListener;
    private OnShowPasswordClickListener showPasswordListener;

    public PasswordAdapter(List<Password> passwords,
                           OnCopyClickListener copyListener,
                           OnDeleteClickListener deleteListener,
                           OnEditClickListener editListener,
                           OnShowPasswordClickListener showPasswordListener) {
        this.passwords = passwords;
        this.copyListener = copyListener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.showPasswordListener = showPasswordListener;
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
                    holder.tvPassword.setText("••••••••");
                }

                if (holder.btnCopyUsername != null) {
                    holder.btnCopyUsername.setOnClickListener(v -> {
                        if (copyListener != null) {
                            copyListener.onCopyClick(password.getUsername());
                        }
                    });
                }

                if (holder.btnCopyPassword != null) {
                    holder.btnCopyPassword.setOnClickListener(v -> {
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

                if (holder.btnEdit != null) {
                    holder.btnEdit.setOnClickListener(v -> {
                        if (editListener != null) {
                            editListener.onEditClick(password);
                        }
                    });
                }

                if (holder.btnShowPassword != null) {
                    holder.btnShowPassword.setOnClickListener(v -> {
                        if (showPasswordListener != null) {
                            showPasswordListener.onShowPasswordClick(password, holder.tvPassword);
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
        MaterialButton btnCopyUsername, btnCopyPassword, btnDelete, btnEdit, btnShowPassword;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvPassword = itemView.findViewById(R.id.tvPassword);
                btnCopyUsername = itemView.findViewById(R.id.btnCopyUsername);
                btnCopyPassword = itemView.findViewById(R.id.btnCopyPassword);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnShowPassword = itemView.findViewById(R.id.btnShowPassword);
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

    public interface OnShowPasswordClickListener {
        void onShowPasswordClick(Password password, TextView tvPassword);
    }
}