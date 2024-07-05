package Entrega2;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

public class Restaurante {
    public static class Mesa {
        private final Semaphore lugares = new Semaphore(5);
        private final ArrayList<Cliente> filaEspera = new ArrayList<>();
        private final Lock lock = new ReentrantLock();
        private CyclicBarrier barrier = new CyclicBarrier(5, () -> {
            System.out.println("Clientes saindo juntos...");
            lugares.release(5);
            for (int i = 1; i <= 5; i++) {
                notificarProximoCliente();
            }
        });

        public void chegarNoRestaurante(Cliente cliente) {
            System.out.println("Cliente " + cliente.getId() + " chegou no restaurante.");
            try {
                if (lugares.tryAcquire() && filaEspera.isEmpty()) {
                    jantar(cliente);
                } else {
                    irParaFila(cliente);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void jantar(Cliente cliente) {
            System.out.println("Cliente " + cliente.getId() + " está comendo.");
            try {
                Thread.sleep(3000);
                verificarSaida(cliente);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        public void irParaFila(Cliente cliente) {
            lock.lock();
            try {
                filaEspera.add(cliente);
                System.out.println("Cliente " + cliente.getId() + " está esperando na fila.");
            } finally {
                lock.unlock();
            }
        }

        public void verificarSaida(Cliente cliente) {
            try {
                if (lugares.availablePermits() == 0 && !filaEspera.isEmpty()) {
                    System.out.println("Cliente " + cliente.getId() + " terminou de comer e está esperando os outros.");
                    barrier.await();
                } else {
                    sair(cliente);
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        public void sair(Cliente cliente) {
            System.out.println("Cliente " + cliente.getId() + " terminou de comer e saiu.");
            lugares.release();
            notificarProximoCliente();
        }

        private void notificarProximoCliente() {
            try {
                while (!filaEspera.isEmpty() && lugares.availablePermits() > 0) {
                    Cliente proximoCliente = filaEspera.remove(0);
                    lugares.acquire();
                    jantar(proximoCliente);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
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

        for (int i = 1; i <= 10; i++) {
            Cliente cliente = new Cliente(mesaUnica, i);
            Thread thread = new Thread(cliente);
            thread.start();
            Thread.sleep(random.nextInt(1000));
        }
    }
}
