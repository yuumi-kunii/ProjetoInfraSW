package Entrega2;

import Entrega1.Banco;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.ArrayList;

public class Restaurante {
    private final Semaphore lugares = new Semaphore(5);
    private final ArrayList<Cliente> filaEspera = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private CyclicBarrier barrier;

    public void entrarNoRestaurante(Cliente cliente) {
        lock.lock();
        try {
            if (tentarJantar(cliente)) {
                jantar(cliente);
            } else {
                irParaFila(cliente);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private boolean tentarJantar(Cliente cliente) {
        return lugares.tryAcquire();
    }

    public void jantar(Cliente cliente) throws InterruptedException {
        System.out.println(cliente.getId() + " sentou.");
        try {
            if (lugares.availablePermits() == 0) {
               iniciarBarreira();
            } else {
               Thread.sleep(5000);
               sair(cliente);
            }

        }
        finally {
            lock.unlock();
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

    private void iniciarBarreira() {
        barrier = new CyclicBarrier(5, () -> {
            System.out.println("Clientes comendo juntos.");
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            lugares.release(5);
            System.out.println("Clientes saíram.");
            notificarProximoCliente();
        });
    }

    private void sair(Cliente cliente) {
        lock.lock();
        try {
                System.out.println("Cliente " + cliente.getId() + " saiu após 5 segundos.");
                lugares.release();
                notificarProximoCliente();
        } finally {
            lock.unlock();
        }
    }

    private void notificarProximoCliente() {
        lock.lock();
        try {
            if (!filaEspera.isEmpty() && lugares.availablePermits() == 5) {
                for (int i = 0; i < 5 && !filaEspera.isEmpty(); i++) {
                    Cliente proximoCliente = filaEspera.remove(0);
                    entrarNoRestaurante(proximoCliente);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static class Cliente implements Runnable {
        private final Restaurante restaurante;
        private final int id;

        public Cliente(Restaurante restaurante, int id) {
            this.restaurante = restaurante;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public void run() {
            restaurante.entrarNoRestaurante(this);
        }
    }

    public static void main(String[] args) {
        Restaurante restaurante = new Restaurante();
        for (int i = 1; i <= 10; i++) {
            Cliente cliente = new Cliente(restaurante, i);
            Thread thread = new Thread(cliente);
            thread.start();
        }
    }
}
