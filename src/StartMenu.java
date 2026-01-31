
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StartMenu implements Runnable {
    public void run() {
        final JFrame startWindow = new JFrame("Chess");
        // Set window properties
        startWindow.setLocation(300,100);
        startWindow.setResizable(false);
        startWindow.setSize(300, 300);
        
        Box components = Box.createVerticalBox();
        startWindow.add(components);

        final JPanel titlePanel = new JPanel();
        components.add(titlePanel);
        final JLabel titleLabel = new JLabel("Chess");
        titlePanel.add(titleLabel);

        final JPanel blackPanel = new JPanel();
        components.add(blackPanel, BorderLayout.EAST);
        final JLabel blackPiece = new JLabel();
        try {
            Image blackImg = ImageIO.read(getClass().getResource("black_pawn.png"));
            blackPiece.setIcon(new ImageIcon(blackImg));
            blackPanel.add(blackPiece);
        } catch (Exception e) {
            System.out.println("Required game file bp.png missing");
        }
        
        
        
        final JTextField blackInput = new JTextField("Black", 10);
        blackPanel.add(blackInput);
        
        // White player selections
        final JPanel whitePanel = new JPanel();
        components.add(whitePanel);
        final JLabel whitePiece = new JLabel();
        
        try {
            Image whiteImg = ImageIO.read(getClass().getResource("white_pawn.png"));
            whitePiece.setIcon(new ImageIcon(whiteImg));
            whitePanel.add(whitePiece);
            startWindow.setIconImage(whiteImg);
        }  catch (Exception e) {
            System.out.println("Required game file wp.png missing");
        }
        
        
        final JTextField whiteInput = new JTextField("White", 10);
        whitePanel.add(whiteInput);
        
        // Timer settings
        final String[] minSecInts = new String[60];
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                minSecInts[i] = "0" + Integer.toString(i);
            } else {
                minSecInts[i] = Integer.toString(i);
            }
        }
        
        final JComboBox<String> seconds = new JComboBox<String>(minSecInts);
        final JComboBox<String> minutes = new JComboBox<String>(minSecInts);
        final JComboBox<String> hours = 
                new JComboBox<String>(new String[] {"0","1","2","3"});
        
        Box timerSettings = Box.createHorizontalBox();
        
        hours.setMaximumSize(hours.getPreferredSize());
        minutes.setMaximumSize(minutes.getPreferredSize());
        seconds.setMaximumSize(minutes.getPreferredSize());
        
        timerSettings.add(hours);
        timerSettings.add(Box.createHorizontalStrut(10));
        timerSettings.add(seconds);
        timerSettings.add(Box.createHorizontalStrut(10));
        timerSettings.add(minutes);
        
        timerSettings.add(Box.createVerticalGlue());
        
        components.add(timerSettings);
        
        // Buttons
        Box buttons = Box.createHorizontalBox();
        final JButton quit = new JButton("Quit");
        
        quit.addActionListener(e -> startWindow.dispose());
        
        final JButton instr = new JButton("Instructions");
        
        instr.addActionListener(e -> JOptionPane.showMessageDialog(startWindow,
                "To begin a new game, input player names\n" +
                "next to the pieces. Set the clocks and\n" +
                "click \"Start\". Setting the timer to all\n" +
                "zeroes begins a new untimed game.",
                "How to play",
                JOptionPane.PLAIN_MESSAGE));
        
        final JButton start = new JButton("Start");
        
        start.addActionListener(e -> {
            String bn = blackInput.getText();
            String wn = whiteInput.getText();
            int hh = Integer.parseInt((String) Objects.requireNonNull(hours.getSelectedItem()));
            int mm = Integer.parseInt((String) Objects.requireNonNull(minutes.getSelectedItem()));
            int ss = Integer.parseInt((String) Objects.requireNonNull(seconds.getSelectedItem()));

            new GameWindow(bn, wn, hh, mm, ss);
            startWindow.dispose();
        });
        
        buttons.add(start);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(instr);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(quit);
        components.add(buttons);
        
        Component space = Box.createGlue();
        components.add(space);

        startWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startWindow.setVisible(true);
    }
}
