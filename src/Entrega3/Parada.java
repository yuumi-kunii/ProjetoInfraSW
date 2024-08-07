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

            parada.getLock().lock();
            try {
                //Se o passageiro chegar e tiver um ônibus no embarque, ele aguarda para embarcar no próximo
                while (parada.onibusNaParada) {
                    parada.getOnibusChegouCondition().await();
                }
            } finally {
                parada.getLock().unlock();
            }
            parada.esperarOnibus(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getNome() {
        return nome;
    }
}

class ParadaDeOnibus {
    private final int capacidadeOnibus;
    private final Semaphore semaforoEmbarque;
    private final Lock lock = new ReentrantLock();
    private final Condition onibusChegou = lock.newCondition();
    private int lugaresDisponiveis;
    public boolean onibusNaParada = false;
    public int passageirosEsperando = 0;

    public ParadaDeOnibus(int capacidadeOnibus) {
        this.capacidadeOnibus = capacidadeOnibus;
        this.lugaresDisponiveis = capacidadeOnibus;
        this.semaforoEmbarque = new Semaphore(0); // Inicialmente bloqueado
    }

    public void esperarOnibus(Passageiro passageiro) throws InterruptedException {
        passageirosEsperando++;
        semaforoEmbarque.acquire(); // Aguarda até que haja espaço no ônibus

        lock.lock(); //Lock necessário para consulta de variáveis compartilhadas
        try {
            //Passageiro espera a chegada do ônibus para embarcar
            while (!onibusNaParada) {
                onibusChegou.await();
            }
            if (lugaresDisponiveis > 0) {
                lugaresDisponiveis--;
                System.out.println(passageiro.getNome() + " embarcou."); //Registro do embarque
                passageirosEsperando--;
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
        lock.lock();
        try {
            System.out.println("Ônibus chegou na parada.");
            onibusNaParada = true;
            int passageirosParaEmbarcar = Math.min(passageirosEsperando, capacidadeOnibus);
            onibusChegou.signalAll(); // Avisa os passageiros que o ônibus chegou
            semaforoEmbarque.release(passageirosParaEmbarcar); // Libera todos os lugares embarcados de uma vez para simular concorrência

            // Espera que os passageiros embarquem enquanto houverem passageiros liberados para embarcar e vagas no ônibus
            while (passageirosEsperando > 0 && lugaresDisponiveis > 0) {
                onibusChegou.await(1, TimeUnit.SECONDS);
            }
            System.out.println("Ônibus partiu com " + (capacidadeOnibus - lugaresDisponiveis) + " passageiros.");
            //Encerra a thread do ônibus quando acabam os passageiros
            if ((capacidadeOnibus - lugaresDisponiveis) == 0) {
                System.out.println("Acabaram os passageiros, a parada fechou!");
                Thread.currentThread().interrupt();
            }
            //Resetando as variáveis da parada
            onibusNaParada = false;
            lugaresDisponiveis = capacidadeOnibus;
            onibusChegou.signalAll(); // Avisa os passageiros que o ônibus partiu
        } finally {
            lock.unlock();
        }
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getOnibusChegouCondition() {
        return onibusChegou;
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
            //Chega dos ônibus em intervalos de 1 a 4 segundos
            while (true) {
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 4));
                parada.onibusChegou();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Parada {
    public static void main(String[] args) {
        final int capacidadeOnibus = 50;
        ParadaDeOnibus parada = new ParadaDeOnibus(capacidadeOnibus);

        Onibus onibus = new Onibus(parada);
        onibus.start();

        // Cria e inicia threads de passageiros
        for (int i = 1; i <= 100; i++) {
            Passageiro passageiro = new Passageiro(parada, "Passageiro " + i);
            passageiro.start();
            try {
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(0, 100)); // Intervalo entre a chegada dos passageiros
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread principal foi interrompida.");
            }
        }
    }
}
