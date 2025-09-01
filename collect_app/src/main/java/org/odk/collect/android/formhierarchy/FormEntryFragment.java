package org.odk.collect.android.formhierarchy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioRecordingControllerFragment;
import org.odk.collect.android.databinding.FormEntryBinding;
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder;

public class FormEntryFragment extends Fragment {

    private final ViewModelProvider.Factory viewModelFactory;

    public FormEntryFragment(ViewModelProvider.Factory viewModelFactory) {
        super(R.layout.form_entry);
        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getChildFragmentManager().setFragmentFactory(new FragmentFactoryBuilder()
                .forClass(
                        AudioRecordingControllerFragment.class,
                        () -> new AudioRecordingControllerFragment(viewModelFactory)
                )
                .build()
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return FormEntryBinding.inflate(inflater, container, false).getRoot();
    }
}
