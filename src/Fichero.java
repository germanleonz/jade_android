import java.util.LinkedList;
import java.io.Serializable;
import jade.core.AID;

public class Fichero implements Serializable {

    // Usuario con el archivo original
    private String owner ;
    // Permisos que posee el archivo
    private boolean permisos;
    // Usuarios que poseen el archivo
    private LinkedList<AID> holders ;
    // Nombre del archivo
    private String nombre;


    /*
     Metodo Constructor
     */
    public Fichero() {
        permisos = true;
    }
    
    public Fichero(AID creador,String fileName){
    	permisos = true;
    	owner    = creador.getName();
    	nombre   = fileName;
    	holders  = new LinkedList<AID>();
    	holders.add(creador);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean getPermisos() {
        return permisos;
    }

    public void setPermisos(boolean permisos) {
        this.permisos = permisos;
    }

    public LinkedList<AID> getHolders() {
        return holders;
    }

    public void setHolders(LinkedList<AID> tenantList) {
        this.holders = tenantList;
    }

    public void setAHolder(AID holder){
        this.holders.addLast(holder);
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }
  
}
