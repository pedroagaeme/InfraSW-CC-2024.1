import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Conta {
    private double saldo;
    private final Lock lock = new ReentrantLock();

    public Conta(double saldo_inicial) {
        saldo = saldo_inicial;
    }

    public void Deposito(double valor) {
        lock.lock();
        try {
            saldo += valor;
            System.out.printf("Valor depositado: %.2f\n", valor);
            Checar_saldo();
        } 
        finally {
            lock.unlock();
        }
    }

    public void Saque(double valor) {
        lock.lock();
        try {
            if (saldo >= valor) {
                saldo -= valor;
                System.out.printf("Valor retirado: %.2f reais\n", valor);
            }
            else {
                System.out.printf("Nao foi possivel retirar o valor: %.2f\n", valor);
            }
            Checar_saldo();
        }
        finally {
            lock.unlock();
        }
    }

    public void Checar_saldo() {
        if(saldo >= 0) {
            System.out.printf("Saldo atual: %.2f\n", saldo);
        }
        else {
            System.out.println("Ocorreu o seguinte erro: saldo negativo");
        }
    } 
}

public class Banco {
    public static void main(String[] args) {
        Conta conta = new Conta(1000);
        int operacao;
        for (int i = 0; i < 10; i++) {
            operacao = (int)(Math.random() * 1000);
            if (operacao % 2 == 0) {
            (new Thread(() -> {conta.Deposito(Math.random() * 1000);})).start();
            }
            else {
            (new Thread(() -> {conta.Saque(Math.random() * 1000);})).start();
            }
        }
    }
}
