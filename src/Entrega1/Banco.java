package Entrega1;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Banco {
        public static class Conta {
            private int saldo;
            private final Lock lock = new ReentrantLock();

            public Conta(int saldo) {
                this.saldo = saldo;
            }

            public void depositar(Usuario usuario, int valor) throws InterruptedException {
                System.out.println(usuario.pegarNome() + " esperando para depositar " + valor + " dinheiros...");
                lock.lock();
                try {
                    saldo += valor;
                    System.out.println(usuario.pegarNome() + " depositou " + valor + " dinheiros.");
                    System.out.println("O saldo atual é: " + saldo);
                }
                finally {
                    Thread.sleep(4000);
                    lock.unlock();
                }
            }

            public void sacar(Usuario usuario, int valor) throws InterruptedException {
                System.out.println(usuario.pegarNome() + " esperando para sacar " + valor + " dinheiros...");
                lock.lock();
                try {
                    if (saldo >= valor) {
                        saldo -= valor;
                        System.out.println(usuario.pegarNome() + " sacou " + valor + " dinheiros.");
                        System.out.println("O saldo atual é: " + saldo);
                    } else {
                        System.out.println("Saldo insuficiente para " + usuario.pegarNome() + " sacar " + valor + " dinheiros.");
                    }

                }
                finally {
                    Thread.sleep(3000);
                    lock.unlock();
                }
            }
        }

        public static class Usuario {
            private String nome;

            public Usuario(String nome) {
                this.nome = nome;
            }
            public String pegarNome() {
                return nome;
            }
        }

        public static class Deposito implements Runnable {
            private int valorDeposito;
            private Conta conta;
            private Usuario usuario;

            public Deposito(Conta conta, Usuario usuario, int valorDeposito) {
                this.valorDeposito = valorDeposito;
                this.conta = conta;
                this.usuario = usuario;
            }

            public void run() {
                try {
                    conta.depositar(usuario, valorDeposito);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        public static class Saque implements Runnable {
            private int valorSaque;
            private Conta conta;
            private Usuario usuario;

            public Saque(Conta conta, Usuario usuario, int valorSaque) {
                this.conta = conta;
                this.usuario = usuario;
                this.valorSaque = valorSaque;
            }

            public void run() {
                try {
                    conta.sacar(usuario, valorSaque);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }



        public static void main(String[] args) throws InterruptedException {
            Conta Conta1 = new Conta(1500);

            Usuario TioPatinhas = new Usuario("Tio Patinhas");
            Usuario PatoDonald = new Usuario("Pato Donald");
            Usuario Huguinho = new Usuario("Huguinho");
            Usuario Zezinho = new Usuario("Zezinho");
            Usuario Luisinho = new Usuario("Luisinho");

            System.out.println("O saldo inicial é: " + Conta1.saldo);

            Deposito Deposito1 = new Deposito(Conta1, TioPatinhas, 1340);
            Saque Saque1 = new Saque(Conta1, Huguinho, 1234);
            Saque Saque2 = new Saque(Conta1, Zezinho, 689);
            Deposito Deposito2 = new Deposito(Conta1, PatoDonald, 956);
            Saque Saque3 = new Saque(Conta1, Luisinho, 236);
            Saque Saque4 = new Saque(Conta1, Zezinho, 1678);
            Deposito Deposito3 = new Deposito(Conta1, PatoDonald, 325);
            Saque Saque5 = new Saque(Conta1, Luisinho, 795);
            Saque Saque6 = new Saque(Conta1, Huguinho, 1234);
            Saque Saque7 = new Saque(Conta1, Zezinho, 689);
            Deposito Deposito4 = new Deposito(Conta1, PatoDonald, 164);
            Saque Saque8 = new Saque(Conta1, Luisinho, 236);

            Thread TD1 = new Thread(Deposito1);
            Thread TD2 = new Thread(Saque1);
            Thread TD3 = new Thread(Saque2);
            Thread TD4 = new Thread(Deposito2);
            Thread TD5 = new Thread(Saque3);
            Thread TD6 = new Thread(Saque4);
            Thread TD7 = new Thread(Deposito3);
            Thread TD8 = new Thread(Saque5);
            Thread TD9 = new Thread(Saque6);
            Thread TD10 = new Thread(Saque7);
            Thread TD11 = new Thread(Deposito4);
            Thread TD12 = new Thread(Saque8);

            TD1.start();
            TD2.start();
            TD3.start();
            TD4.start();
            TD5.start();
            TD6.start();
            TD7.start();
            TD8.start();
            TD9.start();
            TD10.start();
            TD11.start();
            TD12.start();

            TD1.join();
            TD2.join();
            TD3.join();
            TD4.join();
            TD5.join();
            TD6.join();
            TD7.join();
            TD8.join();
            TD9.join();
            TD10.join();
            TD11.join();
            TD12.join();

            System.out.println("O saldo final é:" + Conta1.saldo);
        }

}
