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
        
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                (new Thread(new Carro("direita", i))).start();
            } else {
                (new Thread(new Carro("esquerda", i))).start();
            }
        }
    }
}

