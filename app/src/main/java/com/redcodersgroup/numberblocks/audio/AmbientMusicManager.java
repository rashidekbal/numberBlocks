package com.redcodersgroup.numberblocks.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Process;
import java.util.Random;

/**
 * Procedural Dreamscape Ambient Music Generator (100% Non-Copyright).
 * Mathematically synthesizes a soothing, evolving 4-chord progression
 * (Dmaj9 ➔ Bm9 ➔ Gmaj7 ➔ Asus4) featuring lush analog ambient choir pads,
 * warm Rhodes electric piano downbeat chords, and crystalline kalimba/star plucks.
 * Creates an authentic, relaxing indie game soundtrack (analogous to Monument Valley / Alto's Adventure).
 */
public class AmbientMusicManager {

    private static AmbientMusicManager instance;

    private static final int SAMPLE_RATE = 22050;
    private static final int CHUNK_SIZE = 1024;

    private AudioTrack audioTrack;
    private Thread synthThread;

    private volatile boolean isRunning = false;
    private volatile boolean isPaused = true;
    private volatile boolean isEnabled = true;

    // Master volume & smooth fading
    private float targetGain = 1.0f;
    private float currentGain = 0.0f;
    private static final float MASTER_VOLUME = 0.72f;

    // Musical Structure: 72 BPM Tempo
    // 1 Beat = (SAMPLE_RATE * 60 / 72) ≈ 18,375 samples (~0.833 sec)
    // 1 Bar = 4 Beats ≈ 73,500 samples (~3.33 sec)
    // 4 Bars = 16 Beats ≈ 13.33 sec loop cycle
    private static final int SAMPLES_PER_BEAT = (int) (SAMPLE_RATE * 60.0 / 72.0);
    private static final int SAMPLES_PER_BAR = SAMPLES_PER_BEAT * 4;

    // 4 Evolving Chords (Dmaj9 -> Bm9 -> Gmaj7 -> Asus4)
    // Voices: [Bass, Fifth, Third, Color/Ninth]
    private static final double[][] CHORD_PAD_FREQS = new double[][]{
            {146.83, 220.00, 277.18, 329.63}, // Bar 0: Dmaj9  (D3, A3, C#4, E4)
            {123.47, 185.00, 220.00, 277.18}, // Bar 1: Bm9    (B2, F#3, A3, C#4)
            {196.00, 246.94, 293.66, 369.99}, // Bar 2: Gmaj7  (G3, B3, D4, F#4)
            {164.81, 220.00, 293.66, 329.63}  // Bar 3: Asus4  (E3, A3, D4, E4)
    };

    // Pentatonic & Chord Pluck notes for the kalimba / glass star plucks
    private static final double[][] CHORD_PLUCK_NOTES = new double[][]{
            {293.66, 369.99, 440.00, 554.37, 587.33, 659.25}, // D4, F#4, A4, C#5, D5, E5
            {293.66, 329.63, 369.99, 440.00, 493.88, 554.37}, // D4, E4, F#4, A4, B4, C#5
            {293.66, 369.99, 392.00, 440.00, 493.88, 587.33}, // D4, F#4, G4, A4, B4, D5
            {293.66, 329.63, 440.00, 493.88, 587.33, 659.25}  // D4, E4, A4, B4, D5, E5
    };

    // Song position counters
    private int currentSampleInBar = 0;
    private int currentChordIndex = 0;
    private final Random random = new Random();

    // 1. Pad Oscillators (4 continuous voices with smooth portamento)
    private final double[] padCurrentFreq = new double[]{146.83, 220.00, 277.18, 329.63};
    private final double[] padPhase = new double[4];
    private final double[] padPhaseChorus = new double[4];
    private double padLfoPhase = 0;

    // 2. Rhodes Electric Piano Chords (4 struck voices with exponential decay)
    private final double[] epianoPhase = new double[4];
    private final float[] epianoAmp = new float[4];
    private double epianoTremoloPhase = 0;

    // 3. Kalimba / Celestial Star Plucks (3 polyphonic voices)
    private static final int PLUCK_VOICES = 3;
    private final double[] pluckPhase1 = new double[PLUCK_VOICES];
    private final double[] pluckPhase2 = new double[PLUCK_VOICES];
    private final double[] pluckFreq = new double[PLUCK_VOICES];
    private final float[] pluckAmp = new float[PLUCK_VOICES];
    private int pluckVoiceIndex = 0;
    private int samplesUntilNextPluck = SAMPLES_PER_BEAT / 2;

