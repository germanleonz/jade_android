import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SuperNodoAgent extends Agent {

    // Se almacenan los recursos y quienes lo poseen
    private Hashtable<String, Fichero> catalogo;

    // Se almacena los nodos y su confiabilidad
    private Hashtable<AID, Cliente> nodos;

    private ArrayList<AID> superNodos;

    /*
       Metodo setup
       Este metodo define la inicializacion de un agente que prestara
       el servicio de superNodo
       */
    protected void setup() {
        catalogo = new Hashtable<String, Fichero>();
        nodos    = new Hashtable<AID, Cliente>();

        //  Se registra este nodo como SuperNodo
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("supernodo");
        sd.setName("JADE-file-sharing");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //  Se registran los comportamientos de inicializacion
        addBehaviour(new Nacimiento());
        addBehaviour(new EntregarCatalogo());
        addBehaviour(new Actualizar());
        addBehaviour(new ActualizarNodos());

        //  Se registra el resto de los comportamientos del SuperNodo
        addBehaviour(new WhoHasFileServer());
        addBehaviour(new Propagate());
        addBehaviour(new Registro());
        addBehaviour(new ActualizarConfiabilidad());
    }

    /*
       Metodo takeDown
       Este metodo tiene como funcion finalizar la ejecucion del agente
       */
    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Supernodo-agent " + getAID().getName() + " terminando.");
    }


    /*
       Metodo Registro
       Este behaviour se encarga de registrar a un cliente localmente y de enviar la nueva
       tabla de nodos a los demas supernodos
       */
    private class Registro extends CyclicBehaviour {
        MessageTemplate mt;
        ACLMessage msg;
        Cliente client;

        public void action() {

            // Recibimos el mensaje
            mt =  MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("registro"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                AID sender = msg.getSender();
                client     = new Cliente(sender,Integer.valueOf(msg.getContent()));
                System.out.println("Registro nuevo cliente");
                nodos.put(sender,client);// agrego el nodo a la tabla de hash

                //  Le enviamos la nueva Tabla de Hash de nodos a cada SuperNodo
                for (int i = 0; i < superNodos.size(); i++) {

                    ACLMessage cfp = new ACLMessage(ACLMessage.PROPAGATE);
                    cfp.setConversationId("actualizarCatalogo");
                    cfp.addReceiver(superNodos.get(i));
                    try{
                        cfp.setContentObject(nodos); 
                    }catch (Exception io) {
                        io.printStackTrace();
                    }

                    cfp.setConversationId("actualizarNodos");
                    myAgent.send(cfp);
                }
            } else {
                block();
            }
        }
    }   

    /*
       Metodo Nacimiento
       Este metodo se encarga de buscar los agentes que otorguen el servicio
       de superNodo, toma el primero de estos y le envia un mensaje indicandole
       que acaba de nacer para que este le envie la tabla de hash de los recursos
       */
    private class Nacimiento extends Behaviour {
        private ACLMessage cfp;

        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("supernodo");
            template.addServices(sd);
            try {
                //  Le preguntamos al DF por todos los superNodos y se 
                //  agregan a la lista local
                System.out.println("Agregando supernodos a mi lista local");
                DFAgentDescription[] result = DFService.search(myAgent, template); 
                superNodos = new ArrayList<AID>();

                for (int i = 0; i < result.length; ++i) {
                    superNodos.add(result[i].getName());
                }

                // Le solicitamos al primer supernodo el catalogo
                System.out.println("Solicitando catalogo al primer supernodo.");
                if (superNodos.size() > 1) {
                    cfp = new ACLMessage(ACLMessage.INFORM);
                    cfp.setConversationId("solicitud catalogo");

                    for (AID superNodo: superNodos) {
                        if (!superNodo.getName().contains(getAID().getName())) {
                            cfp.addReceiver(superNodo);
                            break;
                        }
                    }
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                } else {
                    System.out.println("Este es el unico supernodo del sistema. El catalogo es vacio.");
                }

                //  Notificamos a todos los supernodos distintos a mi de la
                //  creacion de este superNodo  
                System.out.println("Notificando a todos los supernodos que llegue.");
                if (superNodos.size() > 1) {
                    cfp = new ACLMessage(ACLMessage.INFORM);
                    cfp.setConversationId("notificando nacimiento");
                    for (AID superNodo: superNodos) {
                        if (!superNodo.getName().contains(getAID().getName())) {
                            cfp.addReceiver(superNodo);
                        }
                    }
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                } else {
                    System.out.println("Este es el unico supernodo del sistema. No envio notificaciones.");
                }
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }catch (Exception io){
                io.printStackTrace();
            }
        }
        public boolean done() {
            return true;
        }
    }

    /*
       Metodo WhoHasFileServer
       Este metodo se encarga de responder al cliente cual nodo tiene el archivo
       que desea. El cual le solicitó por medio de un mensaje anterior
       */
    private class WhoHasFileServer extends CyclicBehaviour {
        public void action() {
            // Recibimos el mensaje
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchConversationId("seek-holder"));
            ACLMessage msg = myAgent.receive(mt);

            // Si no hemos recibido ningun mensaje se bloquea para no ocupar CPU
            if (msg != null) {

                String fileName = msg.getContent();
                ACLMessage reply = msg.createReply();

                System.out.println("Recibí peticion por el archivo : "+fileName);

                // Buscamos en el catalogo quien posee el archivo deseado por el cliente
                Fichero fileData = catalogo.get(fileName);

                reply.setPerformative(ACLMessage.INFORM);
                reply.setConversationId("holders");

                if (fileData != null) {
                    // En caso de que varios nodos posean el archivo, le enviamos
                    // como respuesta el nombre del nodo mas confiable que lo tenga
                    Cliente mejorHolder = new Cliente();
                    if(fileData.getPermisos()){
                        System.out.println("tiene permisos");
                        reply.addUserDefinedParameter("permisos", "true");
                        for (AID holder: fileData.getHolders()) {
                            Cliente datosNodo = nodos.get(holder);
                            if (datosNodo.getConfiabilidad() >= mejorHolder.getConfiabilidad()) {
                                mejorHolder = datosNodo;
                            }
                        }   
                        try{
                            reply.addUserDefinedParameter("nombreArchivo",fileName);
                            reply.setContentObject(mejorHolder.getClientAID());
                        } catch (Exception io) {
                            io.printStackTrace();
                        }
                    }else{
                         reply.addUserDefinedParameter("permisos", "false");
                    }

                }
                else {
                    System.out.println("El archivo :"+fileName+" no existe");
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    /*
       Metodo EntregarCatalogo
       Este metodo se encarga de recibir los mensajes que provienen de los nodos una vez que
       este es creado con el fin de enviarle el catalogo de los recursos
       */
    private class EntregarCatalogo extends CyclicBehaviour {
        MessageTemplate mt;
        ACLMessage msg;
        ACLMessage reply;

        public void action() {
            // Recibimos el mensaje que puede venir de otro SuperNodo 
            mt =  MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("solicitud catalogo"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    // Caso en el que un SuperNodo requiere la Tabla de Hash
                    System.out.println("Respondiendo a solicitud del catalogo");
                    reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPAGATE);
                    reply.setConversationId("actualizarCatalogo");
                    reply.setContentObject(catalogo);
                    myAgent.send(reply);
                    superNodos.add(msg.getSender());
                } catch (Exception io) {
                    io.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    private class addSuperNode extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt =  MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("notificando nacimiento"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    AID nuevo = msg.getSender();
                    superNodos.add(nuevo);
                } catch (Exception io) {
                    io.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    /* 	
        Metodo Propagate
        Este metodo se encarga de actualizar la Tabla de Hash ya que el cliente tiene
        un nuevo recurso que desea compartir
        */
    private class Propagate extends CyclicBehaviour {
        public void action() {
            // Recibimos el mensaje que puede venir de un Nodo
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg     = myAgent.receive(mt);

            if (msg != null) {
                Fichero arch;
                Fichero arch2;
                try {
                    if(msg.getConversationId().equalsIgnoreCase("NuevoArchivo")){
                       arch = (Fichero)msg.getContentObject();

                        /** Espacio para  actualizar */
                        //Nuevo Archivo se recibe desde el cliente y se guarda

                        /*

                                Replicar:
                                1. Buscar 2k-1 replicas
                                2. Enviar lista de replicas al nodo

                        */
                       System.out.println("tam " + arch.getTam());
                       AID[] replicas = findReplicas(arch.getTam());
                       ACLMessage reply = msg.createReply();

                       reply.setPerformative(ACLMessage.INFORM);
                       reply.setConversationId("lista replicas");
                       try{
                           reply.addUserDefinedParameter("nombreArchivo", arch.getNombre());
                           reply.setContentObject(replicas);
                       } catch (Exception io) {
                           io.printStackTrace();
                       }
                       myAgent.send(reply);
                       catalogo.put(arch.getNombre(),arch);

                    }else if(msg.getConversationId().equalsIgnoreCase("NuevoPermiso")){
                        //Cambio de Permisos de un archivo se recibe el archivo desde el cliente
                        arch  = (Fichero) msg.getContentObject();
                        arch2 = catalogo.get(arch.getNombre());
                        //Actualizar la lista de holders
                        arch.setHolders(arch2.getHolders());
                        catalogo.put(arch.getNombre(),arch);
                    }else if(msg.getConversationId().equalsIgnoreCase("NuevaCapacidad")){
                        int tamArch =  Integer.parseInt(msg.getUserDefinedParameter("tamArch"));
                        AID nodo = (AID) msg.getContentObject();
                        int capacidad = nodos.get(nodo).getCapacidad();
                        nodos.get(nodo).setCapacidad(capacidad - tamArch);
                        System.out.println("Actualizando capacidad del nodo: " + nodo); 
                    }else{
                        //Nuevo nodo con el archivo
                        arch  = (Fichero) msg.getContentObject();
                        arch2 = catalogo.get(arch.getNombre());
                        arch2.setOwner(arch.getOwner());
                        catalogo.put(arch.getNombre(),arch2);
                    }

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("supernodo");
                    template.addServices(sd);
                    // Buscamos todos los agentes que posean el servicio SuperNodo
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                    AID[] superNodos = new AID[result.length];

                    //Por cada superNodo le enviamos la nueva Tabla de Hash
                    for (int i = 0; i < result.length; ++i) {
                        superNodos[i] = result[i].getName();
                        ACLMessage cfp = new ACLMessage(ACLMessage.PROPAGATE);
                        cfp.addReceiver(superNodos[i]);
                        cfp.setContentObject(catalogo);
                        cfp.setConversationId("actualizarCatalogo");
                        cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                        myAgent.send(cfp);
                        System.out.println(superNodos[i].getName());
                    }

                }catch (FIPAException fe) {
                    fe.printStackTrace();
                }catch (Exception io){
                    io.printStackTrace();
                }
            }else {
                block();
            }
        }
    }

    /* Metodo Actualizar
       Este metodo se encarga de actualizar la Tabla de Hash debido a una notificacion de 
       actualizacion, desechamos la tabla anterior y asignamos como nueva la recibida en
       el mensaje
       */
    private class Actualizar extends CyclicBehaviour {
        MessageTemplate mt;
        ACLMessage msg;

        public void action() {
            mt  = MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                    MessageTemplate.MatchConversationId("actualizarCatalogo"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                try {
                    // Se reemplaza la tabla de la informacion de los archivos con la que llego
                    Hashtable<String, Fichero> contenido = (Hashtable<String, Fichero>) msg.getContentObject();
                    catalogo = contenido;
                    System.out.println("Tabla de Hash Actualizada.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    /* Metodo ActualizarNodos
       Este behaviour se encarga de actualizar la Tabla de Hash de nodos debido un
       registro de un nuevo nodo
       */
    private class ActualizarNodos extends CyclicBehaviour {
        MessageTemplate mt;
        ACLMessage msg;

        public void action() {
            mt  = MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                    MessageTemplate.MatchConversationId("actualizarNodos"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                try {
                    // En el mensaje se encuentra la tabla de Hash de nodos
                    // Esta sera nuestra nueva tabla de hash de nodos
                    Hashtable<AID, Cliente> nuevosNodos = (Hashtable<AID, Cliente>)msg.getContentObject();
                    nodos = nuevosNodos;
                    System.out.println("Tabla de Hash de NODOS Actualizada.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    public String HashForFile(String filePath) {
        StringBuffer hexString = new StringBuffer();

        try {
            MessageDigest md    = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(filePath);

            byte[] dataBytes = new byte[1024];

            int nread = 0; 
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();

            for (int i = 0; i < mdbytes.length; i++) {
                hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }
        } catch(NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return hexString.toString();
    }

    /* 
       Metodos usados para replicar
    */
    
    /*
       Este metodo se encarga de buscar las 2k-1 replicas con mas capacidad
    */
    private AID[] findReplicas(long tam){

        int count = 0;
        int k = 2;
        AID[] reps = new AID[(2*k)-1];
        for (AID r: reps)
            r = null;
        
        for (AID key: nodos.keySet()) {
            Cliente cliaux = nodos.get(key);

            if (cliaux.getCapacidad() > tam){
                reps[count] = cliaux.getClientAID();
                count++;
            }
            if (count == (2*k)-1 )
                break;
        }

        return reps;
    }


    /*
        Behaviour que recibe las notificaciones de los clientes cuando una descarga
        se realiza satisfactoriamente :)
    */
    private class ActualizarConfiabilidad extends CyclicBehaviour {
        MessageTemplate mt;
        ACLMessage msg;

        public void action() {
            mt  = MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("descargaOK"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                try {
                    // En el mensaje se encuentra el cliente que envio el
                    // archivo y todo funciono bien
                    AID sender = (AID)msg.getContentObject();
                    Cliente client = nodos.get(sender);
                    int confiabilidad = client.getConfiabilidad();
                    client.setConfiabilidad(confiabilidad++);
                    // Colocamos el cliente con la confiabilidad aumentada
                    nodos.put(sender,client);

                    // Actualizacion de tabla de hash de super Nodos
                    //  Le enviamos la nueva Tabla de Hash de nodos a cada SuperNodo
                    for (int i = 0; i < superNodos.size(); i++) {

                        ACLMessage cfp = new ACLMessage(ACLMessage.PROPAGATE);
                        cfp.addReceiver(superNodos.get(i));
                        try{
                            cfp.setContentObject(nodos); 
                        }catch (Exception io) {
                            io.printStackTrace();
                        }

                        cfp.setConversationId("actualizarNodos");
                        myAgent.send(cfp);
                    }

                   
                    System.out.println("Actualizada confiabilidad (Positiva)");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }



}

