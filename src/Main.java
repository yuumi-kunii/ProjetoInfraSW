public class Main {
    public static class Pessoa {
        public String nome;

        public Pessoa(String nome) {
            this.nome = nome;
        }
    }
    public static class Usuario extends Pessoa implements Runnable {
        public Usuario (String nome) {
            super(nome);
        }

        public void run() {
            for(int i = 0; i < 3; i++) {
                System.out.println("Professor " + nome + ": " + i );
            }
        }
    }

}