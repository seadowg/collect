package org.odk.collect.android.formhierarchy;

import static org.odk.collect.android.javarosawrapper.FormControllerExt.getGroupsForCurrentIndex;
import static org.odk.collect.android.javarosawrapper.FormControllerExt.getQuestionPrompts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsUtils;
import org.odk.collect.android.audio.AudioRecordingControllerFragment;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.FormIndexAnimationHandler;
import org.odk.collect.android.formentry.FormSessionRepository;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.formentry.PrinterWidgetViewModel;
import org.odk.collect.android.formentry.SwipeHandler;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.RepeatsInFieldListException;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.ControllableLifecyleOwner;
import org.odk.collect.android.widgets.utilities.ExternalAppRecordingRequester;
import org.odk.collect.android.widgets.utilities.FormControllerWaitingForDataRegistry;
import org.odk.collect.android.widgets.utilities.InternalRecordingRequester;
import org.odk.collect.androidshared.system.IntentLauncher;
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audioclips.AudioPlayer;
import org.odk.collect.audioclips.AudioPlayerFactory;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.savepoints.SavepointsRepository;
import org.odk.collect.permissions.PermissionsProvider;

public class FormEntryFragment extends Fragment implements FormLoaderListener, FormIndexAnimationHandler.Listener {

    private final ViewModelProvider.Factory viewModelFactory;
    private final FormLoaderTask.FormEntryControllerFactory formEntryControllerFactory;
    private final Scheduler scheduler;
    private final SavepointsRepository savepointsRepository;
    private final Uri formUri;
    private final FormIndexAnimationHandler formIndexAnimationHandler = new FormIndexAnimationHandler(this);
    private final AudioRecorder audioRecorder;
    private final AudioPlayerFactory audioPlayerFactory;
    private final @NotNull PermissionsProvider permissionsProvider;
    private final @NotNull IntentLauncher intentLauncher;
    private final FormSessionRepository formSessionRepository;

    private ControllableLifecyleOwner odkViewLifecycle;
    private FormEntryViewModel formEntryViewModel;
    private FormSaveViewModel formSaveViewModel;
    private FormControllerWaitingForDataRegistry waitingForDataRegistry;
    private PrinterWidgetViewModel printerWidgetViewModel;
    private InternalRecordingRequester internalRecordingRequester;
    private ExternalAppRecordingRequester externalAppRecordingRequester;
    private ODKView odkView;

    public FormEntryFragment(ViewModelProvider.Factory viewModelFactory, FormLoaderTask.FormEntryControllerFactory formEntryControllerFactory, Scheduler scheduler, SavepointsRepository savepointsRepository, Uri formUri, AudioPlayerFactory audioPlayerFactory, AudioRecorder audioRecorder, @NotNull PermissionsProvider permissionsProvider, @NotNull IntentLauncher intentLauncher, FormSessionRepository formSessionRepository) {
        super(R.layout.form_entry);
        this.viewModelFactory = viewModelFactory;
        this.formEntryControllerFactory = formEntryControllerFactory;
        this.scheduler = scheduler;
        this.savepointsRepository = savepointsRepository;
        this.formUri = formUri;
        this.audioPlayerFactory = audioPlayerFactory;
        this.audioRecorder = audioRecorder;
        this.permissionsProvider = permissionsProvider;
        this.intentLauncher = intentLauncher;
        this.formSessionRepository = formSessionRepository;
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

        ViewModelProvider viewModelProvider = new ViewModelProvider(
                requireActivity(),
                viewModelFactory
        );

        formEntryViewModel = viewModelProvider.get(FormEntryViewModel.class);
        formSaveViewModel = viewModelProvider.get(FormSaveViewModel.class);
        printerWidgetViewModel = viewModelProvider.get(PrinterWidgetViewModel.class);

        waitingForDataRegistry = new FormControllerWaitingForDataRegistry(() -> {
            return formEntryViewModel.getFormController();
        });

        internalRecordingRequester = new InternalRecordingRequester(requireActivity(), audioRecorder, permissionsProvider);
        externalAppRecordingRequester = new ExternalAppRecordingRequester(requireActivity(), intentLauncher, waitingForDataRegistry, permissionsProvider);

        loadForm(formUri);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        formEntryViewModel.getCurrentIndex().observe(getViewLifecycleOwner(), indexAndValidationResult -> {
            if (indexAndValidationResult != null) {
                FormIndex formIndex = indexAndValidationResult.component1();
                formIndexAnimationHandler.handle(formIndex);
            }
        });
    }

