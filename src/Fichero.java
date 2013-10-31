
import java.io.Serializable;


public class Fichero implements Serializable {

    // Usuario con el archivo original
    private String owner ;
    // Permisos que posee el archivo
    private int permisos;
    // Usuarios que poseen el archivo
    private String tenantList ;

    /*
     Metodo Constructor
     */
    public Fichero() {
        permisos = 0;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPermisos() {
        return permisos;
    }

    public void setPermisos(int permisos) {
        this.permisos = permisos;
    }

    public String getTenantList() {
        return tenantList;
    }

    public void setTenantList(String tenantList) {
        this.tenantList = tenantList;
    }
  
}