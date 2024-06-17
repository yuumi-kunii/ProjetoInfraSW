import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Banco {
        public static class Conta {
            private int saldo;
            private final Lock lock = new ReentrantLock();

            public Conta(int saldo) {
                this.saldo = saldo;
            }

            public void depositar(int valor) throws InterruptedException {
                System.out.println("Esperando para depositar " + valor + " dinheiros.");
                lock.lock();
                try {
                    saldo += valor;
                    System.out.println("Valor depositado: " + valor);
                }
                finally {
                    Thread.sleep(3);
                    lock.unlock();
                }
            }

            public void sacar(int valor) throws InterruptedException {
                System.out.println("Esperando para sacar " + valor + " dinheiros.");
                lock.lock();
                try {
                    saldo -= valor;
                    System.out.println("Valor sacado: " + valor);
                }
                finally {
                    Thread.sleep(3);
                    lock.unlock();
                }
            }
        }

        public static class Deposito implements Runnable {
            private int valorDeposito;
            private Conta conta;

            public Deposito(int valorDeposito, Conta conta) {
                this.valorDeposito = valorDeposito;
                this.conta = conta;
            }

            public void run() {
                try {
                    conta.depositar(valorDeposito);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public static class Saque implements Runnable {
            private int valorSaque;
            private Conta conta;

            public Saque(int valorSaque, Conta conta) {
                this.valorSaque = valorSaque;
                this.conta = conta;
            }

            public void run() {
                try {
                    conta.sacar(valorSaque);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        

        public static void main(String[] args) throws InterruptedException {
            Conta Conta1 = new Conta(5);

            System.out.println(Conta1.saldo);

            Deposito Deposito1 = new Deposito(50, Conta1);
            Deposito Deposito2 = new Deposito(32, Conta1);
            Saque Saque1 = new Saque(21, Conta1);
            Saque Saque2 = new Saque(3, Conta1);


            Thread TD1 = new Thread(Deposito1);
            Thread TD2 = new Thread(Deposito2);
            Thread TD3 = new Thread(Saque1);
            Thread TD4 = new Thread(Saque2);

            TD1.start();
            TD2.start();
            TD3.start();
            TD4.start();

            TD1.join();
            TD2.join();
            TD3.join();
            TD4.join();

            System.out.println(Conta1.saldo);
        }

}
