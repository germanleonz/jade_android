
/**
 * ****************************************************************************
 * Proyecto P2P hecho con el framework JADE Autores: Javier Arguello German Leon
 * Krysler Pinto Gustavo Ortega
 * ***************************************************************************
 */
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class SuperNodo extends Agent {
    /* Catalogo donde estara la informacion de los archivos a compartir. Donde
     * las claves son los libros y los valores son los clientes que tiene el 
     * archivo.
     */

    private Hashtable catalogo;

    protected void setup() {
        // Cear catalogo
        catalogo = new Hashtable();

        // Register el super nodo
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("SuperNodo");
        sd.setName("ServicioSuperNodo");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Comportamiento para recibir tablas actualizadas
        addBehaviour(new RecibirCatalogo());

        // Comportamiento para recibir actualizaciones de los nodos clientes
        addBehaviour(new ActualizacionCatalogo());

        // Comportamiento para responder a las solicitudes por archivos
        addBehaviour(new RespuestaPorArchivo());

        //Bloqueo activo para actualizar tablas
        while (true) {
            try {
                Thread.sleep(4000);

                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd2 = new ServiceDescription();
                sd2.setType("SuperNodo");
                template.addServices(sd2);
                DFAgentDescription[] result = DFService.search(this, template);
                AID[] sellerAgents = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    sellerAgents[i] = result[i].getName();
                    //Enviar mensaje a los otros super nodos
                    if (!sellerAgents[i].getName().contains(getAID().getName())) {
                        ACLMessage cfp = new ACLMessage(ACLMessage.CONFIRM);
                        cfp.addReceiver(sellerAgents[i]);
                        cfp.setContent("hi");
                        this.send(cfp);
                        //No quiere enviar el mensaje no se por que
                    }
                    System.out.println(sellerAgents[i].getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Limpiar el agente

    protected void takeDown() {
        // Quitar el registro del agente 
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Agente " + getAID().getName() + " terminado.");
    }

    /**
     * This is invoked by the GUI when the user adds a new book for sale
     *
     * public void updateCatalogue(final String title, final int price) {
     * addBehaviour(new OneShotBehaviour() { public void action() {
     * catalogue.put(title, new Integer(price)); System.out.println(title + "
     * inserted into catalogue. Price = " + price); } }); }/
     *
     * /**
     * Clase privada ActualizacionCatalogo. Este comportamiento es usado por los
     * super nodos para registrar los cambios en la tabla de catalogo, los
     * cliente le envian al super nodo un ACLMessage de tipo CFP para actualizar
     * los datos, el super nodos le envia de respuesta un ACLMessage de tipo
     * PROPOSE.
     */
    private class ActualizacionCatalogo extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Mesaje CFP, procede a actualizar los datos.
                // Nombre del archivo nuevo
                String contenido = msg.getContent();
                // Identificador del cliente
                String cliente = msg.getSender().getName();
                //Creacion del mensaje de respuesta
                ACLMessage reply = msg.createReply();

                //Revizar si el cliente esta en la tabla de hash
                if (catalogo.contains(contenido)) {
                    //Obtener el contenido asociado a ese cliente
                    String valor = (String) catalogo.get(contenido);
                    valor = valor + "," + cliente;
                    catalogo.put(contenido, valor);
                } else {
                    catalogo.put(contenido, cliente);
                }

                //Mensaje de respuesta
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent("Archivo registrado");
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * Clase privada RespuestaPorArchivo. Este comportamiento es usado por el
     * super nodo para responderle al cliente cual o cuales clientes tienen el
     * archivo requerido, el super nodo responde al cliente con un ACLMessage de
     * tipo INFORM, mientras que el super nodo recive un ACLMessage de tipo
     * ACCEPT_PROPOSAL.
     */
    private class RespuestaPorArchivo extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                //Archivo pedido
                String archivo = msg.getContent();
                ACLMessage reply = msg.createReply();

                String cliente = (String) catalogo.get(archivo);
                if (cliente != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(cliente);
                } else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("archivo no encontrado");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * Clase privada RecibirCatalogo. Este comportamiento es usado por el super
     * nodo para obtener el catalogo actualizado, no se responde por el otro
     * super nodo mensaje recibido de tipo CONFIRM,
     */
    private class RecibirCatalogo extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Mensaje recibido.
                //Archivo pedido
                try {
                    Object contenido = msg.getContentObject();
                    System.out.println("El tipo de objeto es" + contenido.getClass());
                    System.out.println("lo que es" + contenido.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //catalogo = contenido;

            } else {
                block();
            }
        }
    }
}