package com.redcodersgroup.numberblocks.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

public class SoundManager {
    private static SoundManager instance;
    private boolean enabled = true;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private SoundManager() {}

    public static synchronized SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    private void playTone(final double freq, final int durationMs) {
        if (!enabled) return;
        new Thread(() -> {
            try {
                int sampleRate = 22050;
                int numSamples = durationMs * sampleRate / 1000;
                double[] sample = new double[numSamples];
                byte[] generatedSnd = new byte[2 * numSamples];

                for (int i = 0; i < numSamples; ++i) {
                    double envelope = 1.0 - ((double) i / numSamples);
                    sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freq)) * envelope;
                }

                int idx = 0;
                for (final double dVal : sample) {
                    final short val = (short) ((dVal * 32767));
                    generatedSnd[idx++] = (byte) (val & 0x00ff);
                    generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
                }

                AudioTrack audioTrack = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(generatedSnd.length)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build();

                audioTrack.write(generatedSnd, 0, generatedSnd.length);
                audioTrack.play();
                Thread.sleep(durationMs + 20);
                audioTrack.release();
            } catch (Exception ignored) {}
        }).start();
    }

    public void playMove() { playTone(320.0, 40); }
    public void playMerge(int tileValue) {
        double baseFreq = 440.0;
        int exponent = Math.max(1, (int) (Math.log(tileValue) / Math.log(2)));
        double freq = baseFreq * Math.pow(1.06, exponent);
        playTone(freq, 90);
    }
    public void playMilestone() {
        playTone(523.25, 80);
        handler.postDelayed(() -> playTone(659.25, 80), 90);
        handler.postDelayed(() -> playTone(783.99, 140), 180);
    }
    public void playGameOver() {
        playTone(350.0, 100);
        handler.postDelayed(() -> playTone(280.0, 150), 110);
    }
}
