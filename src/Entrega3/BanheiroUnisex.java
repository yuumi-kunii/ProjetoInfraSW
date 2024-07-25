package Entrega3;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BanheiroUnisex {
    public static class Banheiro {
        private final Semaphore lugares = new Semaphore(3);
        private final LinkedList<Pessoa> filaEspera = new LinkedList<>();
        private final Lock filaLock = new ReentrantLock();
        private String sinal = null;
        private final CountDownLatch latch;

        public Banheiro(CountDownLatch latch) {
            this.latch = latch;
        }

        public void chegarNoBanheiro(Pessoa pessoa) {
            System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") foi ao toalete.");
            filaLock.lock();
            try {
                if (sinal == null || sinal.equals(pessoa.getGenero())) {
                    if (lugares.tryAcquire()) {
                        if (sinal == null) {
                            sinal = pessoa.getGenero();
                        }
                        filaLock.unlock();
                        entrar(pessoa);
                    } else {
                        filaEspera.add(pessoa);
                        System.out.println("Pessoa " + pessoa.getId() + " está esperando na fila.");
                        filaLock.unlock();
                    }
                } else {
                    filaEspera.add(pessoa);
                    System.out.println("Pessoa " + pessoa.getId() + " está esperando na fila.");
                    filaLock.unlock();
                }
            } catch (Exception e) {
                filaLock.unlock();
                throw new RuntimeException(e);
            }
        }

        public void entrar(Pessoa pessoa) {
            try {
                System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") entrou no toalete.");
                Thread.sleep(new Random().nextInt(3000)); // Simula o tempo de uso do banheiro

                sair(pessoa);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void sair(Pessoa pessoa) {
            System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") saiu do toalete.");
            lugares.release();
            filaLock.lock();
            try {
                if (lugares.availablePermits() == 3) {
                    sinal = null;
                    System.out.println("Banheiro está vazio.");
                }
                notificarProximaPessoa();
            } finally {
                filaLock.unlock();
            }
            latch.countDown();
        }

        private void notificarProximaPessoa() {
            filaLock.lock();
            try {
                while (lugares.availablePermits() > 0 && !filaEspera.isEmpty()) {
                    Pessoa proximaPessoa = filaEspera.peek();
                    if (sinal == null || sinal.equals(proximaPessoa.getGenero())) {
                        filaEspera.poll(); // Remove a pessoa da fila
                        if (sinal == null) {
                            sinal = proximaPessoa.getGenero();
                        }
                        new Thread(() -> entrar(proximaPessoa)).start();
                    } else {
                        break;
                    }
                }
            } finally {
                filaLock.unlock();
            }
        }
    }

    public static class Pessoa implements Runnable {
        private final Banheiro banheiro;
        private final int id;
        private final String genero;

        public Pessoa(Banheiro banheiro, int id, String genero) {
            this.banheiro = banheiro;
            this.id = id;
            this.genero = genero;
        }

        public int getId() {
            return id;
        }

        public String getGenero() {
            return genero;
        }

        public void run() {
            banheiro.chegarNoBanheiro(this);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int totalPessoas = 100;
        CountDownLatch latch = new CountDownLatch(totalPessoas);
        Banheiro banheiroUnico = new Banheiro(latch);
        Random random = new Random();

        for (int i = 1; i <= totalPessoas; i++) {
            String genero = random.nextBoolean() ? "mulher" : "homem";
            Pessoa pessoa = new Pessoa(banheiroUnico, i, genero);
            Thread thread = new Thread(pessoa);
            thread.start();
            Thread.sleep(random.nextInt(1000));
        }

        // Espera até que todas as pessoas tenham terminado de usar o banheiro
        latch.await();
        System.out.println("Todas as pessoas terminaram de usar o banheiro.");
    }
}
