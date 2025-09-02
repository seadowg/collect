package org.odk.collect.android.formhierarchy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormDef;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioRecordingControllerFragment;
import org.odk.collect.android.databinding.FormEntryBinding;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.savepoints.SavepointsRepository;

public class FormEntryFragment extends Fragment implements FormLoaderListener {

    private final ViewModelProvider.Factory viewModelFactory;
    private final FormLoaderTask.FormEntryControllerFactory formEntryControllerFactory;
    private final Scheduler scheduler;
    private final SavepointsRepository savepointsRepository;
    private final Uri formUri;

    public FormEntryFragment(ViewModelProvider.Factory viewModelFactory, FormLoaderTask.FormEntryControllerFactory formEntryControllerFactory, Scheduler scheduler, SavepointsRepository savepointsRepository, Uri formUri) {
        super(R.layout.form_entry);
        this.viewModelFactory = viewModelFactory;
        this.formEntryControllerFactory = formEntryControllerFactory;
        this.scheduler = scheduler;
        this.savepointsRepository = savepointsRepository;
        this.formUri = formUri;
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

        loadForm(formUri);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return FormEntryBinding.inflate(inflater, container, false).getRoot();
    }

    private void loadForm(Uri uri) {
        String uriMimeType = requireActivity().getContentResolver().getType(uri);
        FormLoaderTask formLoaderTask = new FormLoaderTask(uri, uriMimeType, null, null, formEntryControllerFactory, scheduler, savepointsRepository);
        formLoaderTask.setFormLoaderListener(this);
        formLoaderTask.execute();
    }

    @Override
    public void loadingComplete(FormLoaderTask task, FormDef fd, String warningMsg) {
        requireActivity().setTitle(fd.getTitle());
    }

    @Override
    public void loadingError(String errorMsg) {

    }

    @Override
    public void onProgressStep(String stepMessage) {

    }
}
