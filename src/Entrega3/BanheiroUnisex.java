package Entrega3;

import Entrega2.Restaurante;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BanheiroUnisex {
    public static class Banheiro {
        private final Semaphore lugares = new Semaphore(3); // Gerencia o número de vagas no banheiro
        private final LinkedList<Pessoa> filaEspera = new LinkedList<>(); // Fila de espera das pessoas
        private final Lock filaLock = new ReentrantLock(); // Lock para gerenciar a fila de espera
        private String sinal = null; // Indica o gênero da pessoa que está no banheiro

        // Método que direciona a pessoa que chega para o banheiro ou para a fila
        public void chegarNoBanheiro(Pessoa pessoa) throws InterruptedException {
            System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") foi ao toalete.");
            // Verifica se a pessoa pode entrar no banheiro imediatamente ou se precisa esperar
            if ((sinal == null || sinal.equals(pessoa.getGenero())) && lugares.availablePermits() > 0 && filaEspera.isEmpty()) {
                if (sinal == null) {
                    sinal = pessoa.getGenero(); // Define o sinal se o banheiro estava vazio
                }
                // Adquire um lugar no banheiro
                lugares.acquire();
                entrar(pessoa); // Método para a pessoa entrar no banheiro
            } else {
                System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") está esperando na fila.");
                esperar(pessoa);
            }

        }

        private void esperar(Pessoa pessoa) {
            filaLock.lock();
            try {
                filaEspera.add(pessoa);
            } finally {
                filaLock.unlock();
            }
        }

        private void entrar(Pessoa pessoa) {
            try {
                System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") entrou no toalete.");
                Thread.sleep(new Random().nextInt(1000)); // Simula o tempo de uso do banheiro
                sair(pessoa); // Método para a pessoa sair do banheiro
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void sair(Pessoa pessoa) {
            System.out.println("Pessoa " + pessoa.getId() + " (" + pessoa.getGenero() + ") saiu do toalete.");
            lugares.release(); // Libera um lugar no banheiro
            try {
                if (lugares.availablePermits() == 3) {
                    sinal = null; // Se o banheiro está vazio, o sinal volta a ser nulo
                    System.out.println("Banheiro está vazio.");
                }
                notificarProximaPessoa(); // Chama a próxima pessoa da fila
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        private void notificarProximaPessoa() {
            filaLock.lock();
            try {
                // Enquanto houver pessoas na fila e vagas disponíveis, processa as pessoas na fila
                while (!filaEspera.isEmpty() && lugares.availablePermits() > 0) {
                    // Retorna a primeira pessoa da fila sem removê-la
                    Pessoa proximaPessoa = filaEspera.peek();

                    // Verifica se o gênero da pessoa na fila é o mesmo que o sinal ou se o sinal está nulo
                    if (sinal == null || sinal.equals(proximaPessoa.getGenero())) {
                        // Altera o sinal se o banheiro estava vazio
                        if (sinal == null) {
                            sinal = proximaPessoa.getGenero();
                        }
                        filaEspera.removeFirst();
                        lugares.acquire();
                        new Thread(() -> {
                            entrar(proximaPessoa);
                        }).start();
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                filaLock.unlock();
            }
        }
    }

    // Criação da classe Pessoa com o banheiro, id e o gênero
    public static class Pessoa implements Runnable {
        private final Banheiro banheiro;
        private final int id;
        private final String genero;

        // Construtor da classe
        public Pessoa(Banheiro banheiro, int id, String genero) {
            this.banheiro = banheiro;
            this.id = id;
            this.genero = genero;
        }

        // Método que retorna o id da pessoa
        public int getId() {
            return id;
        }

        // Método que retorna o gênero
        public String getGenero() {
            return genero;
        }

        // Método chamado quando a thread é iniciada
        public void run() {
            try {
                banheiro.chegarNoBanheiro(this);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Banheiro banheiroUnico = new Banheiro(); // Instância do banheiro
        Random random = new Random(); // Gerador de números aleatórios

        // Loop para inicializar as threads
        for (int i = 1; i <= 100; i++) {
            String genero = random.nextBoolean() ? "mulher" : "homem"; // Define um gênero aleatoriamente
            Pessoa pessoa = new Pessoa(banheiroUnico, i, genero); // Instância da pessoa
            Thread thread = new Thread(pessoa); // Instância da thread
            thread.start(); // Inicia a thread
            Thread.sleep(random.nextInt(500)); // Tempo aleatório para criação das threads
        }
    }
}
