package ru.xdxasoft.xdxanotes.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.adapters.PasswordAdapter;
import ru.xdxasoft.xdxanotes.models.Password;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link PasswordFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class PasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView rvPasswords;
    private PasswordAdapter adapter;
    private List<Password> passwords;
    private SQLiteDatabase database;
    private FloatingActionButton fabAdd;
    private BottomSheetDialog bottomSheetDialog;
    private EditText etTitle, etUsername, etPassword;
    private Button btnSave;
    private TextView tvBottomSheetTitle;
    private Password currentEditingPassword;

    public PasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        initViews(view);
        initDatabase();
        setupRecyclerView();
        setupBottomSheet();
        loadPasswords();

        fabAdd.setOnClickListener(v -> showBottomSheet(null));

        return view;
    }

    private void initViews(View view) {
        rvPasswords = view.findViewById(R.id.rvPasswords);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void initDatabase() {
        database = requireContext().openOrCreateDatabase("passwords.db", Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS passwords ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT, "
                + "username TEXT, "
                + "password TEXT)");
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
        tvBottomSheetTitle.setText(password == null ? getString(R.string.Add_password) : "Редактировать пароль");

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
                this::showBottomSheet
        );
        rvPasswords.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPasswords.setAdapter(adapter);
    }

    private void loadPasswords() {
        passwords.clear();
        Cursor cursor = database.rawQuery("SELECT * FROM passwords", null);
        while (cursor.moveToNext()) {
            passwords.add(new Password(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            ));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void savePassword() {
        String title = etTitle.getText().toString();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (title.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("username", username);
        cv.put("password", password);

        if (currentEditingPassword != null) {
            database.update("passwords", cv, "id = ?",
                    new String[]{String.valueOf(currentEditingPassword.getId())});
        } else {
            database.insert("passwords", null, cv);
        }

        loadPasswords();
        bottomSheetDialog.dismiss();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "Скопировано", Toast.LENGTH_SHORT).show();
    }

    private void deletePassword(long id) {
        database.delete("passwords", "id = ?", new String[]{String.valueOf(id)});
        loadPasswords();
    }
}
