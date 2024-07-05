import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Cliente implements Runnable {
    private final String nome;
    private final Restaurante restaurante;
    private boolean vaga;


    public Cliente(Restaurante restaurante, String nome) {
        this.restaurante = restaurante;
        this.nome = nome;
        this.vaga = false;
    }

    @Override
    public void run() {
        try {
            this.restaurante.entrarRestaurante(this);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String checarNome() {
        return this.nome;
    }

    public void garantirVaga() {
        this.vaga = true;
    }

    public boolean checarVaga() {
        return this.vaga;
    }

}

class ControleDeVagas implements Runnable {
    private final Restaurante restaurante;

    public ControleDeVagas(Restaurante restaurante) {
        this.restaurante  = restaurante;
    }

    @Override
    public void run() {
        try {
            this.restaurante.liberarVagas();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


class Restaurante {
    private final int CAPACIDADE;
    private  int vagasLivres;
    private final ArrayList<Cliente> fila;
    private final ReentrantLock lock;
    private final Condition esvaziou;
    private final Condition novasVagas;
    private int vagasEmUso;
 
    public Restaurante() {
        this.CAPACIDADE = 5;
        this.vagasLivres = 0;
        this.lock = new ReentrantLock();
        this.esvaziou = lock.newCondition();
        this.novasVagas = lock.newCondition();
        this.vagasEmUso = 0;
        this.fila = new ArrayList<>();
    }

    public void liberarVagas() throws InterruptedException {
        while (true) {
            this.lock.lock();
            try {
                while (vagasEmUso == CAPACIDADE) {
                    this.vagasLivres = 0;
                    this.esvaziou.await();
                }
                if(!fila.isEmpty()) {
                    Cliente cliente = this.fila.get(0);
                    this.fila.remove(0);
                    String proximo = "sem proximo";
                    if(!this.fila.isEmpty()) {
                        proximo = this.fila.get(0).checarNome();
                    }
                    System.out.println(cliente.checarNome() + " saiu da fila de espera. Proximo da fila: " + proximo + ".");
                    this.vagasEmUso++;
                    cliente.garantirVaga();
                    this.novasVagas.signalAll();
                }
                else {
                    while(vagasLivres + vagasEmUso < CAPACIDADE) {
                        this.vagasLivres++;
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
    }
    
    public void entrarRestaurante(Cliente cliente) throws InterruptedException {
        System.out.println(cliente.checarNome() + " entrou no restaurante e vai tentar comer.");
        this.lock.lock();
        try {
            if (this.vagasLivres == 0) {
                this.fila.add(cliente);
                System.out.println(cliente.checarNome() + " entrou na fila de espera. Primeiro da Fila: " + fila.get(0).checarNome() + ".");
                while(cliente.checarVaga() == false)
                    this.novasVagas.await();
            }
            else {
            this.vagasEmUso++;
            cliente.garantirVaga();
            this.vagasLivres--;
            }
            }
         finally {
            lock.unlock();
        }
    
        if (cliente.checarVaga() == true) {
            System.out.println(cliente.checarNome() + " sentou-se e vai comer.");
            Thread.sleep((long) (Math.random() * 1000));
            this.lock.lock();
            try {
                vagasEmUso--;
                System.out.println(cliente.checarNome() + " terminou de comer e saiu. Lugares vazios: " + (5 - vagasEmUso) + ".");
                if(this.vagasEmUso == 0) {
                    this.esvaziou.signal();
                }
            }
            finally {
                this.lock.unlock();
            }
        }
    }
    
}

public class SimuladorRestaurante {
    public static void main(String[] args) {
        Restaurante restaurante = new Restaurante();
        Thread clientes[] = new Thread [100];

        Thread controleDeVagas =  new Thread(new ControleDeVagas(restaurante));
        controleDeVagas.start();

        int numeroDeConsumidores = 100; 
        for (int i = 0; i < numeroDeConsumidores; i++) {
            clientes[i] = new Thread(new Cliente(restaurante, "Cliente " + (i + 1)));
        }
        for (int i = 0; i < numeroDeConsumidores; i++) {
            clientes[i].start();
        }
    }
}