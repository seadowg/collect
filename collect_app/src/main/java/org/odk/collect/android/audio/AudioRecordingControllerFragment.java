package org.odk.collect.android.audio;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.databinding.AudioRecordingControllerFragmentBinding;
import org.odk.collect.android.formentry.BackgroundAudioViewModel;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.audiorecorder.Consumable;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.strings.format.LengthFormatterKt;

import javax.inject.Inject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.android.utilities.DialogUtils.showIfNotShowing;
import static org.odk.collect.android.utilities.LiveDataUtils.zip4;

public class AudioRecordingControllerFragment extends Fragment {

    @Inject
    AudioRecorder audioRecorder;

    @Inject
    FormEntryViewModel.Factory formEntryViewModelFactory;

    @Inject
    BackgroundAudioViewModel.Factory backgroundAudioViewModelFactory;

    public AudioRecordingControllerFragmentBinding binding;
    private FormEntryViewModel formEntryViewModel;
    private BackgroundAudioViewModel backgroundAudioViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        formEntryViewModel = new ViewModelProvider(requireActivity(), formEntryViewModelFactory).get(FormEntryViewModel.class);
        backgroundAudioViewModel = new ViewModelProvider(requireActivity(), backgroundAudioViewModelFactory).get(BackgroundAudioViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AudioRecordingControllerFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        zip4(
                formEntryViewModel.hasBackgroundRecording(),
                backgroundAudioViewModel.isBackgroundRecordingEnabled(),
                audioRecorder.getCurrentSession(),
                audioRecorder.failedToStart()
        ).observe(getViewLifecycleOwner(), quad -> {
            boolean hasBackgroundRecording = quad.first;
            boolean isBackgroundRecordingEnabled = quad.second;
            RecordingSession session = quad.third;
            Consumable<Exception> failedToStart = quad.fourth;

            update(hasBackgroundRecording, isBackgroundRecordingEnabled, session, failedToStart);
        });

        binding.stopRecording.setOnClickListener(v -> audioRecorder.stop());
    }

    private void update(boolean hasBackgroundRecording, boolean isBackgroundRecordingEnabled, RecordingSession session, Consumable<Exception> failedToStart) {
        if (!failedToStart.isConsumed() && failedToStart.getValue() != null) {
            showIfNotShowing(AudioRecordingErrorDialogFragment.class, getParentFragmentManager());
        }

        if (session != null) {
            if (session.getFile() == null) {
                binding.getRoot().setVisibility(VISIBLE);
                renderRecordingInProgress(session);
            } else {
                binding.getRoot().setVisibility(GONE);
            }
        } else {
            if (hasBackgroundRecording && failedToStart.getValue() != null) {
                binding.getRoot().setVisibility(VISIBLE);
                renderRecordingProblem(TranslationHandler.getString(requireContext(), R.string.start_recording_failed));
            } else if (hasBackgroundRecording && !isBackgroundRecordingEnabled) {
                binding.getRoot().setVisibility(VISIBLE);
                renderRecordingProblem(TranslationHandler.getString(requireContext(), R.string.recording_disabled, "⋮"));
            } else {
                binding.getRoot().setVisibility(GONE);
            }
        }
    }

    private void renderRecordingProblem(String string) {
        binding.recordingIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_mic_off_24));
        binding.timeCode.setText(string);
        binding.waveform.setVisibility(GONE);
        binding.pauseRecording.setVisibility(GONE);
        binding.stopRecording.setVisibility(GONE);
    }

    private void renderRecordingInProgress(RecordingSession session) {
        binding.timeCode.setText(LengthFormatterKt.formatLength(session.getDuration()));
        binding.waveform.addAmplitude(session.getAmplitude());

        if (session.getPaused()) {
            binding.pauseRecording.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_mic_24));
            binding.pauseRecording.setContentDescription(getString(R.string.resume_recording));
            binding.pauseRecording.setOnClickListener(v -> audioRecorder.resume());

            binding.recordingIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause_24dp));
        } else {
            binding.pauseRecording.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause_24dp));
            binding.pauseRecording.setContentDescription(getString(R.string.pause_recording));
            binding.pauseRecording.setOnClickListener(v -> {
                audioRecorder.pause();
                formEntryViewModel.logFormEvent(AnalyticsEvents.AUDIO_RECORDING_PAUSE);
            });

            binding.recordingIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_mic_24));
        }

        // Pause not available before API 24
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            binding.pauseRecording.setVisibility(GONE);
        }

        if (backgroundAudioViewModel.isBackgroundRecording()) {
            binding.pauseRecording.setVisibility(GONE);
            binding.stopRecording.setVisibility(GONE);
        }
    }
}
