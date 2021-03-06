
import jade.core.Agent;
import jade.core.AID;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import jade.core.behaviours.*;
import jade.wrapper.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import jade.domain.introspection.*;

public class NodoAgent extends Agent {

    private String targetFileName;
    private ArrayList<AID> superNodos;
    String nodename;
    String capacidadMax;  // capacidad con la que inicia
    int capacidadAct; // capacidad disponible
    // GUI a través de la cual el cliente podra interactuar :P
    private NodoAgentGUI myGui;
    
    //LinkedList de mis archivos publicados
    private LinkedList<Fichero> misPublicaciones;
    

    protected void setup() {


        AMSSubscriber myAMSSubscriber = new AMSSubscriber() {
            protected void installHandlers(Map handlers) {
                //Asociar el evento solo a agentes muertos
                EventHandler terminationsHandler = new EventHandler() {
                    public void handle(Event ev) {
                        DeadAgent da = (DeadAgent) ev;
                        System.out.println("Dead agent " + da.getAgent().getName());
                        AID agente = da.getAgent();
                        if (superNodos.contains(agente)) {
                            superNodos.remove(agente);
                        }
                    }
                };
                handlers.put(IntrospectionVocabulary.DEADAGENT,
                        terminationsHandler);
            }
        };
        addBehaviour(myAMSSubscriber);
        
        //Inicializo mis publicaciones
        misPublicaciones = new LinkedList();

        System.out.println("Nodo-agent " + getAID().getName() + " is ready.");
        nodename = getAID().getName().substring(0, getAID().getName().indexOf("@"));
        capacidadAct = 0;
        superNodos = new ArrayList();
        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Create and show the GUI 
            myGui = new NodoAgentGUI(this);
            myGui.show();

            capacidadMax = (String) args[0];

            addBehaviour(new SeekSuperNodes());
            addBehaviour(new AskForHolders());
            addBehaviour(new SendFile());
            addBehaviour(new ReceiveFile());
            addBehaviour(new Replicar());
            addBehaviour(new waitMyFiles());

            File folder = new File("./" + nodename + ":Files_JADE");
            if (!folder.exists()) {
                folder.mkdir();
            }

			// Si el nodo ya habia realizado publicaciones y cayo, debe 
			// actualizar su tabla de publicaciones
			getMyFiles();
			
        } else {
            System.out.println("Debe especificar la capacidad maxima");
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Nodo-agent " + getAID().getName() + " terminating.");
    }

    /**
     * Este behaviour se encarga de encontrar a todos los supernodos y de
     * notificarle mi nacimiento al primero de la lista
     */
    private class SeekSuperNodes extends Behaviour {

        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("supernodo");
            template.addServices(sd);
            try {
                //  Buscamos a todos los SuperNodos
                System.out.println("Imprimiendo lista de supernodos que encontre...");
                DFAgentDescription[] result = DFService.search(myAgent, template);
                superNodos = new ArrayList<AID>();
                for (int i = 0; i < result.length; ++i) {
                    superNodos.add(result[i].getName());
                    System.out.println(superNodos.get(i).getName());
                    if (i == 0) {
                        // Me registro y le envio una notificacion al 
                        // primer supernodo que encuentre
                        System.out.println("Me registro!");
                        ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
                        cfp.addReceiver(result[i].getName());
                        cfp.setContent(capacidadMax);
                        cfp.setConversationId("registro");

                        myAgent.send(cfp);
                    }
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

        }

        public boolean done() {
            return true;
        }
    }

