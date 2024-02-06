import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Kliker extends JFrame {

    private Robot robot;
    private int czasKlikania = 100;
    private int maskaPrzycisku = InputEvent.BUTTON1_DOWN_MASK;
    private JButton startPrzycisk;
    private JButton stopPrzycisk;
    private JLabel statusLabel;
    private JTextField poleInterwalu;
    private boolean klikanie;

    public Kliker() {
        super("Automatyczny Kliker");
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        setLayout(new FlowLayout());

        startPrzycisk = new JButton("Start-F5");
        stopPrzycisk = new JButton("Stop-F6");
        statusLabel = new JLabel("<html>Klikanie jest <b style=color:red;>zatrzymane</b></html>");
        poleInterwalu = new JTextField("1", 3);

        startPrzycisk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rozpocznijKlikanie();
            }
        });

        stopPrzycisk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zatrzymajKlikanie();
            }
        });

        add(startPrzycisk);
        add(stopPrzycisk);
        add(statusLabel);
        JPanel panelInterwalu = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        panelInterwalu.add(new JLabel("Interwał sekundowy:")); 
        add(panelInterwalu);
        add(poleInterwalu);

        stopPrzycisk.setEnabled(false);
        setSize(250, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Nie udało się zainicjować JNativeHook: " + ex.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_F5) {
                    rozpocznijKlikanie();
                } else if (e.getKeyCode() == NativeKeyEvent.VC_F6) {
                    zatrzymajKlikanie();
                }
            }
        });

        poleInterwalu.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0' && c <= '9') || c == '.' || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    e.consume();
                    JOptionPane.showMessageDialog(Kliker.this, "Dozwolone są tylko cyfry, kropka lub przecinek.", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void rozpocznijKlikanie() {
        final double interwalSekundy;
        try {
            interwalSekundy = Double.parseDouble(poleInterwalu.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowy format interwału.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (interwalSekundy <= 0) {
            JOptionPane.showMessageDialog(this, "Interwał musi być większy niż zero.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        klikanie = true;
        statusLabel.setText("<html>Klikanie jest <b style=color:green;>uruchomione</b><html>");
        startPrzycisk.setEnabled(false);
        stopPrzycisk.setEnabled(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (klikanie) {
                    robot.mousePress(maskaPrzycisku);
                    try {
                        Thread.sleep(czasKlikania);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    robot.mouseRelease(maskaPrzycisku);
                    try {
                        Thread.sleep((long)(interwalSekundy * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void zatrzymajKlikanie() {
        klikanie = false;
        statusLabel.setText("<html>Klikanie jest <b style=color:red;>zatrzymane</b></html>");
        startPrzycisk.setEnabled(true);
        stopPrzycisk.setEnabled(false);
    }

    public static void main(String[] args) {
        new Kliker();
    }
}
