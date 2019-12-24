package sudoku2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * bu sınıf threadi temsil eder, her çalıştırılan thread bu sınıftan çalışır
 */
public class SudokuThread extends Thread {
    public static boolean bittiMi = false; // eğer bittiyse diğer threadler çalışmaz
    public long baslangic, bitis; // süreyi tutmak için
    private List<Hamle> hamleler; // hamleleri buraya kaydediyoruz sonradan göstermek içiin
    private int[][] sudokuTahtasi;

    public SudokuThread(int[][] sudokuTahtasi) {
        this.sudokuTahtasi = sudokuTahtasi;
        hamleler = new ArrayList<>();
    }

    /**
     * Threadi çalıştıran metod
     */
    @Override
    public void run() {//thread başladıktan sonra yapılan işlem
        baslangic = System.currentTimeMillis();
        int i, j;
        if (getName().contains("1")) {//ifler rastgele satır seçiyor başlamak için
            i = new Random().nextInt(5);//0 ile 5 arası kafadan değer atar
            j = new Random().nextInt(5);//Sudokuyu çözmeye başlıycağı yeri seçiyor
        } else if (getName().contains("2")) {//Her thread farklı yerden başlıyor bu sayede
            i = new Random().nextInt(5);
            j = new Random().nextInt(5);
        } else {
            i = new Random().nextInt(5);
            j = new Random().nextInt(5);
        }
        if (coz(i, j, sudokuTahtasi)) {
            sureyiYaz();
            sudokuyuYazdir(sudokuTahtasi);
        }
    }

    /**
     * sudokuyu parametre olarak aldığı değer ile çözmeye çalışır
     * i: satır
     * j: sutun
     */
    //backtraking ile yaptık
    boolean coz(int i, int j, int[][] cozulecekSudoku) {//çözümü başltıyor
        /*
           bittiMi statik deger Java bu nesneden 1 tane oluşturuyor 
           Bütün threadler ortak kullanıyor 
           Eğer 1 thread çözerse bu değişkeni true yapar
           Ve diğer threadler bunu görür
        */
        if (bittiMi) {
            if (isAlive()) {
                stop(); // eğer başka bir thread çözdüyse bitir
                System.out.println("son hali");
                sudokuyuYazdir(cozulecekSudoku);
            }
        } 
        else {
            bekle();//Bekle metodunu çağırıyor Çok hızlı çözmesin diye
        }

        if (i == 9) {
            i = 0;
            if (++j == 9)//sütunu bir ileri taşır //Ama sütün en fazla 8 olabilir
                j = 0;//Eğer 9 olursa sutunu 0 yapar
        }

        if (bittimiBak(cozulecekSudoku)) {
            bitis = System.currentTimeMillis();
            return true;
        }

        if (cozulecekSudoku[i][j] != 0)//Bura önemli eğer bulduğumuz bir yere bakıyorsa bi sonraki alana bak boşa uğraşma diyoruz
            return coz(i + 1, j, cozulecekSudoku);

        for (int val = 1; val <= 9; ++val) {
            if (kontrolEt(i, j, val, cozulecekSudoku)) {
                cozulecekSudoku[i][j] = val;//Bulursa zaten recursion kendini çağırır oraya düşmez
                hamleler.add(new Hamle(i, j, val));
                if (coz(i + 1, j, cozulecekSudoku))//çöz metodunu tekrar çağırır ama dikkat ederseniz satırı 1 artırır öyle çağır//Recursion
                    return true;
            }
        }
        cozulecekSudoku[i][j] = 0;//Bura eğer hiçbirşey bulamazsa emniyet olarak çalışıyor 
        return false;
    }

    /**
     * cozum bulunduktan sonra hamleleri yazdıran metod
     */
    public void hamleleriYazdir() {
        System.out.println("*****************************************");
        System.out.println("*****************************************");
        System.out.println(getName() + " Hamleleri");
        int i = 0;
        for (Hamle hamle : hamleler) {
            if (i % 10 == 0) {
                System.out.println();
            }
            System.out.print(hamle + "\t");
            i++;
        }
        System.out.println();
        System.out.println("Sudokunun son durumu: ");
        sudokuyuYazdir(sudokuTahtasi);
        System.out.println("*****************************************");
        System.out.println("*****************************************");
        dosyayaHamleleriYaz();
    }

    /*
     * 20ms bekleme yaptırır threadler kendine gelsin diye
     */
    /*kod eğer çok hızlı çözerse hepsi aynı anda çözer ondan dolayı 
    0-20 arası rastgele sayı atıyor ve o kadar milisaniye bekliyor*/
    private void bekle() {
        try {
            int beklemeSuresi = new Random().nextInt(20);
            Thread.sleep(beklemeSuresi);
        } catch (InterruptedException e) {

        }
    }

    /**
     * sudoku çözülmüşmü kontrol eder
     */
    boolean bittimiBak(int[][] cozulecekSudoku) {//thread in kendi çözümü bitmişmi ona bakar
        for (int[] alan : cozulecekSudoku) {//Çift boyutlu arrayi dolaşıypr ondan çift for
            for (int rakam : alan) {
                if (rakam == 0) {
                    return false;
                }
            }
        }
        bittiMi = true;
        return true;
    }

    /**
     * sudokuya değer atamadan önce burda kontrol edilir
     * tüm kurallara uygunmu diye
     * yani satırlarda aynı numaradan olmıycak
     * sutunlarda ve kendi içindeki 3x3 kutularda aynı numara olmaması lazım
     */
    boolean kontrolEt(int i, int j, int val, int[][] cozulecekSudoku) {
        /**
         * satırları kontrol eder
         */
        for (int k = 0; k < 9; ++k)
            if (val == cozulecekSudoku[k][j])
                return false;
        /**
         * sutunları kontrol eder
         */
        for (int k = 0; k < 9; ++k)
            if (val == cozulecekSudoku[i][k])
                return false;
        /**
         * 3x3 lük alanları kontrol eder
         */
        int satirAlani = (i / 3) * 3;
        int sutunAlani = (j / 3) * 3;
        for (int k = 0; k < 3; ++k)
            for (int m = 0; m < 3; ++m)
                if (val == cozulecekSudoku[satirAlani + k][sutunAlani + m])
                    return false;

        return true;
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

    public void sureyiYaz() {
        System.out.println("sudoku " + getName() + " tarafından " + (bitis - baslangic) + " milisaniyede çözülmüştür");
    }

    private void dosyayaHamleleriYaz() {  

        try {  
            Calistir.bw.write("*****************************************\n");
            Calistir.bw.write("*****************************************\n");
            Calistir.bw.write(getName() + " Hamleleri\n");
            int h = 0;
            for (Hamle hamle : hamleler) {
                if (h % 10 == 0) {
                    Calistir.bw.newLine();
                }
                Calistir.bw.write(hamle + "\t");
                h++;
            }
            Calistir.bw.newLine();
            Calistir.bw.write("Sudokunun son durumu: \n");
            Calistir.bw.write("*****************************************\n");
            Calistir.bw.write("*****************************************\n");
            for (int i = 0; i < 9; ++i) {
                if (i % 3 == 0)
                    Calistir.bw.write(" -----------------------\n");
                for (int j = 0; j < 9; ++j) {
                    if (j % 3 == 0) Calistir.bw.write("| ");
                    Calistir.bw.write(sudokuTahtasi[i][j] == 0
                            ? "*"
                            : Integer.toString(sudokuTahtasi[i][j]));

                    Calistir.bw.write(' ');
                }
                Calistir.bw.write("|\n");
            }
            Calistir.bw.write(" -----------------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}