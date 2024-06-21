import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Carro implements Runnable {
    private final String direcao;
    private final int numero_carro;
    private final Ponte ponte;

    public Carro(String direcao, int numero_carro, Ponte ponte) {
        this.direcao = direcao;
        this.numero_carro = numero_carro;
        this.ponte = ponte;
    }

    @Override
    public void run() {
        ponte.atravessar(this);
    }

    public String getDirecao() {
        return direcao;
    }

    public int getNumero_carro() {
        return numero_carro;
    }
}

class Ponte {
    private final Lock lock = new ReentrantLock();

    public void atravessar(Carro carro) {
        lock.lock();
        try {
            System.out.println("Carro " + carro.getNumero_carro() + " da " + carro.getDirecao() + " entrou na ponte.");
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Carro " + carro.getNumero_carro() + " da " + carro.getDirecao() + " saiu da ponte.");
        } finally {
            lock.unlock();
        }
    }
}

public class PonteComControle {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Ponte ponte = new Ponte();

        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                executor.execute(new Carro("direita", i, ponte));
            } else {
                executor.execute(new Carro("esquerda", i, ponte));
            }
        }

        executor.shutdown();
    }
}
