package mixer;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AudioProcessor {
    private byte[] audioBytes;
    private AudioFormat format;
    private SourceDataLine line;
    private volatile boolean playing = false;
    private volatile double pitchOffset = 1.0;
    private volatile double amplitude = 1.0;
    private Thread playThread;

    public void loadFile(File file) throws UnsupportedAudioFileException, IOException {
        stop();
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        format = ais.getFormat();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = ais.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        audioBytes = baos.toByteArray();
        ais.close();
        System.out.println("Loaded audio. Bytes: " + audioBytes.length + " Format: " + format);
    }

    public void setPitch(double pitch) {
        this.pitchOffset = pitch; // e.g. 0.5 for half speed/lower pitch, 2.0 for double speed/higher pitch
    }

    public void setAmplitude(double amp) {
        this.amplitude = amp;
    }

    public void play() {
        if (playing)
            return;
        if (audioBytes == null)
            return;

        try {
            playing = true;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            playThread = new Thread(() -> {
                int frameSize = format.getFrameSize();
                boolean is16Bit = format.getSampleSizeInBits() == 16;
                boolean isBigEndian = format.isBigEndian();

                double position = 0;
                byte[] bufferOut = new byte[8192];

                while (playing && position < audioBytes.length / frameSize) {
                    int outIdx = 0;

                    while (outIdx + frameSize <= bufferOut.length && position < (audioBytes.length / frameSize) - 1) {
                        int frameIdx = (int) position;
                        int byteIdx = frameIdx * frameSize;

                        if (is16Bit) {
                            for (int i = 0; i < frameSize; i += 2) {
                                if (byteIdx + i + 1 < audioBytes.length) {
                                    int sample;
                                    if (isBigEndian) {
                                        sample = (audioBytes[byteIdx + i] << 8) | (audioBytes[byteIdx + i + 1] & 0xFF);
                                    } else {
                                        sample = (audioBytes[byteIdx + i + 1] << 8) | (audioBytes[byteIdx + i] & 0xFF);
                                    }

                                    // Apply Amplitude
                                    sample = (int) (sample * amplitude);

                                    // Hard clipping
                                    if (sample > 32767)
                                        sample = 32767;
                                    if (sample < -32768)
                                        sample = -32768;

                                    if (isBigEndian) {
                                        bufferOut[outIdx + i] = (byte) (sample >> 8);
                                        bufferOut[outIdx + i + 1] = (byte) (sample & 0xFF);
                                    } else {
                                        bufferOut[outIdx + i + 1] = (byte) (sample >> 8);
                                        bufferOut[outIdx + i] = (byte) (sample & 0xFF);
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < frameSize; i++) {
                                if (byteIdx + i < audioBytes.length) {
                                    bufferOut[outIdx + i] = audioBytes[byteIdx + i];
                                }
                            }
                        }

                        outIdx += frameSize;
                        position += pitchOffset;
                    }

                    if (outIdx > 0 && playing) {
                        line.write(bufferOut, 0, outIdx);
                    }
                }

                if (line != null && line.isOpen()) {
                    line.drain();
                    line.stop();
                    line.close();
                }
                playing = false;
            });
            playThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            playing = false;
        }
    }

    public void stop() {
        playing = false;
        if (playThread != null && playThread.isAlive()) {
            try {
                playThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
        }
    }

    public boolean isPlaying() {
        return playing;
    }
}
