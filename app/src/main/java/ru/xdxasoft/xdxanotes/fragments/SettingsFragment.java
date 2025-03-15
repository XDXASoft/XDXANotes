package ru.xdxasoft.xdxanotes.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ru.xdxasoft.xdxanotes.R;
import ru.xdxasoft.xdxanotes.activity.MainActivity;
import ru.xdxasoft.xdxanotes.utils.LocaleHelper;
import ru.xdxasoft.xdxanotes.utils.firebase.FirebaseManager;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link SettingsFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private Button btnSyncNotes, fbabtn;
    private Button btnSyncPasswords;
    private Button btnToggleLanguage;
    private Button btnSystemLanguage;
    private TextView tvCurrentLanguage;
    private TextView tvLanguageMode;
    private FirebaseManager firebaseManager;

    FirebaseAuth fba;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fba = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        try {
            firebaseManager = FirebaseManager.getInstance(requireContext());

            btnSyncNotes = view.findViewById(R.id.btnSyncNotes);
            btnSyncPasswords = view.findViewById(R.id.btnSyncPasswords);
            btnToggleLanguage = view.findViewById(R.id.btnToggleLanguage);
            btnSystemLanguage = view.findViewById(R.id.btnSystemLanguage);
            tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage);
            tvLanguageMode = view.findViewById(R.id.tvLanguageMode);
            fbabtn = view.findViewById(R.id.btnlogauth);


            fbabtn.setOnClickListener(v->{
                fba.signOut();
            });
            // Отображаем текущий язык и режим
            updateLanguageInfo();

            btnSyncNotes.setOnClickListener(v -> {
                if (firebaseManager.isUserLoggedIn()) {
                    Toast.makeText(requireContext(), "Синхронизация заметок...", Toast.LENGTH_SHORT).show();
                    firebaseManager.syncNotesWithFirebase(success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Заметки синхронизированы", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Ошибка синхронизации заметок", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show();
                }
            });

            btnSyncPasswords.setOnClickListener(v -> {
                if (firebaseManager.isUserLoggedIn()) {
                    Toast.makeText(requireContext(), "Синхронизация паролей...", Toast.LENGTH_SHORT).show();
                    firebaseManager.syncPasswordsWithFirebase(success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Пароли синхронизированы", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Ошибка синхронизации паролей", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show();
                }
            });

            btnToggleLanguage.setOnClickListener(v -> {
                if (getActivity() != null) {
                    LocaleHelper.toggleLanguage(getActivity());
                }
            });

            btnSystemLanguage.setOnClickListener(v -> {
                if (getActivity() != null) {
                    LocaleHelper.useSystemLanguage(getActivity());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(requireContext(), "Ошибка инициализации настроек: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void updateLanguageInfo() {
        try {
            String currentLanguage = LocaleHelper.getLanguage(requireContext());
            String languageName = currentLanguage.equals("ru") ? "Русский" : "English";
            tvCurrentLanguage.setText(getString(R.string.current_language) + ": " + languageName);

            boolean useSystemLanguage = LocaleHelper.isUsingSystemLanguage(requireContext());
            String modeText = useSystemLanguage
                    ? getString(R.string.using_system_language)
                    : getString(R.string.using_custom_language);
            tvLanguageMode.setText(modeText);
        } catch (Exception e) {
            Log.e(TAG, "Error updating language info", e);
        }
    }


}
