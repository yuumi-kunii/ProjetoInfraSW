package Entrega2;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barbearia {

    public static class Barbeiro implements Runnable {
        private int numCadeiras;
        private Semaphore semaphoreCadeiras;
        private Semaphore semaphoreCorte;
        private Lock lock;
        private boolean dormindo;


        public Barbeiro(int numCadeiras) {
            this.numCadeiras = numCadeiras;
            this.semaphoreCadeiras = new Semaphore(numCadeiras, true); // Semaphore para controlar as cadeiras disponíveis na sala de espera
            this.semaphoreCorte = new Semaphore(1);
            this.lock = new ReentrantLock();
            this.dormindo = true;

        }

        public void dormir() throws InterruptedException {
            lock.lock();
            try {
                System.out.println("O expediente foi longo! O Barbeiro vai tirar um cochilinho...");
                this.dormindo = true;
            } finally {
                lock.unlock();
            }
        }

        public void atender(String cliente) throws InterruptedException {

            try {
                semaphoreCorte.acquire();

                System.out.println("Figarô! Barbeiro está atendendo " + cliente);
                Thread.sleep(5000); // Simula o tempo de corte de cabelo
                System.out.println(cliente + " atendido!");


                // Verifica se há mais clientes na fila
                if (semaphoreCadeiras.availablePermits() == numCadeiras - 1) {
                    dormir();
                }

                semaphoreCorte.release();

            }  catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void acordar(String cliente) {
            lock.lock();
            try {
                System.out.println("Opa! O barbeiro estava dormindo e o " + cliente + " precisou acordá-lo");
                this.dormindo = false;

            } finally {
                lock.unlock();
            }
        }

        public void entrarFila() throws InterruptedException {
            semaphoreCadeiras.acquire();
        }

        public void liberarFila() throws InterruptedException {
            semaphoreCadeiras.release();
        }

        public boolean isDormindo() {
            lock.lock();
            try {
                return dormindo;
            } finally {
                lock.unlock();
            }
        }
        public void run() {
            System.out.println("A barbearia abriu!");
        }

    }

    public static class Cliente implements Runnable {
        private Barbeiro barbeiro;
        private String nome;

        public Cliente(Barbeiro barbeiro, String nome) {
            this.barbeiro = barbeiro;
            this.nome = nome;
        }

        public void run() {

            System.out.println(nome + " chegou");
            if (barbeiro.isDormindo()) {
                barbeiro.acordar(nome);
            } else if (barbeiro.semaphoreCadeiras.availablePermits() == 0) {
                System.out.println("Barbearia lotada! " + nome + " foi embora cabeludo.");
                return;
            }

            try {
                barbeiro.entrarFila();
                System.out.println(nome + " sentou em uma cadeira.");
                barbeiro.atender(nome);
                barbeiro.liberarFila();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }

    public static void main(String[] args) throws InterruptedException {
        Barbeiro barbeiro = new Barbeiro(5);
        Thread threadBarbeiro = new Thread(barbeiro);
        threadBarbeiro.start();



        for (int i = 1; i <= 10; i++) {
            Thread cliente = new Thread(new Cliente(barbeiro, "Cliente " + i));

            cliente.start();
            Thread.sleep(500); // Intervalo para simular chegada de novos clientes
        }
        Thread.sleep(20000);
        for (int i = 11; i <= 100; i++) {
            Thread cliente = new Thread(new Cliente(barbeiro, "Cliente " + i));

            cliente.start();
            Thread.sleep(500); // Intervalo para simular chegada de novos clientes
        }


        threadBarbeiro.interrupt(); // Interrompe a thread do barbeiro após atender todos os clientes

    }
}
