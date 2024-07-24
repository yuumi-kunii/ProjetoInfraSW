package Entrega3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BanheiroUnisex {
    public static class Banheiro {
        private static final int MAX_CAPACITY = 3;
        private int currentCount = 0;
        private String currentGender = "";
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        public void entrarBanheiro(Pessoa pessoa) throws InterruptedException {
            lock.lock();
            try {
                while (!currentGender.isEmpty() && !currentGender.equals(pessoa.pegarGenero()) || currentCount >= MAX_CAPACITY) {
                    condition.await();
                }
                currentCount++;
                if (currentCount == 1) {
                    currentGender = pessoa.pegarGenero();
                }
                System.out.println(pessoa.pegarNome() + " (" + pessoa.pegarGenero() + ") entrou no banheiro.");
            } finally {
                lock.unlock();
            }
        }

        public void sairBanheiro(Pessoa pessoa) {
            lock.lock();
            try {
                currentCount--;
                System.out.println(pessoa.pegarNome() + " (" + pessoa.pegarGenero() + ") liberou o trono.");
                if (currentCount == 0) {
                    currentGender = "";
                }
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public static class Pessoa {
        private final String nome;
        private final String genero;

        public Pessoa(String nome, String genero) {
            this.nome = nome;
            this.genero = genero;
        }

        public String pegarNome() {
            return nome;
        }

        public String pegarGenero() {
            return genero;
        }
    }

    public static class UsoBanheiro implements Runnable {
        private final Banheiro banheiro;
        private final Pessoa pessoa;

        public UsoBanheiro(Banheiro banheiro, Pessoa pessoa) {
            this.banheiro = banheiro;
            this.pessoa = pessoa;
        }

        public void run() {
            try {
                banheiro.entrarBanheiro(pessoa);
                Thread.sleep((long) (Math.random() * 1000));
                banheiro.sairBanheiro(pessoa);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Banheiro banheiro = new Banheiro();

        Pessoa[] pessoas = {
                new Pessoa("João", "Masculino"),
                new Pessoa("Maria", "Feminino"),
                new Pessoa("Pedro", "Masculino"),
                new Pessoa("Ana", "Feminino"),
                new Pessoa("Carlos", "Masculino"),
                new Pessoa("Paula", "Feminino")
        };

        Thread[] threads = new Thread[pessoas.length];
        for (int i = 0; i < pessoas.length; i++) {
            threads[i] = new Thread(new UsoBanheiro(banheiro, pessoas[i]));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Interditado para limpeza.");
    }
}
