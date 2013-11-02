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
        //catalogo.put("archivo1", "nodo1");

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

        addBehaviour(new WhoHasFileServer());
        addBehaviour(new Propagate());
        addBehaviour(new Actualizar());
        addBehaviour(new Nacimiento());
        addBehaviour(new WasBorn());
        addBehaviour(new DescargaExitosa());
        addBehaviour(new DescargaNoExitosa());
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
        System.out.println("Supernodo-agent "+getAID().getName()+" terminating.");
    }


    /*
       Metodo Nacimiento
       Este metodo se encarga de buscar los agentes que otorguen el servicio
       de superNodo, toma el primero de estos y le envia un mensaje indicandole
       que acaba de nacer para que este le envie la tabla de hash de los recursos
       */
    private class Nacimiento extends Behaviour{
        public void action(){
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("supernodo");
            template.addServices(sd);
            try {
                System.out.println("Acabo de Nacer!\n");
                DFAgentDescription[] result = DFService.search(myAgent, template); 
                //Buscamos todos los superNodos
                AID[] superNodos = new AID[result.length];

                for (int i = 0; i < result.length; ++i) {
                    if (!result[i].getName().getName().contains(getAID().getName())){
                        superNodos[0] = result[i].getName();
                        break;
                    }
                }
                ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
                cfp.setConversationId("nacimiento");
                // Le enviamos al primer super nodo el mensaje indicandole que acabo de nacer
                cfp.setContent("NuevoNodo");
                cfp.addReceiver(superNodos[0]);
                cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);

                cfp = new ACLMessage(ACLMessage.INFORM);
                cfp.setConversationId("agregarSuperNodo");
                cfp.setContent("NuevoNodo");
                for (int i = 0; i < superNodos.length; i++){
                    cfp.addReceiver(superNodos[i]);
                }
                cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);

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

                // Buscamos en el catalogo quien posee el archivo deseado por el cliente
                String fileHolder = (String) catalogo.get(fileName);


                if (fileHolder != null) {
                    // En caso de que alguien posea el archivo, le enviamos
                    // como respuesta el nombre de los nodos que lo tienen e informacion extra 
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(fileHolder);
                }
                else {
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
       Metodo wasBorn
       Este metodo se encarga de recibir los mensajes que provienen de los nodos una vez que
       este es creado con el fin de enviarle el catalogo de los recursos
       */
    private class WasBorn extends CyclicBehaviour {
        public void action() {
            // Recibimos el mensaje que puede venir de otro SuperNodo 
            MessageTemplate mt =  MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("nacimiento"));
            ACLMessage msg = myAgent.receive(mt);
            ACLMessage reply;
            if (msg != null) {

                try {
                    if(msg.getContent().equalsIgnoreCase("NuevoNodo")){
                        System.out.println("Existe un nuevo nodo!\n");
                        // Caso en el que un SuperNodo requiere la Tabla de Hash
                        reply = msg.createReply();
                        reply.setPerformative(ACLMessage.PROPAGATE);
                        // Le enviamos en el mensaje el catalogo
                        reply.setContentObject(catalogo);
                        myAgent.send(reply);
                    }

                }catch (Exception io){
                    io.printStackTrace();
                }
            }else {
                block();
            }

            block();
            mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("listaSuperNodos"));
            msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPAGATE);
                    reply.setContentObject(superNodos);
                    reply.setConversationId("listaSuperNodos");
                    myAgent.send(reply);
                } catch (Exception io) {
                    io.printStackTrace();
                }
            }
        }
    }

    private class addSuperNode extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt =  MessageTemplate.and(  
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("agregarSuperNodo"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    AID nuevo = msg.getSender();
                    superNodos.add(nuevo);
                } catch (Exception io) {
                    io.printStackTrace();
                }
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
                    if(msg.getContent().equalsIgnoreCase("NuevoArchivo")){
                        /** Espacio para  actualizar */
                        //Nuevo Archivo se recibe desde el cliente y se guarda

                        arch = (Fichero)msg.getContentObject();
                        catalogo.put(arch.getNombre(),arch);
                    }else if(msg.getContent().equalsIgnoreCase("NuevoPermiso")){
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
                        cfp. setContentObject(catalogo);
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
        int step = 0; 
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
            ACLMessage msg = myAgent.receive(mt);

            switch(step){
                case 0: 
                    if (msg != null) {
                        // Mensaje recibido.
                        //Archivo pedido
                        try {
                            // En el mensaje se encuentra la tabla de Hash
                            Hashtable contenido = (Hashtable)msg.getContentObject();
                            // Esta sera nuestra nueva tabla de hash
                            catalogo = contenido;
                            System.out.println("Tabla de Hash Actualizada\n");
                            step = 1;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        block();
                    }

                    break;
                case 1:
                    msg = new ACLMessage();
                    msg.setPerformative(ACLMessage.INFORM);
                    msg.setConversationId("listaSuperNodos");
                    msg.setContent("listaSuperNodos");
                    myAgent.send(msg);
                    block();

                    mt = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchConversationId("listaSuperNodos"));
                    msg = myAgent.receive(mt);
                        try {
                            superNodos = (ArrayList<AID>) msg.getContentObject();
                            System.out.println("Lista de super nodos Actualizada\n");
                            step = 2;
                        } catch (Exception e){
                            e.printStackTrace();
                        }
            }

        }
    }

    /*
       Metodo DescargaExitosa
       Este behaviour se ejecuta cuando un super nodo es notificado de que 
       una descarga se ha realizado satisfactoriamente y debe aumentar la 
       confiabilidad del nodo que se le pasa en el contenido del mensaje
       */
    private class DescargaExitosa extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {


                // Obtenemos del contenido del mensaje cual es el nodo
                // con el cual se ejecuto la descarga correctamente
                String cliente = msg.getContent();

                //Obtenemos la confiablidad que poseía el cliente
                String confiablidad = (String) nodos.get(cliente);
                int conf = Integer.parseInt(confiablidad);

                if (confiablidad != null){
                    //Aumentamos su confiablidad
                    nodos.put(cliente,conf++);
                }
                System.out.println("Descarga Exitosa. Buen trabajo "+cliente);

                try {
                    //Reenviamos el mismo mensaje al resto de los super nodos
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("supernodo");
                    template.addServices(sd);
                    // Buscamos todos los agentes que posean el servicio SuperNodo
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                    AID[] superNodos = new AID[result.length];

                    //Por cada superNodo le enviamos la nueva Tabla de Hash
                    for (int i = 0; i < result.length; ++i) {
                        if (!result[i].getName().getName().contains(getAID().getName())){
                            superNodos[i] = result[i].getName();
                            ACLMessage cfp = new ACLMessage(ACLMessage.INFORM_REF);
                            cfp.addReceiver(superNodos[i]);
                            cfp. setContentObject(cliente);
                            myAgent.send(cfp);
                        }
                    }
                }catch (FIPAException fe) {
                    fe.printStackTrace();
                }catch (Exception io){
                    io.printStackTrace();
                }

            } else {
                block();
            }
        }
    }


    /*
       Metodo DescargaNoExitosa
       Este behaviour se ejecuta cuando un super nodo es notificado de que 
       una descarga NO se ha realizado satisfactoriamente y debe disminuir la 
       confiabilidad del nodo que se le pasa en el contenido del mensaje
       */
    private class DescargaNoExitosa extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {


                // Obtenemos del contenido del mensaje cual es el nodo
                // con el cual se ejecuto la descarga correctamente
                String cliente = msg.getContent();

                //Obtenemos la confiablidad que poseía el cliente
                String confiablidad = (String) nodos.get(cliente);
                int conf = Integer.parseInt(confiablidad);

                if (confiablidad != null){
                    //Disminuimos su confiablidad
                    nodos.put(cliente,conf--);
                }
                System.out.println("Descarga Fallida. "+cliente);

                try {
                    //Reenviamos el mismo mensaje al resto de los super nodos
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("supernodo");
                    template.addServices(sd);
                    // Buscamos todos los agentes que posean el servicio SuperNodo
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                    AID[] superNodos = new AID[result.length];

                    //Por cada superNodo le enviamos la nueva Tabla de Hash
                    for (int i = 0; i < result.length; ++i) {
                        if (!result[i].getName().getName().contains(getAID().getName())){
                            superNodos[i] = result[i].getName();
                            ACLMessage cfp = new ACLMessage(ACLMessage.INFORM_IF);
                            cfp.addReceiver(superNodos[i]);
                            cfp. setContentObject(cliente);
                            myAgent.send(cfp);
                        }
                    }
                }catch (FIPAException fe) {
                    fe.printStackTrace();
                }catch (Exception io){
                    io.printStackTrace();
                }

            } else {
                block();
            }
        }
    }

}
