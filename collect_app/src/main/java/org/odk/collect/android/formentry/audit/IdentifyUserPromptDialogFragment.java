package org.odk.collect.android.formentry.audit;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;

public class IdentifyUserPromptDialogFragment extends DialogFragment {

    public static final String TAG = "IdentifyUserPromptDialogFragment";
    private static final String ARG_FORM_NAME = "ArgFormName";

    private EditText identityField;
    private IdentityPromptViewModel viewModel;

    public static IdentifyUserPromptDialogFragment create(String formName) {
        IdentifyUserPromptDialogFragment dialog = new IdentifyUserPromptDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString(IdentifyUserPromptDialogFragment.ARG_FORM_NAME, formName);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.identify_user_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getArguments().getString(ARG_FORM_NAME));
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
            viewModel.promptClosing();
        });

        identityField = view.findViewById(R.id.identity);
        identityField.setOnEditorActionListener((textView, i, keyEvent) -> {
            viewModel.setIdentity(identityField.getText().toString());
            return true;
        });

        identityField.requestFocus();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        viewModel = ViewModelProviders.of(requireActivity()).get(IdentityPromptViewModel.class);
        viewModel.requiresIdentity().observe(this, requiresIdentity -> {
            if (!requiresIdentity) {
                dismiss();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Collect_Dialog_FullScreen);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);

            // Make sure soft keyboard shows for focused field - annoyingly needed
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            setCancelable(false);
            dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialogInterface.dismiss();
                    viewModel.promptClosing();
                    return true;
                } else {
                    return false;
                }
            });
        }
    }
}