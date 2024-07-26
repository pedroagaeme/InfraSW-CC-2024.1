import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Barbearia {
    private final int capacidade;
    private final ReentrantLock lock;
    private final Condition novoCliente;
    private final ClienteBarbearia[] cadeira;
    private int cadeirasOcupadas;
    private int proxCadeira;
    private int vez;
    private boolean precisaAcordar;

    public Barbearia(int capacidade) {
        this.capacidade = capacidade;
        this.lock = new ReentrantLock();
        this.novoCliente = lock.newCondition();
        this.cadeirasOcupadas = 0;
        this.cadeira = new ClienteBarbearia[capacidade];
        this.proxCadeira = 0;
        this.vez = 0;
        this.precisaAcordar = false;
    }

    public void atender(Barbeiro barbeiro) throws InterruptedException {
        while(true) {
            this.lock.lock();
            try {
                if(this.cadeirasOcupadas == 0) {
                    this.precisaAcordar = true;
                    System.out.println("Sem clientes, barbeiro foi dormir...");
                    while(cadeirasOcupadas == 0) {
                        this.novoCliente.await();
                    }
                    System.out.println("Barbeiro Acordou!");
                }
                this.cadeirasOcupadas--;
            }
            finally {
                this.lock.unlock();
            }
            ClienteBarbearia clienteEmAtendimento = this.cadeira[this.vez];
            System.out.println(clienteEmAtendimento.checarNome() + " passou a ser atendido pelo barbeiro.");
            this.vez = (this.vez + 1) % this.capacidade;
            Thread.sleep(50);
            System.out.println(clienteEmAtendimento.checarNome() + " terminou de ser atendido pelo barbeiro.");
        }
    }

    public void entrarBarbearia(ClienteBarbearia cliente) throws InterruptedException {
        Thread.sleep((long) (Math.random() * 5000));
        System.out.println(cliente.checarNome() + " entrou na barbearia.");
        this.lock.lock();
        try {
            if (this.cadeirasOcupadas < this.capacidade) {
                this.cadeirasOcupadas++;
                System.out.println(cliente.checarNome() + " sentou na cadeira " + (this.proxCadeira + 1) + ". Cadeiras Ocupadas: " + this.cadeirasOcupadas + ".");
                this.cadeira[this.proxCadeira] = cliente;
                this.proxCadeira = (this.proxCadeira + 1) % this.capacidade;
                if (this.precisaAcordar) {
                    System.out.println(cliente.checarNome() + " foi acordar o barbeiro...");
                    this.novoCliente.signal();
                    this.precisaAcordar = false;
                }
            }
            else {
                System.out.println(cliente.checarNome() + " foi embora por falta de cadeiras de espera.");
            }
        } finally {
            this.lock.unlock();
        }
    }
}

class Barbeiro implements Runnable {
    private final Barbearia barbearia;
    private final int tempoDeCorte;

    public Barbeiro(Barbearia barbearia, int tempoDeCorte) {
        this.barbearia = barbearia;
        this.tempoDeCorte = tempoDeCorte;
    }

    @Override
    public void run() {
        try {
            this.barbearia.atender(this);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ClienteBarbearia implements Runnable {
    private final String nome;
    private final Barbearia barbearia;


    public ClienteBarbearia(Barbearia barbearia, String nome) {
        this.barbearia = barbearia;
        this.nome = nome;
    }

    @Override
    public void run() {
        try {
        this.barbearia.entrarBarbearia(this);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String checarNome() {
        return this.nome;
    }
}

public class SimuladorBarbearia {
    public static void main(String[] args) {
        Barbearia barbearia = new Barbearia(5);
        Thread clientes[] = new Thread [100];

        Thread barbeiro =  new Thread(new Barbeiro(barbearia, 50));
        barbeiro.start();

        int numeroDeClientes = 100; 
        for (int i = 0; i < numeroDeClientes; i++) {
            clientes[i] = new Thread(new ClienteBarbearia(barbearia, "Cliente " + (i + 1)));
        }
        for (int i = 0; i < numeroDeClientes; i++) {
            clientes[i].start();
        }
    }
}