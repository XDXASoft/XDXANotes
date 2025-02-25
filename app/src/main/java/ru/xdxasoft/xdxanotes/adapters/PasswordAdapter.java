package ru.xdxasoft.xdxanotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.function.Consumer;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.models.Password;
import com.google.android.material.button.MaterialButton;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    private List<Password> passwords;
    private Consumer<String> onCopyClick;
    private Consumer<Long> onDeleteClick;
    private Consumer<Password> onEditClick;

    public PasswordAdapter(List<Password> passwords, Consumer<String> onCopyClick, Consumer<Long> onDeleteClick, Consumer<Password> onEditClick) {
        this.passwords = passwords;
        this.onCopyClick = onCopyClick;
        this.onDeleteClick = onDeleteClick;
        this.onEditClick = onEditClick;
    }

    @Override
    public PasswordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PasswordViewHolder holder, int position) {
        Password password = passwords.get(position);
        holder.bind(password);
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    class PasswordViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvUsername, tvPassword;
        MaterialButton btnEdit, btnDelete, btnShowPassword, btnCopyUsername, btnCopyPassword;
        boolean isPasswordVisible = false;

        PasswordViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnShowPassword = itemView.findViewById(R.id.btnShowPassword);
            btnCopyUsername = itemView.findViewById(R.id.btnCopyUsername);
            btnCopyPassword = itemView.findViewById(R.id.btnCopyPassword);
        }

        void bind(Password password) {
            tvTitle.setText(password.getTitle());
            tvUsername.setText(password.getUsername());
            tvPassword.setText("••••••••");

            btnShowPassword.setOnClickListener(v -> {
                isPasswordVisible = !isPasswordVisible;
                tvPassword.setText(isPasswordVisible ? password.getPassword() : "••••••••");
                btnShowPassword.setIconResource(isPasswordVisible
                        ? android.R.drawable.ic_menu_view
                        : android.R.drawable.ic_menu_view);
            });

            btnCopyUsername.setOnClickListener(v -> onCopyClick.accept(password.getUsername()));
            btnCopyPassword.setOnClickListener(v -> onCopyClick.accept(password.getPassword()));
            btnEdit.setOnClickListener(v -> onEditClick.accept(password));
            btnDelete.setOnClickListener(v -> onDeleteClick.accept(password.getId()));
        }
    }
}
