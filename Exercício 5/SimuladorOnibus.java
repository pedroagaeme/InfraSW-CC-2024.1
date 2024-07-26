import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

class Parada {
    private final int CAPACIDADE = 50;
    private int numeroPassageirosNoOnibus = 0;
    private int passageirosPrevistos = 100;
    private ArrayList<Passageiro> passageirosEsperando = new ArrayList<>();
    private ArrayList<Passageiro> passageirosNoOnibus = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private int contador = 0;

    public void chegarParada(Onibus onibus) throws InterruptedException {
        while (passageirosPrevistos > 0) {
            lock.lock();
            try {
                System.out.println("Onibus chegou na parada.");
                numeroPassageirosNoOnibus = 0;
                Collections.shuffle(passageirosEsperando); 
                while (numeroPassageirosNoOnibus < CAPACIDADE && !passageirosEsperando.isEmpty()) {
                    Passageiro passageiro = passageirosEsperando.get(0);
                    numeroPassageirosNoOnibus++;
                    passageirosEsperando.remove(0);
                    passageirosPrevistos--;
                    passageirosNoOnibus.add(passageiro);
                }
            } finally {
                lock.unlock();
            }
            while (!passageirosNoOnibus.isEmpty()) {
                Passageiro passageiro = passageirosNoOnibus.get(0);
                passageirosNoOnibus.remove(0);
                contador++;
                System.out.println(passageiro.getNome() + " entrou no onibus. Lugares Ocupados: " + contador);
                Thread.sleep((long) (Math.random() * 10));
            }
            contador = 0;
            lock.lock();
            try {
                if (numeroPassageirosNoOnibus > 0) {
                    System.out.println("Onibus partiu com " + numeroPassageirosNoOnibus + " passageiros.");
                } else {
                    System.out.println("Onibus partiu vazio.");
                }
            } finally {
                lock.unlock();
            }
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        }
    }

    public void entrarOnibus(Passageiro passageiro) throws InterruptedException {
        Thread.sleep((long) (Math.random() * 10000));
        lock.lock();
        try {
            System.out.println(passageiro.getNome() + " chegou na parada.");
            passageirosEsperando.add(passageiro);
        } finally {
            lock.unlock();
        }
    }
}

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
        return nome;
    }

    @Override
    public void run() {
        try {
            parada.entrarOnibus(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class SimuladorOnibus {
    public static void main(String[] args) throws InterruptedException {
        Parada parada = new Parada();

        for (int i = 1; i <= 100; i++) {
            new Passageiro("Passageiro " + i, parada).start();
        }
        new Onibus(parada).start();
    }
}
