import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Carro implements Runnable {
    private final String direcao;
    private final int numero_carro;

    public Carro(String direcao, int numero_carro) {
        this.direcao = direcao;
        this.numero_carro = numero_carro;
    }

    @Override
    public void run() {
        System.out.println("Carro " + numero_carro + " da " + direcao + " entrou na ponte.");
        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Carro " + numero_carro + " da " + direcao + " saiu da ponte.");
    }
}

public class PonteSemControle {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                executor.execute(new Carro("direita", i));
            } else {
                executor.execute(new Carro("esquerda", i));
            }
        }

        executor.shutdown();
    }
}