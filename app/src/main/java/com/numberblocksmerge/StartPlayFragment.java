package com.numberblocksmerge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.numberblocksmerge.audio.HapticManager;
import com.numberblocksmerge.audio.SoundManager;

public class StartPlayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnPlay = view.findViewById(R.id.btn_start_play);
        btnPlay.setOnClickListener(v -> {
            SoundManager.getInstance().playMove();
            HapticManager.getInstance().click();
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).openHomeDashboard();
            }
        });
    }
}