    private void loadForm(Uri uri) {
        String uriMimeType = requireActivity().getContentResolver().getType(uri);
        FormLoaderTask formLoaderTask = new FormLoaderTask(uri, uriMimeType, null, null, formEntryControllerFactory, scheduler, savepointsRepository);
        formLoaderTask.setFormLoaderListener(this);
        formLoaderTask.execute();
    }

    @Override
    public void loadingComplete(FormLoaderTask task, FormDef fd, String warningMsg) {
        formControllerAvailable(task.getFormController(), task.getForm(), null);
        formEntryViewModel.refresh();
    }

    @Override
    public void loadingError(String errorMsg) {

    }

    @Override
    public void onProgressStep(String stepMessage) {

    }

    @Override
    public void onScreenChange(FormIndexAnimationHandler.@org.jetbrains.annotations.Nullable Direction direction) {
        onScreenRefresh(false);
    }

    @Override
    public void onScreenRefresh(boolean isFormStart) {
        int event = formEntryViewModel.getFormController().getEvent();

        SwipeHandler.View current = createView(event, isFormStart);
        requireView().<FrameLayout>findViewById(R.id.questionholder).removeAllViews();
        requireView().<FrameLayout>findViewById(R.id.questionholder).addView(current);

        formIndexAnimationHandler.setLastIndex(formEntryViewModel.getFormController().getFormIndex());
    }

    private void formControllerAvailable(@NonNull FormController formController, @NonNull Form form, @Nullable Instance instance) {
        formSessionRepository.set(formEntryViewModel.getSessionId(), formController, form, instance);
        AnalyticsUtils.setForm(formController);
    }

    @NotNull
    private ODKView createODKView(boolean advancingPage, FormEntryPrompt[] prompts, FormEntryCaption[] groups) {
        odkViewLifecycle = new ControllableLifecyleOwner();
        odkViewLifecycle.start();
        AudioPlayer audioPlayer = audioPlayerFactory.create(requireActivity(), odkViewLifecycle);
        audioPlayer.isLoading().observe(odkViewLifecycle, (isLoading) -> {
            requireView().findViewById(R.id.loading_screen).setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        return new ODKView(requireActivity(), prompts, groups, advancingPage, formSaveViewModel, waitingForDataRegistry, audioPlayer, audioRecorder, formEntryViewModel, printerWidgetViewModel, internalRecordingRequester, externalAppRecordingRequester, odkViewLifecycle);
    }

    /**
     * Creates and returns a new view based on the event type passed in. The view returned is
     * of type {@link View} if the event passed in represents the end of the form or of type
     * {@link ODKView} otherwise.
     *
     * @param advancingPage -- true if this results from advancing through the form
     * @return newly created View
     */
    private SwipeHandler.View createView(int event, boolean advancingPage) {
        releaseOdkView();

        FormController formController = formEntryViewModel.getFormController();

        String formTitle = formController.getFormTitle();
        requireActivity().setTitle(formTitle);

        FormEntryCaption[] groups = getGroupsForCurrentIndex(formController);
        FormEntryPrompt[] prompts;
        try {
            prompts = getQuestionPrompts(formController);
        } catch (RepeatsInFieldListException e) {
            throw new RuntimeException(e);
        }

        odkView = createODKView(advancingPage, prompts, groups);
        return odkView;
    }

    private void releaseOdkView() {
        if (odkViewLifecycle != null) {
            odkViewLifecycle.destroy();
        }

        if (odkView != null) {
            odkView = null;
        }
    }
}
