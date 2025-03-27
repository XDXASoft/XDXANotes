package ru.xdxasoft.xdxanotes.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.MainActivity;
import ru.xdxasoft.xdxanotes.adapters.PasswordAdapter;
import ru.xdxasoft.xdxanotes.models.Password;
import ru.xdxasoft.xdxanotes.utils.IdGenerator;
import ru.xdxasoft.xdxanotes.utils.PasswordDatabaseHelper;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;

public class PasswordFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView rvPasswords;
    private PasswordAdapter adapter;
    private List<Password> passwords;
    private SQLiteDatabase database;
    private PasswordDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;
    private BottomSheetDialog bottomSheetDialog;
    private EditText etTitle, etUsername, etPassword;
    private Button btnSave;
    private TextView tvBottomSheetTitle;
    private Password currentEditingPassword;
    private FirebaseManager firebaseManager;

    public PasswordFragment() {
    }

    public static PasswordFragment newInstance(String param1, String param2) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container, false);

        try {
            initViews(view);
            initDatabase();
            setupRecyclerView();
            setupBottomSheet();

            firebaseManager = FirebaseManager.getInstance(requireContext());

            loadPasswords();

            if (firebaseManager.isUserLoggedIn()) {
                firebaseManager.syncPasswordsWithFirebase(success -> {
                    if (success) {
                        loadPasswords();
                    }
                });
            }

            fabAdd.setOnClickListener(v -> showBottomSheet(null));
        } catch (Exception e) {
            Log.e("PasswordFragment", "Error in onCreateView", e);
            showToast(getString(R.string.Initialization_error) + e.getMessage(), true);
        }

        return view;
    }

    private void initViews(View view) {
        rvPasswords = view.findViewById(R.id.rvPasswords);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void initDatabase() {
        dbHelper = new PasswordDatabaseHelper(requireContext());
        database = dbHelper.getWritableDatabase();
    }

    private void setupBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_password, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        etTitle = bottomSheetView.findViewById(R.id.etTitle);
        etUsername = bottomSheetView.findViewById(R.id.etUsername);
        etPassword = bottomSheetView.findViewById(R.id.etPassword);
        btnSave = bottomSheetView.findViewById(R.id.btnSave);
        tvBottomSheetTitle = bottomSheetView.findViewById(R.id.tvTitle);

        btnSave.setOnClickListener(v -> savePassword());
    }

    private void showBottomSheet(Password password) {
        currentEditingPassword = password;
        tvBottomSheetTitle.setText(password == null ? getString(R.string.Add_password) : getString(R.string.Edit_password));

        if (password != null) {
            etTitle.setText(password.getTitle());
            etUsername.setText(password.getUsername());
            etPassword.setText(password.getPassword());
        } else {
            etTitle.setText("");
            etUsername.setText("");
            etPassword.setText("");
        }

        bottomSheetDialog.show();
    }

    private void setupRecyclerView() {
        passwords = new ArrayList<>();
        adapter = new PasswordAdapter(
                passwords,
                this::copyToClipboard,
                this::deletePassword,
                this::showBottomSheet,
                this::togglePasswordVisibility
        );
        rvPasswords.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPasswords.setAdapter(adapter);
    }

    private void loadPasswords() {
        passwords.clear();
        Cursor cursor = database.query("passwords", null, "userId = ?",
                new String[]{firebaseManager.getUserId()}, null, null, "title ASC");

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String title = cursor.getString(1);
            String username = cursor.getString(2);
            String password = cursor.getString(3);
            String userId = cursor.getString(4);

            Password passwordObj = new Password(id, title, username, password, userId);
            passwords.add(passwordObj);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void savePassword() {
        String title = etTitle.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (title.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showToast(getString(R.string.Please_fill_in_all_fields), true);
            return;
        }

        Password newPassword;
        if (currentEditingPassword != null) {
            newPassword = currentEditingPassword;
            newPassword.setTitle(title);
            newPassword.setUsername(username);
            newPassword.setPassword(password);
        } else {
            newPassword = new Password(title, username, password, firebaseManager.getUserId());
        }

        firebaseManager.savePasswordToFirebase(newPassword, success -> {
            if (success) {
                ContentValues values = new ContentValues();
                values.put("id", newPassword.getId());
                values.put("title", newPassword.getTitle());
                values.put("username", newPassword.getUsername());
                values.put("password", newPassword.getPassword());
                values.put("userId", newPassword.getUserId());

                if (currentEditingPassword != null) {
                    database.update("passwords", values, "id = ? AND userId = ?",
                            new String[]{newPassword.getId(), newPassword.getUserId()});
                } else {
                    database.insert("passwords", null, values);
                }

                loadPasswords();
                bottomSheetDialog.dismiss();
                showToast(getString(R.string.Password_saved), false);
            } else {
                showToast(getString(R.string.Error_saving_password), true);
            }
        });
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", text);
        clipboard.setPrimaryClip(clip);
        showToast(getString(R.string.Copied), false);
    }

    private void deletePassword(String id) {
        if (firebaseManager.isUserLoggedIn()) {
            firebaseManager.deletePasswordFromFirebase(id, null);
        }

        database.delete("passwords", "id = ?", new String[]{id});
        loadPasswords();
        showToast("DELLPASS", false);
    }

    private void togglePasswordVisibility(Password password, TextView tvPassword) {
        if (tvPassword.getText().toString().equals("••••••••")) {
            tvPassword.setText(password.getPassword());
        } else {
            tvPassword.setText("••••••••");
        }
    }

    private void showToast(String message, boolean isError) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.showCustomToast(
                    message,
                    isError ? R.drawable.ic_error_black : R.drawable.ic_galohca_black,
                    ContextCompat.getColor(requireContext(),
                            isError ? R.color.error_red : R.color.success_green),
                    ContextCompat.getColor(requireContext(), R.color.black),
                    ContextCompat.getColor(requireContext(), R.color.black),
                    false
            );
        }
    }
}