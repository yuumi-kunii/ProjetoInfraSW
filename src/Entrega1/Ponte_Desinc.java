package Entrega1;

public class Ponte_Desinc {
    public static class Ponte {
        private boolean ponteBloqueada;

        public Ponte() {
            this.ponteBloqueada = false;
        }

        public synchronized boolean tentarAtravessar(String carro) {
            if (!ponteBloqueada) {
                ponteBloqueada = true;
                System.out.println(carro + " está atravessando a ponte...");
                return true;
            } else {
                System.out.println(carro + " não conseguiu atravessar, ponte bloqueada!");
                return false;
            }
        }

        public synchronized void sair(String carro) {
            ponteBloqueada = false;
            System.out.println(carro + " saiu da ponte.");
        }
    }

    public static class Fio implements Runnable {
        private Ponte ponte;
        private String carro;

        public Fio(Ponte ponte, String carro) {
            this.ponte = ponte;
            this.carro = carro;
        }

        public void run() {
            try {
                boolean atravessou = ponte.tentarAtravessar(carro);
                if (atravessou) {
                    Thread.sleep(4000); // Simula o tempo de travessia
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Ponte MauricioDeNassau = new Ponte();

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
