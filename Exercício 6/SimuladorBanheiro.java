import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Banheiro {
    private final int CAPACIDADE = 3;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition vezHomens = lock.newCondition();
    private final Condition vezMulheres = lock.newCondition();
    private final Condition alguemChegou = lock.newCondition();
    private final Condition esperarVez = lock.newCondition();
    private int homensNoBanheiro = 0;
    private int mulheresNoBanheiro = 0;
    private int pessoasPrevistas = 100;
    private ArrayList<Pessoa>  fila = new ArrayList<>();


    public void controlarEntrada() throws InterruptedException {
        while(pessoasPrevistas > 0) {
        this.lock.lock();
        try {
            if (!fila.isEmpty()) {
                if(fila.get(0).checarGenero()) {
                    while (mulheresNoBanheiro > 0) {
                        vezHomens.await();
                    }
                    if(homensNoBanheiro < CAPACIDADE) {
                        homensNoBanheiro++;
                        System.out.println(fila.get(0).checarNome() + " entrou no banheiro. Homens: " + homensNoBanheiro);
                        pessoasPrevistas--;
                        fila.get(0).garantirLugar();
                        esperarVez.signalAll();
                        fila.remove(0);
                    }
                }
                else {
                    while(homensNoBanheiro > 0) {
                        vezMulheres.await();
                    }
                    if(mulheresNoBanheiro < CAPACIDADE) {
                        mulheresNoBanheiro++;
                        System.out.println(fila.get(0).checarNome() + " entrou no banheiro. Mulheres: " + mulheresNoBanheiro);
                        pessoasPrevistas--;
                        fila.get(0).garantirLugar();
                        esperarVez.signalAll();
                        fila.remove(0);
                    }
                }
            }
            else {
                while(fila.isEmpty()) {
                    alguemChegou.await();
                }
            }
        } finally {
            lock.unlock();
        }
        }
    }
    public void entrar(Pessoa pessoa) throws InterruptedException {
        this.lock.lock();
        try {
            System.out.println(pessoa.checarNome() + " quer usar o banheiro.");
            fila.add(pessoa);
            if(fila.size() == 1) {
                alguemChegou.signalAll();
            }
            while(!pessoa.checarLugar()) {
                esperarVez.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void sair(Pessoa pessoa) throws InterruptedException {
        Thread.sleep((long) (Math.random() * 1000)); 
        this.lock.lock();
        try {
            if(pessoa.checarGenero()) {
                homensNoBanheiro--;
                System.out.println(pessoa.checarNome() + " saiu do banheiro. Homens restantes: " + homensNoBanheiro);
                if(homensNoBanheiro == 0) {
                    vezMulheres.signalAll();
                }
            }
            else {
                mulheresNoBanheiro--;
                System.out.println(pessoa.checarNome() + " saiu do banheiro. Mulheres restantes: " + mulheresNoBanheiro);
                if(mulheresNoBanheiro == 0) {
                    vezHomens.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}

class Controle extends Thread {
    private Banheiro banheiro;

    public Controle(Banheiro banheiro) {
        this.banheiro = banheiro;
    }

    @Override
    public void run() {
        try {
            this.banheiro.controlarEntrada();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Pessoa extends Thread {
    private String nome;
    private boolean isHomem;
    private Banheiro banheiro;
    private boolean lugar;

    public Pessoa(String nome, boolean isHomem, Banheiro banheiro) {
        this.nome = nome;
        this.isHomem = isHomem;
        this.banheiro = banheiro;
        this.lugar = false;
    }

    public String checarNome() {
        return this.nome;
    }

    public boolean checarGenero() {
        return this.isHomem;
    }
     
    public boolean checarLugar() {
        return this.lugar;
    }

    public void garantirLugar() {
        this.lugar = true;
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long) (Math.random() * 10000));
            this.banheiro.entrar(this);
            this.banheiro.sair(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class SimuladorBanheiro {
    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();
        Controle controle = new Controle(banheiro);
        controle.start();
        for (int i = 0; i < 100; i++) {
            int j = (int) (Math.random() * 1000);
            if (j % 2 == 0) {
                Pessoa pessoa = new Pessoa("Pessoa " + (i) + " (mulher)", false, banheiro);
                pessoa.start();
            }
            else {
                Pessoa pessoa = new Pessoa("Pessoa " + (i) + " (homem)" , true, banheiro);
                pessoa.start();
            }
        }
    }
}