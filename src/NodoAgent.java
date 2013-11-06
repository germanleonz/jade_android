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

public class NodoAgent extends Agent {
    private String targetFileName;
    private ArrayList<AID> superNodos;
    String nodename;
    String capacidadMax;  // capacidad con la que inicia
    int capacidadAct; // capacidad disponible

    // GUI a través de la cual el cliente podra interactuar :P
    private NodoAgentGUI myGui;

    protected void setup() {
        System.out.println("Nodo-agent "+getAID().getName()+" is ready.");
        nodename = getAID().getName().substring(0,getAID().getName().indexOf("@"));
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

            File folder = new File("./"+nodename+":Files_JADE");
            if (!folder.exists()) {
                folder.mkdir();
            }


        }else{
            System.out.println("Debe especificar la capacidad maxima");
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Nodo-agent "+getAID().getName()+" terminating.");
    }

    //Funcion privada que revisa si los nodos aun estan activos
    //No esta funcionando 
    private void getSuperNode() throws Exception {
        Object cc = getContainerController().getPlatformController(); 
        System.out.println(cc);
        for(int i=0;i < superNodos.size(); ++i){
            //System.out.println(this.superNodos[i].getLocalName());
            //System.out.println(cc.getAgent(this.superNodos[i].getLocalName()));
            //System.out.println("El HAP"+this.superNodos[i].getHap());
        }
    }

    /**
      Este behaviour se encarga de encontrar a todos los supernodos
      y de notificarle mi nacimiento al primero de la lista
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
                    if (i==0) {
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
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }

        }

        public boolean done() {
            return true;
        }
    }

    /**
      This is invoked by the GUI when the user adds a new file for search
      */
    public void AskHolders(final String title) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                MessageTemplate mt;
                System.out.println("Quiero buscar el archivo : "+title);
                // Send the cfp to all sellers
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                cfp.addReceiver(superNodos.get(0));
                cfp.setContent(title);
                cfp.setConversationId("seek-holder");
                cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("seek-holder"),
                    MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
            }
        } );
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
                try{
                    //Revizamos permisos y que exista el archivo
                    if (reply.getContent().equals("not-available")) {
                        System.out.println("Attempt failed archivo no encontrado");
                        myGui.visibleMensajeError();
                    }else if(reply.getUserDefinedParameter("permisos").equals("false")){
                        myGui.visibleMensajeError();
                    }else{
                        // This is an offer 
                        String nombreArchivo = reply.getUserDefinedParameter("nombreArchivo");
                        AID mejorHolder      = (AID) reply.getContentObject();

                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                        inform.addReceiver(mejorHolder);
                        inform.setContent(nombreArchivo);
                        System.out.println("El archivo lo tiene"+mejorHolder.getName());

                        System.out.println("Solicitando " + nombreArchivo);
                        inform.setConversationId("download-file");
                        myAgent.send(inform);
                        System.out.println("Solicitud enviada");
                    }

                }catch(Exception e){
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
      This is invoked by the GUI when the user adds a new file for upload
      */
    public void upload(final String path) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                MessageTemplate mt;
                Fichero f;
                String nombre;
                System.out.println("Quiero subir el archivo : "+path);

                // Le aviso al superNodo que tengo un nuevo archivo, y le envio
                // el objeto de tipo Archivo
                String[] split = path.split("/");
                //hacemos split 
                ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
                cfp.addReceiver(superNodos.get(0));   
                File file =new File(path);
                f = new Fichero(getAID(),split[split.length-1],file.length());

                // Le aviso al superNodo que tengo un nuevo archivo, y le envio
                // el objeto de tipo Archivo
                cfp = new ACLMessage(ACLMessage.REQUEST);
                cfp.addReceiver(superNodos.get(0));   

                //Copiamos el archivo a la carpeta de jade que creamos
                FileInputStream is = null;
                FileOutputStream os = null;
                try {
                    is = new FileInputStream(path);
                    os = new FileOutputStream("./"+nodename+":Files_JADE/"+f.getNombre());
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    is.close();
                    os.close();
                    cfp.setContentObject(f);
                    cfp.setConversationId("NuevoArchivo");
                    cfp.setReplyWith("request"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("seek-holder"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                }catch (Exception io){
                    io.printStackTrace();

                }

            }
        } );
    }

    private class SendFile extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("download-file"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String nombre = msg.getContent();
                File arch = new File("./"+nodename+":Files_JADE/"+nombre);
                System.out.println("Solicitaron archivo " + nombre);
                if(arch.exists()){
                    FileInputStream in = null;
                    LinkedList<Integer> lista= new LinkedList<Integer>();
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
                    }catch(OutOfMemoryError b){
                        System.out.println("Error: El archivo sobrepasa el limite de tamaño");
                        myAgent.doDelete();
                    }

                    System.out.println("Enviando archivo "+nombre);

                    Object[] fileContent= lista.toArray();
                    byte[] bytefileContent= new byte[lista.size()];
                    for(int i=0; i<lista.size(); i++){
                        bytefileContent[i]= (((Integer)fileContent[i]).byteValue());
                    }

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REQUEST);
                    //EL byteSequenceContent sobreescribe el Content por eso usamos otro parametro
                    reply.addUserDefinedParameter("file-name", nombre);
                    reply.setByteSequenceContent(bytefileContent);
                    myAgent.send(reply);
                }

            }else{
                block();
            }
        }
    }

    private class ReceiveFile extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                String arch = "./"+nodename+":Files_JADE/"+msg.getUserDefinedParameter("file-name");
                FileOutputStream out = null;
                byte[] fileContent = msg.getByteSequenceContent();
                try{
                    // Almacenar contenido                        

                    out = new FileOutputStream(arch);        
                    out.write(fileContent);
                    out.close();

                }catch(Exception e ){
                    e.printStackTrace();
                }

            } else {
                block();
            }

        }
    }

    private class Replicar extends CyclicBehaviour {
        public void action(){
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("lista replicas"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String nombre = "";
                ACLMessage msgRep = new ACLMessage(ACLMessage.REQUEST);
                byte[] bytefileContent = null;
                // Reply received
                try{
                    // This is an offer 
                    nombre = msg.getUserDefinedParameter("nombreArchivo");
                    AID[] replicas      = (AID[]) msg.getContentObject();

                    msgRep.setConversationId("replicar");
                    System.out.println("Replicando archivo " + nombre + "en los nodos");
                    for (AID rep : replicas){
                        msgRep.addReceiver(rep);
                        System.out.println(rep);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }         

                File arch = new File("./"+nodename+":Files_JADE/"+nombre);

                if(arch.exists()){
                    FileInputStream in = null;
                    LinkedList<Integer> lista= new LinkedList<Integer>();
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
                    }catch(OutOfMemoryError b){
                        System.out.println("Error: El archivo sobrepasa el limite de tamaño");
                        myAgent.doDelete();
                    }

                    System.out.println("Enviando archivo "+nombre);

                    Object[] fileContent= lista.toArray();
                    bytefileContent= new byte[lista.size()];
                    for(int i=0; i<lista.size(); i++){
                        bytefileContent[i]= (((Integer)fileContent[i]).byteValue());
                    }
                }

            msgRep.addUserDefinedParameter("file-name", nombre);
            msgRep.setByteSequenceContent(bytefileContent);
            myAgent.send(msgRep);
            }
        }
    }
}

