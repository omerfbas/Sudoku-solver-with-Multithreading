package sudoku2;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Calistir {

    public static BufferedWriter bw = null;// hamleleri dosyaya yazmak icin 
    public static FileWriter fw = null;

    static {
        try {
            fw = new FileWriter("hamleler.txt");
            bw = new BufferedWriter(fw);
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {
        new Calistir();
    }


    public Calistir() {
        int[][] sudokuTahtasi = new int[9][9];
        /**
         * seçilen dosyadan sudokuyu okur ve array haline getirir
         */
        try (Stream<String> stream = Files.lines(Paths.get(dosyayiIste().getAbsolutePath()))) {//Kullanıcıdan dosya seçmesini ister seçilen dosyayı satır satır okur
            int j = 0;
            for (Object satir : stream.toArray()) {
                for (int i = 0; i < satir.toString().length(); i++) {
                    if (satir.toString().charAt(i) == '*') {
                        sudokuTahtasi[j][i] = 0;
                    } else {
                        sudokuTahtasi[j][i] = Integer.parseInt("" + satir.toString().charAt(i));
                    }
                }
                j += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Çözülecek Sudoku");
        sudokuyuYazdir(sudokuTahtasi);

        /**
         * Threadler burda verilen isimleriyle başlatılır
         */
        SudokuThread thread1 = new SudokuThread(copyTahta(sudokuTahtasi));
        SudokuThread thread2 = new SudokuThread(copyTahta(sudokuTahtasi));
        SudokuThread thread3 = new SudokuThread(copyTahta(sudokuTahtasi));
        thread1.setName("Cozucu 1");
        thread2.setName("Cozucu 2");
        thread3.setName("Cozucu 3");
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.interrupt();//İş bitince tüm threadler dursun diye
        thread2.interrupt();//Zaten o interrupt kısmına gelmesi için birinin bitmesi şazım
        thread3.interrupt();
        /**
         * threadler bittikten sonra hamle kontrolü yapılır
         */
        boolean thread1Bittimi = false;
        boolean thread2Bittimi = false;
        boolean thread3Bittimi = false;

        while (!thread1Bittimi) {
            if (!thread1.isAlive()) {
                thread1Bittimi = true;
                thread1.hamleleriYazdir();
            }
        }
        while (!thread2Bittimi) {
            if (!thread2.isAlive()) {
                thread2Bittimi = true;
                thread2.hamleleriYazdir();
            }
        }
        while (!thread3Bittimi) {
            if (!thread3.isAlive()) {
                thread3Bittimi = true;
                thread3.hamleleriYazdir();
            }
        }
        try {
            if (Calistir.bw != null)//dosyayı kapatır yazdıktan sonraa
                Calistir.bw.close();
            if (Calistir.fw != null)//dosyayı kapatır yazdıktan sonr
                Calistir.fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * kullaniciya dosya isteme ekrani acar
     * aldigi dosyasi doner
     */
    private File dosyayiIste() {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        File secilenDosya = null;
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            secilenDosya = jfc.getSelectedFile();
        }
        return secilenDosya;
    }
/*
    Burda biz tahtayı her thread için kopyalıyıp threade veriyoruz çözsün diye 
    tüm threadler aynı tahtayı verirsek biri biteri değiştirdiği an bütün çözüm bozulur
    */
    private int[][] copyTahta(int[][] tahta) {
        int[][] kopyaTahta = new int[9][9];

        int i = 0, j = 0;
        for (int[] ints : tahta) {
            for (int deger : ints) {
                kopyaTahta[i][j] = deger;
                j++;
            }
            i++;
            j = 0;
        }
        return kopyaTahta;
    }

    void sudokuyuYazdir(int[][] solution) {
        for (int i = 0; i < 9; ++i) {
            if (i % 3 == 0)
                System.out.println(" -----------------------");
            for (int j = 0; j < 9; ++j) {
                if (j % 3 == 0) System.out.print("| ");
                System.out.print(solution[i][j] == 0
                        ? "*"
                        : Integer.toString(solution[i][j]));

                System.out.print(' ');
            }
            System.out.println("|");
        }
        System.out.println(" -----------------------");
    }
}