    /**
     * This is invoked by the GUI when the user adds a new file for search
     */
    public void AskHolders(final String title) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                MessageTemplate mt;
                System.out.println("Quiero buscar el archivo : " + title);
                // Send the cfp to all sellers
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                cfp.addReceiver(superNodos.get(0));
                cfp.setContent(title);
                cfp.setConversationId("seek-holder");
                cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("seek-holder"),
                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
            }
        });
    }

    private class AskForHolders extends Behaviour {

        private String fileHolder;
        private MessageTemplate mt;
        private int step = 0;
        private AID holder = null;

        public void action() {

            // Receive all proposals/refusals from seller agents
            mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("holders"));
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                // Reply received
                try {
                    //Revizamos permisos y que exista el archivo
                    if (reply.getContent().equals("not-available")) {
                        System.out.println("Attempt failed archivo no encontrado");
                        myGui.visibleMensajeError();
                    } else if (reply.getUserDefinedParameter("permisos").equals("false")) {
                        myGui.visibleMensajeError();
                    } else {
                        // This is an offer 
                        String nombreArchivo = reply.getUserDefinedParameter("nombreArchivo");
                        AID mejorHolder = (AID) reply.getContentObject();

                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                        inform.addReceiver(mejorHolder);
                        inform.setContent(nombreArchivo);
                        System.out.println("El archivo lo tiene" + mejorHolder.getName());

                        System.out.println("Solicitando " + nombreArchivo);
                        inform.setConversationId("download-file");
                        myAgent.send(inform);
                        System.out.println("Solicitud enviada");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }

        public boolean done() {
            if (holder == null) {
                //System.out.println("Attempt failed: "+targetFileName+" not available for sale");
            }
            return (holder != null);
        }
    }

    /**
     * Metodo llamado por la GUI cuando el usuario selecciona el boton subir
     archivo
     */
    public void subirArchivo(final String path) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                MessageTemplate mt;
                Fichero f;
                String nombre;
                System.out.println("Quiero subir el archivo : " + path);

                // Le aviso al superNodo que tengo un nuevo archivo, y le envio
                // el objeto de tipo Archivo
                String[] split = path.split("/");
                //hacemos split 
                ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
                cfp.addReceiver(superNodos.get(0));
                File file = new File(path);
                f = new Fichero(getAID(), split[split.length - 1], file.length());

                // Le aviso al superNodo que tengo un nuevo archivo, y le envio
                // el objeto de tipo Archivo
                cfp = new ACLMessage(ACLMessage.REQUEST);
                cfp.addReceiver(superNodos.get(0));

                //Actualizamos nuestra propia tabla de archivos subidos
                misPublicaciones.add(f);

                //Copiamos el archivo a la carpeta de jade que creamos
                FileInputStream is = null;
                FileOutputStream os = null;
                try {
                    is = new FileInputStream(path);
                    os = new FileOutputStream("./" + nodename + ":Files_JADE/" + f.getNombre());
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    is.close();
                    os.close();
                    cfp.setContentObject(f);
                    cfp.setConversationId("NuevoArchivo");
                    cfp.setReplyWith("request" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("seek-holder"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                } catch (Exception io) {
                    io.printStackTrace();

                }

            }
        });
    }

    private class SendFile extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("download-file"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String nombre = msg.getContent();
                File arch = new File("./" + nodename + ":Files_JADE/" + nombre);
                System.out.println("Solicitaron archivo " + nombre);
                if (arch.exists()) {
                    FileInputStream in = null;
                    LinkedList<Integer> lista = new LinkedList<Integer>();
                    try {
                        in = new FileInputStream(arch);
                        int c;
                        int cont = 0;
                        while ((c = in.read()) != -1) {
                            // Leer byte a byte e insertarlos en la lista
                            lista.add(c);
                        }
                    } catch (Exception e) {
                        System.err.println(e);
                    } catch (OutOfMemoryError b) {
                        System.out.println("Error: El archivo sobrepasa el limite de tamaño");
                        myAgent.doDelete();
                    }

                    System.out.println("Enviando archivo " + nombre);

                    Object[] fileContent = lista.toArray();
                    byte[] bytefileContent = new byte[lista.size()];
                    for (int i = 0; i < lista.size(); i++) {
                        bytefileContent[i] = (((Integer) fileContent[i]).byteValue());
                    }

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REQUEST);
                    //EL byteSequenceContent sobreescribe el Content por eso usamos otro parametro
                    reply.addUserDefinedParameter("file-name", nombre);
                    reply.setByteSequenceContent(bytefileContent);
                    myAgent.send(reply);
                }

            } else {
                block();
            }
        }
    }

    private class ReceiveFile extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String arch = "./" + nodename + ":Files_JADE/" + msg.getUserDefinedParameter("file-name");
                FileOutputStream out = null;
                byte[] fileContent = msg.getByteSequenceContent();
                try {
                    // Almacenar contenido                        

                    out = new FileOutputStream(arch);
                    out.write(fileContent);
                    out.close();

                    // Le aviso a un superNodo que todo funciono chevere con el
                    // nodo sender para que este le aumente la confiabilidad
                    ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                    mensaje.setConversationId("descargaOK");
                    mensaje.addReceiver(superNodos.get(0));
                    mensaje.setContentObject(msg.getSender());

                    myAgent.send(mensaje);
                    System.out.println("Informamos a superNodo -> DescargaOK");

                    // Notificar al super nodo cambio en la capacidad
                    if (msg.getConversationId().equalsIgnoreCase("replicar")) {
                        msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.setConversationId("NuevaCapacidad");
                        msg.addReceiver(superNodos.get(0));
                        msg.setContentObject(getAID());
                        File f = new File(arch);
                        msg.addUserDefinedParameter("tamArch", String.valueOf(f.length()));
                        myAgent.send(msg);
                        System.out.println("Actualizar capacidad");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                block();
            }

        }
    }

    private class Replicar extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("lista replicas"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String nombre = "";
                ACLMessage msgRep = new ACLMessage(ACLMessage.REQUEST);
                byte[] bytefileContent = null;
                // Reply received
                try {
                    // This is an offer 
                    nombre = msg.getUserDefinedParameter("nombreArchivo");
                    AID[] replicas = (AID[]) msg.getContentObject();

                    msgRep.setConversationId("replicar");
                    System.out.println("Replicando archivo " + nombre + "en los nodos");
                    for (AID rep : replicas) {
                        msgRep.addReceiver(rep);
                        System.out.println(rep);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File arch = new File("./" + nodename + ":Files_JADE/" + nombre);

                if (arch.exists()) {
                    FileInputStream in = null;
                    LinkedList<Integer> lista = new LinkedList<Integer>();
                    try {
                        in = new FileInputStream(arch);
                        int c;
                        int cont = 0;
                        while ((c = in.read()) != -1) {
                            // Leer byte a byte e insertarlos en la lista
                            lista.add(c);
                        }
                    } catch (Exception e) {
                        System.err.println(e);
                    } catch (OutOfMemoryError b) {
                        System.out.println("Error: El archivo sobrepasa el limite de tamaño");
                        myAgent.doDelete();
                    }

                    System.out.println("Enviando archivo " + nombre);

                    Object[] fileContent = lista.toArray();
                    bytefileContent = new byte[lista.size()];
                    for (int i = 0; i < lista.size(); i++) {
                        bytefileContent[i] = (((Integer) fileContent[i]).byteValue());
                    }
                }

                msgRep.addUserDefinedParameter("file-name", nombre);
                msgRep.setByteSequenceContent(bytefileContent);
                myAgent.send(msgRep);
            }
        }
    }
    // Le aviso a un superNodo que el nodo sender
    // se cayo y hubo un error en la descarga
    //ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    //msg.setConversationId("descargaKO");
    //msg.addReceiver(superNodos.get(0));   
    //msg.setContentObject(sender);
    //myAgent.send(msg);
    
    /*
    	Metodo llamado desde la GUI para obtener los archivos publicados
    	por este nodo
    */
    private void getMyFiles() {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
            	System.out.println("Deseo mis archivos");
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(superNodos.get(0));
                msg.setConversationId("getmyfiles");
                myAgent.send(msg);
            }
        });
    }
    
    /*
    	Metodo que espera un mensaje de respuesta de algun super nodo
    	indicandome mis archivos publicados
    */
    private class waitMyFiles extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId("yourFiles"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Reply received
                try {
                	//Actualizo mi tabla de publicaciones
                	System.out.println("Recibi mis archivos");
                    misPublicaciones = (LinkedList) msg.getContentObject();
                 
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
    
    /*
    	Metodo que es llamado desde GUI para obtener las publicaciones
    */
    public LinkedList<Fichero> getPublicaciones(){
    	return misPublicaciones;
    }
    

    /*
        Metodo llamado desde la GUI cuando el cliente desea cambiar
        los permisos
    */
    public void setPermisos(final String nombreFichero,final boolean permiso) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                System.out.println("Deseo modificar permisos "+nombreFichero);
                Fichero f = new Fichero();
                f.setNombre(nombreFichero);
                f.setPermisos(permiso);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(superNodos.get(0));
                try {
                    msg.setContentObject(f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                msg.setConversationId("cambiarPermisos");
                myAgent.send(msg);

                for(int i=0;i<misPublicaciones.size();i++){
                    Fichero f1 = misPublicaciones.get(i);

                    if (f1.getNombre().equals(nombreFichero)){
                        f1.setPermisos(permiso);
                        misPublicaciones.remove(i);
                        misPublicaciones.add(f1);
                        break;
                    }

                }

            }
        });
    }
    
}
