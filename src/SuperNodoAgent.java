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
	private Hashtable catalogo;

	/*
		Metodo setup
		Este metodo define la inicializacion de un agente que prestara
		el servicio de superNodo
	*/
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
		addBehaviour(new Propagate());
		addBehaviour(new Actualizar());
		addBehaviour(new Nacimiento());
		
		
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
				superNodos[0] = result[0].getName();
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				// Le enviamos al primer super nodo el mensaje indicandole que acabo de nacer
				cfp.setContent("NuevoNodo");
				cfp.addReceiver(superNodos[0]);
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
		Metodo Propagate
		Este metodo se encarga de actualizar la Tabla de Hash solo en caso de que la solicitud 
		venga de un cliente y propagarla a los otros superNodos. En caso contrario, es que nacio 
		un nuevo nodo y requiere la tabla de hash. 
	*/
	private class Propagate extends CyclicBehaviour {
		public void action() {
			// Recibimos el mensaje que puede venir de otro SuperNodo o de un Nodo
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				
				try {
					if(msg.getContent().equalsIgnoreCase("NuevoNodo")){
						System.out.println("Existe un nuevo nodo!\n");
						// Caso en el que un SuperNodo requiere la Tabla de Hash
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.PROPAGATE);
						// Le enviamos en el mensaje el catalogo
						reply.setContentObject(catalogo);
						myAgent.send(reply);

					}else{
						/** Espacio para  actualizar */
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

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// Mensaje recibido.
				//Archivo pedido
				try {
					// En el mensaje se encuentra la tabla de Hash
					Hashtable contenido = (Hashtable)msg.getContentObject();
					// Esta sera nuestra nueva tabla de hash
					catalogo = contenido;
					System.out.println("Tabla de Hash Actualizada\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				block();
			}
		}
	}

}
