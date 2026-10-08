package womensafety.ui;

import womensafety.service.AppServices;
import womensafety.util.FileManager;
import womensafety.util.SampleData;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        FileManager files = new FileManager();
        SampleData.initialize(files);
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception e) { System.err.println("Using default Swing theme: " + e.getMessage()); }
            new HomeFrame(new AppServices(files)).setVisible(true);
        });
    }
}
