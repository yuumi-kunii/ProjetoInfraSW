package Entrega2;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Restaurante {
    public static class Mesa {
        private final Semaphore lugares = new Semaphore(5);
        private final LinkedList<Cliente> filaEspera = new LinkedList<>();
        private final Lock lock = new ReentrantLock();
        private CyclicBarrier barrier;

        public Mesa() {
            resetBarrier();
        }

        private void resetBarrier() {
            barrier = new CyclicBarrier(5, () -> {
                System.out.println("Clientes saindo juntos...");
                lugares.release(5);
                notificarProximoCliente();
            });
        }

        public void chegarNoRestaurante(Cliente cliente) {
            System.out.println("Cliente " + cliente.getId() + " chegou no restaurante.");
            try {
                lock.lock();
                if (lugares.tryAcquire()) {
                    lock.unlock();
                    jantar(cliente);
                } else {
                    filaEspera.add(cliente);
                    System.out.println("Cliente " + cliente.getId() + " está esperando na fila.");
                    lock.unlock();
                }
            } catch (Exception e) {
                lock.unlock();
                throw new RuntimeException(e);
            }
        }

        public void jantar(Cliente cliente) throws InterruptedException {
            System.out.println("Cliente " + cliente.getId() + " está jantando.");
            Thread.sleep(2000); // Tempo de jantar
            verificarSaida(cliente);
        }

        public void verificarSaida(Cliente cliente) {
            try {
                if (lugares.availablePermits() == 0 && !filaEspera.isEmpty()) {
                    System.out.println("Cliente " + cliente.getId() + " terminou de jantar e está esperando os outros.");
                    barrier.await();
                    if (barrier.getNumberWaiting() == 0) {
                        resetBarrier();
                    }
                } else {
                    System.out.println("Cliente " + cliente.getId() + " terminou de jantar e saiu sozinho.");
                    sair(cliente);
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        public void sair(Cliente cliente) {
            lugares.release();
            notificarProximoCliente();
        }

        private void notificarProximoCliente() {
            lock.lock();
            try {
                while (lugares.availablePermits() > 0 && !filaEspera.isEmpty()) {
                    Cliente proximoCliente = filaEspera.removeFirst();
                    lugares.acquire();
                    new Thread(() -> {
                        try {
                            jantar(proximoCliente);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    public static class Cliente implements Runnable {
        private final Mesa mesa;
        private final int id;

        public Cliente(Mesa mesa, int id) {
            this.mesa = mesa;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void run() {
            mesa.chegarNoRestaurante(this);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Mesa mesaUnica = new Mesa();
        Random random = new Random();

        for (int i = 1; i <= 100; i++) {
            Cliente cliente = new Cliente(mesaUnica, i);
            Thread thread = new Thread(cliente);
            thread.start();
            Thread.sleep(random.nextInt(1000));
        }
    }
}
