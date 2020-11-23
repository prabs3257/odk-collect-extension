package org.odk.collect.android.audio;

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
import org.odk.collect.android.databinding.AudioRecordingControllerFragmentBinding;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory;

import javax.inject.Inject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AudioRecordingControllerFragment extends Fragment {

    @Inject
    AudioRecorderViewModelFactory audioRecorderViewModelFactory;

    public AudioRecordingControllerFragmentBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AudioRecordingControllerFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final AudioRecorderViewModel viewModel = new ViewModelProvider(requireActivity(), audioRecorderViewModelFactory).get(AudioRecorderViewModel.class);

        viewModel.getCurrentSession().observe(getViewLifecycleOwner(), session -> {
            if (session == null) {
                binding.getRoot().setVisibility(GONE);
            } else if (session.getFile() == null) {
                binding.getRoot().setVisibility(VISIBLE);

                if (session.getPaused()) {
                    binding.pauseRecording.setText(R.string.resume_recording);
                    binding.pauseRecording.setOnClickListener(v -> viewModel.resume());
                } else {
                    binding.pauseRecording.setText(R.string.pause_recording);
                    binding.pauseRecording.setOnClickListener(v -> viewModel.pause());
                }
            } else {
                binding.getRoot().setVisibility(GONE);
            }
        });

        binding.stopRecording.setOnClickListener(v -> viewModel.stop());
    }
}
