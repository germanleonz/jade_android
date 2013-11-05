import java.io.Serializable;
import jade.core.AID;

public class Cliente implements Serializable {

    private AID client ;

    private int confiablidad;

    private int capacidad; // capacidad disponible


    /*
       Metodo Constructor
       */
    public Cliente() {
        capacidad = 0;
        confiablidad= 0;
    }

    public Cliente(AID cliente,int cap){
        client = cliente;
        capacidad = cap;
        confiablidad = 0;
    }

    public int getConfiabilidad() {
        return confiablidad;
    }

    public void setConfiabilidad(int conf) {
        this.confiablidad = conf;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int cap) {
        this.capacidad = cap;
    }

    public AID getClientAID() {
        return client;
    }

    public void setClientAID(AID nuevoAID) {
        this.client = nuevoAID;
    }
}
