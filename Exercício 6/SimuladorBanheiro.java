import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Banheiro {
    private final int CAPACIDADE = 3;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition vezHomens = lock.newCondition();
    private final Condition vezMulheres = lock.newCondition();
    private int homensNoBanheiro = 0;
    private int mulheresNoBanheiro = 0;
    private int mulheresEsperando = 0;
    private int homensEsperando = 0;
    private int contador = 0;

    public void entrarHomem(Pessoa homem) throws InterruptedException {
        System.out.println(homem.checarNome() + " quer entrar no Banheiro.");
        this.lock.lock();
        try {
            while (contador == CAPACIDADE || mulheresNoBanheiro > 0) {
                homensEsperando++;
                vezHomens.await();
                homensEsperando--;
            }
            contador++;
            homensNoBanheiro++;
            System.out.println(homem.checarNome() + " entrou no banheiro. Homens: " + homensNoBanheiro);
        } finally {
            this.lock.unlock();
        }
        Thread.sleep((long) (Math.random() * 1000));
        this.lock.lock();
        try {
            homensNoBanheiro--;
            System.out.println(homem.checarNome() + " saiu do banheiro. Homens restantes: " + homensNoBanheiro);
            if (mulheresEsperando > 0) {
                if (homensNoBanheiro == 0) {
                    contador = 0;
                    vezMulheres.signalAll();
                }
            }
            else {
                contador--; 
                if (homensEsperando > 0) {
                    vezHomens.signalAll();
                }
            }
        }
        finally {
            this.lock.unlock();
        }
    }

    public void entrarMulher(Pessoa mulher) throws InterruptedException {
        System.out.println(mulher.checarNome() + " quer entrar no Banheiro.");
        this.lock.lock();
        try {
            while (contador == CAPACIDADE || homensNoBanheiro > 0) {
                mulheresEsperando++;
                vezMulheres.await();
                mulheresEsperando--;
            }
            contador++;
            mulheresNoBanheiro++;
            System.out.println(mulher.checarNome() + " entrou no banheiro. Mulheres: " + mulheresNoBanheiro);
        } finally {
            this.lock.unlock();
        }
        Thread.sleep((long) (Math.random() * 1000));
        this.lock.lock();
        try {
            mulheresNoBanheiro--;
            System.out.println(mulher.checarNome() + " saiu do banheiro. Mulheres restantes: " + mulheresNoBanheiro);
            if (homensEsperando > 0) {
                if (mulheresNoBanheiro == 0) {
                    contador = 0;
                    vezHomens.signalAll();
                }
            }
            else {
                contador--; 
                if (mulheresEsperando > 0) {
                    vezMulheres.signalAll();
                }
            }
        }
        finally {
            this.lock.unlock();
        }
    }
}

class Pessoa extends Thread {
    private String nome;
    private boolean isHomem;
    private Banheiro banheiro;

    public Pessoa(String nome, boolean isHomem, Banheiro banheiro) {
        this.nome = nome;
        this.isHomem = isHomem;
        this.banheiro = banheiro;
    }

    public String checarNome() {
        return this.nome;
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long) (Math.random() * 10000));
            if (this.isHomem) {
                this.banheiro.entrarHomem(this);
            } else {
                this.banheiro.entrarMulher(this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class SimuladorBanheiro {
    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                Pessoa pessoa = new Pessoa("Mulher " + (i/2 + 1), false, banheiro);
                pessoa.start();
            }
            else {
                Pessoa pessoa = new Pessoa("Homem " + (i/2 + 1), true, banheiro);
                pessoa.start();
            }
        }
    }
}