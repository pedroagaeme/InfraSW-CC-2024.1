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

public class exercicio1 {
    public static void main(String[] args) {
        Conta conta = new Conta(1000);
        (new Thread(() -> {conta.Deposito(500.00);})).start();
        (new Thread(() -> {conta.Deposito(500.00);})).start();
        (new Thread(() -> {conta.Saque(3500.00);})).start();
        (new Thread(() -> {conta.Saque(750.50);})).start();
        (new Thread(() -> {conta.Deposito(690.35);})).start();
        (new Thread(() -> {conta.Saque(96.55);})).start();
        (new Thread(() -> {conta.Saque(70.40);})).start();
        (new Thread(() -> {conta.Saque(700.00);})).start();
        (new Thread(() -> {conta.Deposito(12.00);})).start();
        (new Thread(() -> {conta.Saque(1500.00);})).start();
        (new Thread(() -> {conta.Saque(1000.00);})).start();
        (new Thread(() -> {conta.Deposito(460.20);})).start();
        (new Thread(() -> {conta.Deposito(150.50);})).start();
        (new Thread(() -> {conta.Deposito(450.50);})).start();
        (new Thread(() -> {conta.Saque(950.00);})).start();
        (new Thread(() -> {conta.Deposito(500.00);})).start();
        (new Thread(() -> {conta.Saque(750.00);})).start();
        (new Thread(() -> {conta.Saque(750.00);})).start();
        (new Thread(() -> {conta.Saque(750.00);})).start();
    }
}