    private AmbientMusicManager() {}

    public static synchronized AmbientMusicManager getInstance() {
        if (instance == null) {
            instance = new AmbientMusicManager();
        }
        return instance;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (enabled) {
            targetGain = 1.0f;
            resume();
        } else {
            targetGain = 0.0f;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public synchronized void start() {
        if (!isEnabled) return;
        isPaused = false;
        targetGain = 1.0f;

        if (synthThread == null || !synthThread.isAlive()) {
            isRunning = true;
            initAudioTrack();
            synthThread = new Thread(this::synthLoop, "DreamscapeMusicSynthThread");
            synthThread.setPriority(Thread.NORM_PRIORITY);
            synthThread.start();
        }
    }

    public synchronized void resume() {
        if (!isEnabled) return;
        isPaused = false;
        targetGain = 1.0f;
        if (synthThread == null || !synthThread.isAlive()) {
            start();
        }
    }

    public synchronized void pause() {
        targetGain = 0.0f;
        isPaused = true;
    }

    public synchronized void stop() {
        targetGain = 0.0f;
        isPaused = true;
        isRunning = false;
        if (synthThread != null) {
            try {
                synthThread.interrupt();
            } catch (Exception ignored) {}
            synthThread = null;
        }
        releaseAudioTrack();
    }

    private void initAudioTrack() {
        try {
            int minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
            int bufferSize = Math.max(minBufferSize, CHUNK_SIZE * 4);

            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();

            audioTrack.setVolume(1.0f);
            audioTrack.play();
        } catch (Exception ignored) {}
    }

    private void releaseAudioTrack() {
        if (audioTrack != null) {
            try {
                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.pause();
                    audioTrack.flush();
                }
                audioTrack.stop();
                audioTrack.release();
            } catch (Exception ignored) {}
            audioTrack = null;
        }
    }

    private void triggerPluck(double freq, float velocity) {
        pluckFreq[pluckVoiceIndex] = freq;
        pluckAmp[pluckVoiceIndex] = velocity;
        pluckPhase1[pluckVoiceIndex] = 0;
        pluckPhase2[pluckVoiceIndex] = 0;
        pluckVoiceIndex = (pluckVoiceIndex + 1) % PLUCK_VOICES;
    }

    private void synthLoop() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        short[] buffer = new short[CHUNK_SIZE];

        while (isRunning) {
            if (isPaused || !isEnabled) {
                if (currentGain <= 0.001f) {
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
            }

            // Synthesize audio chunk
            for (int i = 0; i < CHUNK_SIZE; i++) {
                // Smooth gain ramping for seamless fade-ins and fade-outs
                if (currentGain < targetGain) {
                    currentGain = Math.min(targetGain, currentGain + 0.00045f);
                } else if (currentGain > targetGain) {
                    currentGain = Math.max(targetGain, currentGain - 0.00085f);
                }

                if (currentGain <= 0.0001f) {
                    buffer[i] = 0;
                    continue;
                }

                // Bar sequencer & chord advancement
                currentSampleInBar++;
                if (currentSampleInBar >= SAMPLES_PER_BAR) {
                    currentSampleInBar = 0;
                    currentChordIndex = (currentChordIndex + 1) % CHORD_PAD_FREQS.length;

                    // Trigger Rhodes Electric Piano downbeat chord
                    for (int v = 0; v < 4; v++) {
                        epianoAmp[v] = 0.36f;
                        epianoPhase[v] = 0;
                    }
                }

                // Occasional Rhodes ghost chord syncopation on beat 3 (75% through bar)
                if (currentSampleInBar == SAMPLES_PER_BEAT * 3) {
                    for (int v = 1; v < 4; v++) {
                        epianoAmp[v] = 0.18f;
                    }
                }

                // 1. Synthesize Lush Ambient Choir / String Pad
                // Modulated by slow 0.11 Hz breathing filter LFO
                padLfoPhase += (2.0 * Math.PI * 0.11) / SAMPLE_RATE;
                double padLfo = 0.65 + 0.35 * Math.sin(padLfoPhase);

                double padSample = 0;
                double[] targetChord = CHORD_PAD_FREQS[currentChordIndex];
                for (int v = 0; v < 4; v++) {
                    // Smooth portamento pitch glide between chords
                    padCurrentFreq[v] += (targetChord[v] - padCurrentFreq[v]) * 0.00035;

                    double f = padCurrentFreq[v];
                    padPhase[v] += (2.0 * Math.PI * f) / SAMPLE_RATE;
                    padPhaseChorus[v] += (2.0 * Math.PI * (f + 0.65)) / SAMPLE_RATE; // Detuned chorusing shimmer

                    // Main voice + detuned chorus + soft octave harmonic
                    double voice = Math.sin(padPhase[v]) * 0.65 +
                            Math.sin(padPhaseChorus[v]) * 0.35 +
                            Math.sin(padPhase[v] * 2.0) * 0.12;

                    padSample += voice;
                }
                padSample = (padSample / 4.0) * padLfo;

                // 2. Synthesize Rhodes Electric Piano
                // Mellow warm timbre with 4.2 Hz tremolo modulation
                epianoTremoloPhase += (2.0 * Math.PI * 4.2) / SAMPLE_RATE;
                double tremolo = 0.85 + 0.15 * Math.sin(epianoTremoloPhase);

                double epianoSample = 0;
                for (int v = 0; v < 4; v++) {
                    if (epianoAmp[v] > 0.001f) {
                        double f = targetChord[v];
                        epianoPhase[v] += (2.0 * Math.PI * f) / SAMPLE_RATE;

                        // Rhodes tone: Fundamental + warm 2nd harmonic + subtle bell 3rd harmonic
                        double tone = Math.sin(epianoPhase[v]) * 0.70 +
                                Math.sin(epianoPhase[v] * 2.0) * 0.22 +
                                Math.sin(epianoPhase[v] * 3.0) * 0.08;

                        epianoSample += tone * epianoAmp[v];
                        // Organic exponential acoustic decay
                        epianoAmp[v] *= 0.99984f;
                    }
                }
                epianoSample *= tremolo;

                // 3. Kalimba / Crystalline Star Plucks Scheduler
                samplesUntilNextPluck--;
                if (samplesUntilNextPluck <= 0) {
                    double[] notes = CHORD_PLUCK_NOTES[currentChordIndex];
                    double chosenNote = notes[random.nextInt(notes.length)];
                    float vel = 0.32f + random.nextFloat() * 0.14f;
                    triggerPluck(chosenNote, vel);

                    // Organic melodic interval (1 to 2 beats, humanized swing)
                    int beatOffset = random.nextBoolean() ? SAMPLES_PER_BEAT : (SAMPLES_PER_BEAT / 2);
                    samplesUntilNextPluck = (int) (beatOffset * (0.95 + random.nextDouble() * 0.25));
                }

                // Synthesize active Kalimba voices
                double pluckSample = 0;
                for (int p = 0; p < PLUCK_VOICES; p++) {
                    if (pluckAmp[p] > 0.001f) {
                        pluckPhase1[p] += (2.0 * Math.PI * pluckFreq[p]) / SAMPLE_RATE;
                        pluckPhase2[p] += (2.0 * Math.PI * pluckFreq[p] * 2.76) / SAMPLE_RATE; // Glass bell overtone

                        double note = Math.sin(pluckPhase1[p]) * 0.75 + Math.sin(pluckPhase2[p]) * 0.25;
                        pluckSample += note * pluckAmp[p];

                        // Exponential bell chime decay
                        pluckAmp[p] *= 0.99965f;
                    }
                }

                // Composite output mixing
                double mixed = (padSample * 0.42 + epianoSample * 0.40 + pluckSample * 0.42) * MASTER_VOLUME * currentGain;

                // Soft saturation limiter to guarantee zero digital clipping
                double clamped = Math.tanh(mixed);
                buffer[i] = (short) (clamped * 32767.0);
            }

            if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING && !isPaused && isEnabled) {
                    try {
                        audioTrack.play();
                    } catch (Exception ignored) {}
                }
                try {
                    audioTrack.write(buffer, 0, CHUNK_SIZE);
                } catch (Exception ignored) {
                    break;
                }
            }
        }
    }
}
