import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

class Onibus extends Thread {
    private Parada parada;

    public Onibus(Parada parada) {
        this.parada = parada;
    }

    @Override
    public void run() {
        try {
            this.parada.chegarParada(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Passageiro extends Thread {
    private String nome;
    private Parada parada;

    public Passageiro(String nome, Parada parada) {
        this.nome = nome;
        this.parada = parada;
    }

    public String getNome() {
        return this.nome;
    }

    @Override
    public void run() {
        try {
            this.parada.entrarOnibus(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Parada {
    private final int CAPACIDADE = 50;
    private int numeroPassageirosNoOnibus = 0;
    private int passageirosPrevistos = 100;
    private ArrayList<Passageiro> passageirosEsperando = new ArrayList<>();
    private ArrayList<Passageiro> passageirosNoOnibus = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private int contador = 0;

    public void chegarParada(Onibus onibus) throws InterruptedException {
        while (this.passageirosPrevistos > 0) {
            this.lock.lock();
            try {
                System.out.println("Onibus chegou na parada.");
                this.numeroPassageirosNoOnibus = 0;
                Collections.shuffle(this.passageirosEsperando); 
                while (this.numeroPassageirosNoOnibus < this.CAPACIDADE && !this.passageirosEsperando.isEmpty()) {
                    Passageiro passageiro = this.passageirosEsperando.get(0);
                    this.numeroPassageirosNoOnibus++;
                    this.passageirosEsperando.remove(0);
                    this.passageirosPrevistos--;
                    this.passageirosNoOnibus.add(passageiro);
                }
            } finally {
                this.lock.unlock();
            }
            while (!this.passageirosNoOnibus.isEmpty()) {
                Passageiro passageiro = this.passageirosNoOnibus.get(0);
                this.passageirosNoOnibus.remove(0);
                this.contador++;
                System.out.println(passageiro.getNome() + " entrou no onibus. Lugares Ocupados: " + this.contador);
                Thread.sleep((long) (Math.random() * 10));
            }
            this.contador = 0;
            this.lock.lock();
            try {
                if (this.numeroPassageirosNoOnibus > 0) {
                    System.out.println("Onibus partiu com " + this.numeroPassageirosNoOnibus + " passageiros.");
                } else {
                    System.out.println("Onibus partiu vazio.");
                }
            } finally {
                this.lock.unlock();
            }
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        }
    }

    public void entrarOnibus(Passageiro passageiro) throws InterruptedException {
        Thread.sleep((long) (Math.random() * 10000));
        this.lock.lock();
        try {
            System.out.println(passageiro.getNome() + " chegou na parada.");
            this.passageirosEsperando.add(passageiro);
        } finally {
            this.lock.unlock();
        }
    }
}

public class SimuladorOnibus {
    public static void main(String[] args) {
        Parada parada = new Parada();

        for (int i = 1; i <= 100; i++) {
            new Passageiro("Passageiro " + i, parada).start();
        }
        new Onibus(parada).start();
    }
}
