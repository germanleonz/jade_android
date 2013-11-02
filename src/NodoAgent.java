import jade.core.Agent;
import jade.core.AID;
import java.io.File;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class NodoAgent extends Agent {
    private String targetFileName;
    private AID[] superNodos;

    protected void setup() {
        System.out.println("Nodo-agent "+getAID().getName()+" is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetFileName = (String) args[0];
            System.out.println("Target file is " + targetFileName);

            addBehaviour(new SeekSuperNodes());
            addBehaviour(new AskForHolders());
        }
        else {
            // Make the agent terminate
            System.out.println("No file name specified");
            doDelete();
        }

        File folder = new File("./Descargas_JADE");
        if (!folder.exists()) { 
            folder.mkdir();
        }
        
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Nodo-agent "+getAID().getName()+" terminating.");
    }

    //Funcion privada que reviza si los nodos aun estan activos
    //No esta funcionando
    private void getSuperNode(){
        for(int i=0;i < superNodos.length; ++i){
            System.out.println("El HAP"+this.superNodos[0].getHap());
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
            superNodos = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
              superNodos[i] = result[i].getName();
	          System.out.println(superNodos[i].getName());
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

    private class AskForHolders extends Behaviour {
        private String fileHolder; 
        private MessageTemplate mt;
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(superNodos[0]);
                    cfp.setContent(targetFileName);
                    cfp.setConversationId("seek-holder");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("seek-holder"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // This is an offer 
                            fileHolder = (String) reply.getContent();
                            System.out.println("File holder " + fileHolder); 
                        }
                        step = 2; 
                    } else {
                        block();
                    }
                    break;
            }        
        }

        public boolean done() {
            if (fileHolder == null) {
                System.out.println("Attempt failed: "+targetFileName+" not available for sale");
            }
            return (fileHolder != null);
        }
    }

}
