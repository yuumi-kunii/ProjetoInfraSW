package Entrega1;

import java.util.concurrent.Semaphore;
public class Pontes {
    public static class Ponte {
        private Semaphore semaphore;

        public Ponte(int numVias){
            this.semaphore = new Semaphore(numVias);
        }

        public void espere() throws InterruptedException{
            semaphore.acquire();
        }

        public void siga() throws InterruptedException{
            semaphore.release();
        }

    }
    public static class Fio implements Runnable{
        public Ponte ponte;
        public String carro;

        public Fio(Ponte ponte, String carro){
            this.ponte = ponte;
            this.carro = carro;
        }

        public void run(){
            System.out.println(carro + " está tentando atravessar...");

            try {
                ponte.espere();
                System.out.println(carro + " atravessou, Catchau!");
                Thread.sleep(4000);
                ponte.siga();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) throws InterruptedException{
        Ponte MauricioDeNassau = new Ponte(1);

        Fio Fio1 = new Fio(MauricioDeNassau, "Lightning McQueen");
        Thread Fio1_Thread = new Thread(Fio1);

        Fio Fio2 = new Fio(MauricioDeNassau, "Tom Mate");
        Thread Fio2_Thread = new Thread(Fio2);

        Fio Fio3 = new Fio(MauricioDeNassau, "Sally Carrera");
        Thread Fio3_Thread = new Thread(Fio3);

        Fio Fio4 = new Fio(MauricioDeNassau, "Ramone");
        Thread Fio4_Thread = new Thread(Fio4);

        Fio Fio5 = new Fio(MauricioDeNassau, "Doc Hudson");
        Thread Fio5_Thread = new Thread(Fio5);

        Fio Fio6 = new Fio(MauricioDeNassau, "Fillmore");
        Thread Fio6_Thread = new Thread(Fio6);

        Fio Fio7 = new Fio(MauricioDeNassau, "Dusty Rust-eze");
        Thread Fio7_Thread = new Thread(Fio7);

        Fio Fio8 = new Fio(MauricioDeNassau, "Chick Hicks");
        Thread Fio8_Thread = new Thread(Fio8);

        Fio1_Thread.start();
        Fio2_Thread.start();
        Fio3_Thread.start();
        Fio4_Thread.start();
        Fio5_Thread.start();
        Fio6_Thread.start();
        Fio7_Thread.start();
        Fio8_Thread.start();

        Fio1_Thread.join();
        Fio2_Thread.join();
        Fio3_Thread.join();
        Fio4_Thread.join();
        Fio5_Thread.join();
        Fio6_Thread.join();
        Fio7_Thread.join();
        Fio8_Thread.join();


    }
}
