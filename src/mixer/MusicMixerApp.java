package mixer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MusicMixerApp extends JFrame {

    private AudioProcessor processor;
    private JLabel currentFileLabel;
    private JSlider pitchSlider;
    private JSlider amplitudeSlider;
    private JButton playButton;
    private JButton stopButton;
    private JLabel pitchValueLabel;
    private JLabel amplitudeValueLabel;

    public MusicMixerApp() {
        super("Music Mixer App Rahul Pal RollNo: 49");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        processor = new AudioProcessor();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JButton loadFileButton = new JButton("Load .WAV File");
        currentFileLabel = new JLabel("No file selected", SwingConstants.CENTER);
        currentFileLabel.setForeground(Color.DARK_GRAY);
        topPanel.add(loadFileButton, BorderLayout.WEST);
        topPanel.add(currentFileLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel pitchPanel = new JPanel(new BorderLayout());
        pitchPanel.setBorder(BorderFactory.createTitledBorder("Pitch (Speed)"));
        pitchSlider = new JSlider(50, 200, 100);
        pitchSlider.setMajorTickSpacing(50);
        pitchSlider.setMinorTickSpacing(10);
        pitchSlider.setPaintTicks(true);
        pitchSlider.setPaintLabels(true);
        pitchValueLabel = new JLabel("1.0x", SwingConstants.CENTER);
        pitchPanel.add(pitchSlider, BorderLayout.CENTER);
        pitchPanel.add(pitchValueLabel, BorderLayout.EAST);

        JPanel amplitudePanel = new JPanel(new BorderLayout());
        amplitudePanel.setBorder(BorderFactory.createTitledBorder("Amplitude (Volume)"));
        amplitudeSlider = new JSlider(0, 200, 100);
        amplitudeSlider.setMajorTickSpacing(50);
        amplitudeSlider.setMinorTickSpacing(10);
        amplitudeSlider.setPaintTicks(true);
        amplitudeSlider.setPaintLabels(true);
        amplitudeValueLabel = new JLabel("1.0x", SwingConstants.CENTER);
        amplitudePanel.add(amplitudeSlider, BorderLayout.CENTER);
        amplitudePanel.add(amplitudeValueLabel, BorderLayout.EAST);

        centerPanel.add(pitchPanel);
        centerPanel.add(amplitudePanel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        playButton = new JButton("▶");
        stopButton = new JButton("■");
        playButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        playButton.setEnabled(false);
        stopButton.setEnabled(false);
        bottomPanel.add(playButton);
        bottomPanel.add(stopButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("WAV Files (*.wav)", "wav"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    processor.loadFile(file);
                    currentFileLabel.setText(file.getName());
                    playButton.setEnabled(true);
                    stopButton.setEnabled(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading WAV file: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        playButton.addActionListener(e -> {
            processor.stop();
            if (!processor.isPlaying()) {
                processor.play();
            }
        });

        stopButton.addActionListener(e -> {
            processor.stop();
        });

        pitchSlider.addChangeListener(e -> {
            double value = pitchSlider.getValue() / 100.0;
            processor.setPitch(value);
            pitchValueLabel.setText(String.format("%.1fx", value));
        });

        amplitudeSlider.addChangeListener(e -> {
            double value = amplitudeSlider.getValue() / 100.0;
            processor.setAmplitude(value);
            amplitudeValueLabel.setText(String.format("%.1fx", value));
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MusicMixerApp().setVisible(true);
        });
    }
}
