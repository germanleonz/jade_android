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
  
}
