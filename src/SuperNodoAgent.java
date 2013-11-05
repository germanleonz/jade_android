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

public class SuperNodoAgent extends Agent {

    // Se almacenan los recursos y quienes lo poseen
    private Hashtable catalogo;

    // Se almacena los nodos y su confiabilidad
    private Hashtable nodos;

    private ArrayList<AID> superNodos;

    /*
       Metodo setup
       Este metodo define la inicializacion de un agente que prestara
       el servicio de superNodo
       */
    protected void setup() {
        catalogo = new Hashtable();
        nodos = new Hashtable();

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
       Este metodo se encarga de registrar un cliente
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
               client= new Cliente(sender,Integer.valueOf(msg.getContent()));
               System.out.println("Registro nuevo cliente");
               nodos.put(sender,client);// agrego el nodo a la tabla de hash


                //Por cada superNodo le enviamos la nueva Tabla de Hash de nodos
                for (int i = 0; i < superNodos.size(); i++) {
                   
                    ACLMessage cfp = new ACLMessage(ACLMessage.PROPAGATE);
                    cfp.addReceiver(superNodos.get(i));
                    try{
                       cfp.setContentObject(nodos); 
                    }catch (Exception io) {
                        io.printStackTrace();
                    }
                    
                    cfp.setConversationId("registro");
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
                    System.out.println("Este es el unico supernodo del sistema.");
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
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            // Si no hemos recibido ningun mensaje se bloquea para no ocupar CPU
            if (msg != null) {

                String fileName = msg.getContent();
                ACLMessage reply = msg.createReply();

                System.out.println("Recibí peticion por el archivo : "+fileName);

                // Buscamos en el catalogo quien posee el archivo deseado por el cliente
                Fichero fileHolder = (Fichero) catalogo.get(fileName);


                if (fileHolder != null) {
                    // En caso de que alguien posea el archivo, le enviamos
                    // como respuesta el nombre de los nodos que lo tienen e informacion extra 
                    reply.setPerformative(ACLMessage.INFORM);
                    try{
                    	reply.setContentObject(fileHolder);
                    } catch (Exception io) {
                    	io.printStackTrace();
                	}
                    
                }
                else {
                    System.out.println("El archivo :"+fileName+" no existe");
                    reply.setPerformative(ACLMessage.REFUSE);
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
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                Fichero arch;
                Fichero arch2;
                try {
                    if(msg.getConversationId().equalsIgnoreCase("NuevoArchivo")){
                        /** Espacio para  actualizar */
                        //Nuevo Archivo se recibe desde el cliente y se guarda

                        arch = (Fichero)msg.getContentObject();
                        catalogo.put(arch.getNombre(),arch);
                    }else if(msg.getConversationId().equalsIgnoreCase("NuevoPermiso")){
                        //Cambio de Permisos de un archivo se recibe el archivo desde el cliente
                        arch = (Fichero) msg.getContentObject();
                        arch2 = (Fichero) catalogo.get(arch.getNombre());
                        //Actualizar la lista de holders
                        arch.setHolders(arch2.getHolders());
                        catalogo.put(arch.getNombre(),arch);
                    }else{
                        //Nuevo nodo con el archivo
                        arch = (Fichero) msg.getContentObject();
                        arch2 = (Fichero) catalogo.get(arch.getNombre());
                        arch2.setAHolder(arch.getOwner());
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
                        cfp.setConversationId("propagate");
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
            mt  = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
            msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                // Archivo pedido
                try {
                    // En el mensaje se encuentra la tabla de Hash
                    Hashtable contenido = (Hashtable)msg.getContentObject();
                    // Esta sera nuestra nueva tabla de hash
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
       Este metodo se encarga de actualizar la Tabla de Hash de nodos debido un
       registro de un nuevo nodo
       */
    private class ActualizarNodos extends CyclicBehaviour {
        MessageTemplate mt;
        ACLMessage msg;

        public void action() {
            mt  = MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE),
                    MessageTemplate.MatchConversationId("registro"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                // Archivo pedido
                try {
                    // En el mensaje se encuentra la tabla de Hash de nodos
                    Hashtable nuevosNodos = (Hashtable)msg.getContentObject();
                    // Esta sera nuestra nueva tabla de hash de nodos
                    nodos = nuevosNodos;
                    System.out.println("Tabla de Hash de NODOS  Actualizada.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

}
