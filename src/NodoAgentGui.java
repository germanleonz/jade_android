import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class NodoAgentGui extends JFrame {	
	private NodoAgent myAgent;
	
	private JTextField titleField;
	
	NodoAgentGui(NodoAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("Nombre archivo:"));
		titleField = new JTextField(20);
		p.add(titleField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Buscar");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					myAgent.AskHolders(title);
					titleField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(NodoAgentGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void show() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.show();
	}	
}
