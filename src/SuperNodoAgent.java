import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class SuperNodoAgent extends Agent {
    private Hashtable catalogo;

    protected void setup() {
        catalogo = new Hashtable();
        catalogo.put("archivo1", "nodo1");

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
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Supernodo-agent "+getAID().getName()+" terminating.");
    }

    private class WhoHasFileServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String fileName = msg.getContent();
                ACLMessage reply = msg.createReply();

                String fileHolder = (String) catalogo.get(fileName);
                if (fileHolder != null) {
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
}
