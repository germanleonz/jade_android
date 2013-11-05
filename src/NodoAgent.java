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
    String nodename ;
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

    private class SeekSuperNodes extends Behaviour {
        public void action() {
          DFAgentDescription template = new DFAgentDescription();
          ServiceDescription sd = new ServiceDescription();
          sd.setType("supernodo");
          template.addServices(sd);
          try {
          	DFAgentDescription[] result = DFService.search(myAgent, template); 
          	System.out.println("Found the following super nodes");
            superNodos = new ArrayList<AID>();
            for (int i = 0; i < result.length; ++i) {
              superNodos.add(result[i].getName());
	          System.out.println(superNodos.get(i).getName());
              if(i==0){
                 System.out.println("Me registro!");
                // Send the cfp to all sellers
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

        public void action() {
            
            // Receive all proposals/refusals from seller agents
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                // Reply received
                try{
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        // This is an offer 
                        String arch = (String) reply.getContent();
                        Fichero file = (Fichero) reply.getContentObject();
                        LinkedList holders = file.getHolders();

                        /*Accion para seleccionar un holder confiable*/

                        AID holder = (AID)holders.getFirst();
                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM_IF);
                        inform.addReceiver(holder);
                        inform.setContent(file.getNombre());
                        inform.setConversationId("download-file");
                        myAgent.send(inform);

                    } 

                    if (reply.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("Attempt failed archivo no encontrado");
                        myGui.visibleMensajeError();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
               
            } else {
                block();
            }
                    
                    
        }

        public boolean done() {
            if (fileHolder == null) {
                //System.out.println("Attempt failed: "+targetFileName+" not available for sale");
            }
            return (fileHolder != null);
        }
    }



    private class SendFile extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                String nombre = msg.getContent();
                File arch = new File("./"+nodename+"Files_JADE/"+nombre);
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
                
                Object[] fileContent= lista.toArray();
                byte[] bytefileContent= new byte[lista.size()];
                for(int i=0; i<lista.size(); i++){
                    bytefileContent[i]= (((Integer)fileContent[i]).byteValue());
                }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REQUEST);
                reply.setByteSequenceContent(bytefileContent);
                reply.setContent(nombre);
                myAgent.send(reply);
            }

            }else{
                block();
            }

          }
    }
    
   /**
     This is invoked by the GUI when the user adds a new file for upload
   */
  public void upload(final String title) {
    addBehaviour(new OneShotBehaviour() {
      public void action() {
        MessageTemplate mt;
        Fichero f;
        System.out.println("Quiero subir el archivo : "+title);
        
        // Le aviso al superNOdo que tengo un nuevo archivo, y le envio
        // el objeto de tipo Archivo
        ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
        cfp.addReceiver(superNodos.get(0));   
        f = new Fichero(getAID(),title);
        
        try {
        	cfp.setContentObject(f);
        }catch (Exception io){
            io.printStackTrace();
        }
        
        cfp.setConversationId("NuevoArchivo");
        cfp.setReplyWith("request"+System.currentTimeMillis()); // Unique value
        myAgent.send(cfp);
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("seek-holder"),
                MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
      }
    } );
  }

    private class ReceiveFile extends CyclicBehaviour {
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = myAgent.receive(mt);
        if(msg != null){
            String arch = "./"+nodename+"Files_JADE/"+msg.getContent();
            FileOutputStream out = null;
            byte[] fileContent = msg.getByteSequenceContent();
            // Almacenar contenido                        
            try{
                out = new FileOutputStream(arch);        
                int cont=0;
                out.write(fileContent);
                      
            }catch(Exception e ){
                System.out.println("error");
            }
            
        }else{
            block();
        }

      }
    }
}
