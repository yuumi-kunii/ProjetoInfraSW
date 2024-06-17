import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;


public class Main {
    public static class Conta {
        private int saldo;
        private final Lock lock = new ReentrantLock();

        public Conta(int saldo) {
            this.saldo = saldo;
        }

        public void depositar(int valor) throws InterruptedException {
            System.out.println("Esperando para sacar " + valor + "dinheiros.");
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

        public static void main(String[] args) throws InterruptedException {
            Conta Conta1 = new Conta(5);

            System.out.println(Conta1.saldo);

            Deposito Deposito1 = new De
        }
    }

}