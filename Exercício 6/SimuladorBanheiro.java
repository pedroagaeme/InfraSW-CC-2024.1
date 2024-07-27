import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
        while(this.pessoasPrevistas > 0) {
        this.lock.lock();
        try {
            if (!this.fila.isEmpty()) {
                if(this.fila.get(0).checarGenero()) {
                    while (this.mulheresNoBanheiro > 0) {
                        this.vezHomens.await();
                    }
                    if(this.homensNoBanheiro < this.CAPACIDADE) {
                        this.homensNoBanheiro++;
                        System.out.println(this.fila.get(0).checarNome() + " entrou no banheiro. Homens: " + this.homensNoBanheiro);
                        this.pessoasPrevistas--;
                        this.fila.get(0).garantirLugar();
                        this.esperarVez.signalAll();
                        this.fila.remove(0);
                    }
                }
                else {
                    while(this.homensNoBanheiro > 0) {
                        this.vezMulheres.await();
                    }
                    if(this.mulheresNoBanheiro < this.CAPACIDADE) {
                        this.mulheresNoBanheiro++;
                        System.out.println(this.fila.get(0).checarNome() + " entrou no banheiro. Mulheres: " + this.mulheresNoBanheiro);
                        this.pessoasPrevistas--;
                        this.fila.get(0).garantirLugar();
                        this.esperarVez.signalAll();
                        this.fila.remove(0);
                    }
                }
            }
            else {
                while(this.fila.isEmpty()) {
                    this.alguemChegou.await();
                }
            }
        } finally {
            this.lock.unlock();
        }
        }
    }
    public void entrar(Pessoa pessoa) throws InterruptedException {
        this.lock.lock();
        try {
            System.out.println(pessoa.checarNome() + " quer usar o banheiro.");
            this.fila.add(pessoa);
            if(this.fila.size() == 1) {
                this.alguemChegou.signalAll();
            }
            while(!pessoa.checarLugar()) {
                this.esperarVez.await();
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void sair(Pessoa pessoa) throws InterruptedException {
        Thread.sleep((long) (Math.random() * 1000)); 
        this.lock.lock();
        try {
            if(pessoa.checarGenero()) {
                this.homensNoBanheiro--;
                System.out.println(pessoa.checarNome() + " saiu do banheiro. Homens restantes: " + this.homensNoBanheiro);
                if(this.homensNoBanheiro == 0) {
                    this.vezMulheres.signalAll();
                }
            }
            else {
                this.mulheresNoBanheiro--;
                System.out.println(pessoa.checarNome() + " saiu do banheiro. Mulheres restantes: " + this.mulheresNoBanheiro);
                if(this.mulheresNoBanheiro == 0) {
                    vezHomens.signalAll();
                }
            }
        } finally {
            this.lock.unlock();
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