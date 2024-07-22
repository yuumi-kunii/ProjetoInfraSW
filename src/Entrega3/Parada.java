package Entrega3;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class Passageiro extends Thread {
    private final ParadaDeOnibus parada;
    private final String nome;

    public Passageiro(ParadaDeOnibus parada, String nome) {
        this.parada = parada;
        this.nome = nome;
    }

    @Override
    public void run() {
        try {
            System.out.println(nome + " chegou na parada.");
            parada.prontoEmbarque();
            parada.esperarOnibus(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(nome + " foi interrompido.");
        }
    }

    public String getNome() {
        return nome;
    }
}

class ParadaDeOnibus {
    private final int capacidadeOnibus;
    private final Semaphore semaforo;
    private final Semaphore bloqueioChegada;
    private final Lock lock = new ReentrantLock();
    private final Condition onibusChegou = lock.newCondition();
    private int lugaresDisponiveis;
    private boolean onibusNaParada = false;
    public int prontosEmbarque = 0;

    public void prontoEmbarque() {
        prontosEmbarque++;
    }

    public ParadaDeOnibus(int capacidadeOnibus) {
        this.capacidadeOnibus = capacidadeOnibus;
        this.lugaresDisponiveis = capacidadeOnibus;
        this.semaforo = new Semaphore(capacidadeOnibus);
        this.bloqueioChegada = new Semaphore(1);
    }

    public void esperarOnibus(Passageiro passageiro) throws InterruptedException {
        bloqueioChegada.acquire(); // Bloqueia a chegada de novos passageiros se o ônibus estiver na parada
        try {
            semaforo.acquire(); // Aguarda até que haja espaço no ônibus

        } finally {
            bloqueioChegada.release(); // Libera a chegada de novos passageiros após adquirir espaço no ônibus
        }

        lock.lock();
        try {
            while (!onibusNaParada) {
                onibusChegou.await();
            }
            if (lugaresDisponiveis > 0) {
                lugaresDisponiveis--;
                System.out.println(passageiro.getNome() + " embarcou.");
                prontosEmbarque--;
                semaforo.release();
                if (lugaresDisponiveis == 0) {
                    System.out.println("Ônibus lotado!");
                    onibusChegou.signal();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void onibusChegou() throws InterruptedException {
        bloqueioChegada.acquire(); // Impede que novos passageiros cheguem enquanto o ônibus está na parada
        lock.lock();
        try {
            System.out.println("Ônibus chegou na parada.");
            onibusNaParada = true;
            onibusChegou.signalAll(); // Avisa todos os passageiros que o ônibus chegou

            while (lugaresDisponiveis > 0 && semaforo.availablePermits() < capacidadeOnibus) {
                onibusChegou.await(1, TimeUnit.SECONDS); // Espera que os passageiros embarquem
            }
            System.out.println("Ônibus partiu com " + (capacidadeOnibus - lugaresDisponiveis) + " passageiros.");
            // Reseta a capacidade do ônibus
            onibusNaParada = false;
            lugaresDisponiveis = capacidadeOnibus;
            onibusChegou.signalAll(); // Avisa os passageiros que o ônibus partiu
        } finally {
            lock.unlock();
            bloqueioChegada.release(); // Libera a chegada de novos passageiros após o ônibus partir
        }
    }
}

class Onibus extends Thread {
    private final ParadaDeOnibus parada;

    public Onibus(ParadaDeOnibus parada) {
        this.parada = parada;
    }

    @Override
    public void run() {
        try {
            while (true) {
                parada.onibusChegou();
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 4));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Ônibus foi interrompido.");
        }
    }
}

public class Parada {
    public static void main(String[] args) {
        final int capacidadeOnibus = 50;
        ParadaDeOnibus parada = new ParadaDeOnibus(capacidadeOnibus);

        // Cria e inicia a thread do ônibus
        Onibus onibus = new Onibus(parada);
        onibus.start();

        // Cria e inicia threads de passageiros
        for (int i = 1; i <= 100; i++) {
            Passageiro passageiro = new Passageiro(parada, "Passageiro " + i);
            passageiro.start();
            try {
                TimeUnit.MILLISECONDS.sleep(100); // Intervalo entre a chegada dos passageiros
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread principal foi interrompida.");
            }
        }
    }
}
