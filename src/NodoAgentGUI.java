/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 *
 * @author krys
 */
public class NodoAgentGUI extends javax.swing.JFrame {

    NodoAgent myAgent;
        // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelCambiosOk;
    private javax.swing.JLabel LabelCargadoExitosa;
    private javax.swing.JLabel LabelDescargaCompleta;
    private javax.swing.JLabel LabelErrorFuente;
    private javax.swing.JLabel LabelInformacion;
    private javax.swing.JLabel LabelIniciandoDescarga1;
    private javax.swing.JLabel LabelNombreArchivo;
    private javax.swing.JLabel LabelPorcentajeDescargado;
    private javax.swing.JLabel LabelRutaArchivo;
    private javax.swing.JLabel LabelSeleccioneArchivo;
    private javax.swing.JLabel LabelUsuarios;
    private javax.swing.JList ListaMisArchivos;
    private javax.swing.JList ListaUsuarios;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnBuscar1;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnExaminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnOtraFuente;
    private javax.swing.JButton btnPermisos;
    private javax.swing.JTextField campoTextoFileName;
    private javax.swing.JTextField campoTextoRutaArchivo;
    private javax.swing.ButtonGroup grupoBotones;
    private javax.swing.JButton jButton1;
    private javax.swing.JFileChooser jFileChooser2;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel labelEstablecerP;
    private javax.swing.JLabel mensajeError;
    private javax.swing.JRadioButton privado;
    private javax.swing.JRadioButton publico;
    private javax.swing.JTextArea textAreaChat;


     public void visibleMensajeError(){
        mensajeError.setVisible(true);
    }

    /**
     * Creates new form NodoAgentGUI
     */
     public NodoAgentGUI(NodoAgent a) {
       
        super(a.getLocalName());
        
        myAgent = a;
        

   

        grupoBotones = new javax.swing.ButtonGroup();
        jFrame1 = new javax.swing.JFrame();
        jFileChooser2 = new javax.swing.JFileChooser();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        LabelNombreArchivo = new javax.swing.JLabel();
        campoTextoFileName = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        mensajeError = new javax.swing.JLabel();
        LabelInformacion = new javax.swing.JLabel();
        LabelIniciandoDescarga1 = new javax.swing.JLabel();
        LabelPorcentajeDescargado = new javax.swing.JLabel();
        LabelDescargaCompleta = new javax.swing.JLabel();
        LabelErrorFuente = new javax.swing.JLabel();
        btnOtraFuente = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        LabelRutaArchivo = new javax.swing.JLabel();
        campoTextoRutaArchivo = new javax.swing.JTextField();
        btnBuscar1 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        LabelCargadoExitosa = new javax.swing.JLabel();
        btnExaminar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaChat = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        ListaUsuarios = new javax.swing.JList();
        LabelUsuarios = new javax.swing.JLabel();
        btnEnviar = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        LabelSeleccioneArchivo = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ListaMisArchivos = new javax.swing.JList();
        publico = new javax.swing.JRadioButton();
        privado = new javax.swing.JRadioButton();
        labelEstablecerP = new javax.swing.JLabel();
        btnPermisos = new javax.swing.JButton();
        LabelCambiosOk = new javax.swing.JLabel();

                grupoBotones.add(publico);
        grupoBotones.add(privado);

        jFileChooser2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooser2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jFileChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jFileChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        LabelNombreArchivo.setText("Nombre del archivo :");

        campoTextoFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTextoFileNameActionPerformed(evt);
            }
        });

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String title = campoTextoFileName.getText().trim();
                    myAgent.AskHolders(title);
                   // campoTextoFileName.setText("");
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(NodoAgentGUI.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
                }
            }
        } );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(LabelNombreArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(campoTextoFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnBuscar, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(32, 32, 32))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelNombreArchivo)
                    .addComponent(campoTextoFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(btnBuscar))
        );

        mensajeError.setText("Lo sentimos, no se han encontrado coincidencias con el nombre del archivo");
        mensajeError.setVisible(false);

        LabelInformacion.setText("Informacion : ");
        LabelInformacion.setVisible(false);

        LabelIniciandoDescarga1.setText("Iniciando la Descarga ... ");
        LabelIniciandoDescarga1.setVisible(false);

        LabelPorcentajeDescargado.setText("Porcentaje Descargado :  ");
        LabelPorcentajeDescargado.setVisible(false);

        LabelDescargaCompleta.setText("Descarga Completa! ");
        LabelDescargaCompleta.setVisible(false);

        LabelErrorFuente.setText("Ha ocurrido un Error! Presione \"Buscar otra fuente\" si desea buscar nuevamente");
        LabelErrorFuente.setVisible(false);

        btnOtraFuente.setText("Buscar otra fuente");
        btnOtraFuente.setVisible(false);
        btnOtraFuente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOtraFuenteActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
             
                   campoTextoFileName.setText("");
                   mensajeError.setVisible(false);
                   LabelInformacion.setVisible(false);
                   LabelIniciandoDescarga1.setVisible(false);
                   LabelPorcentajeDescargado.setVisible(false);
                   LabelDescargaCompleta.setVisible(false);
                    LabelErrorFuente.setVisible(false);
            }
        } );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LabelErrorFuente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LabelPorcentajeDescargado, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mensajeError, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LabelIniciandoDescarga1)
                            .addComponent(LabelInformacion, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(LabelDescargaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 60, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnOtraFuente)
                .addGap(67, 67, 67)
                .addComponent(btnLimpiar)
                .addGap(92, 92, 92))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mensajeError)
                .addGap(18, 18, 18)
                .addComponent(LabelInformacion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LabelIniciandoDescarga1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LabelPorcentajeDescargado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LabelDescargaCompleta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LabelErrorFuente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOtraFuente)
                    .addComponent(btnLimpiar))
                .addGap(27, 27, 27))
        );

        jTabbedPane1.addTab("Descargar ", jPanel1);

        LabelRutaArchivo.setText("Ruta del archivo :");

        campoTextoRutaArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTextoRutaArchivoActionPerformed(evt);
            }
        });

        btnBuscar1.setText("Limpiar");
        btnBuscar1.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
             
                   campoTextoRutaArchivo.setText("");
                   LabelCargadoExitosa.setVisible(false);
            }
        } );

        jButton1.setText("Subir");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myAgent.subirArchivo(campoTextoRutaArchivo.getText());
                LabelCargadoExitosa.setVisible(true);
            }
        });


        LabelCargadoExitosa.setText("Se ha cargado el archivo exitosamente !");
        LabelCargadoExitosa.setVisible(false);

        btnExaminar.setText("Examinar...");
        btnExaminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExaminarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBuscar1)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(202, 202, 202))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(LabelCargadoExitosa))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(LabelRutaArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(campoTextoRutaArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExaminar)
                .addGap(79, 79, 79))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelRutaArchivo)
                    .addComponent(campoTextoRutaArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExaminar))
                .addGap(28, 28, 28)
                .addComponent(LabelCargadoExitosa)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 206, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(btnBuscar1))
                .addContainerGap())
        );
        jTabbedPane1.addTab("Subir Archivo", jPanel2);

        textAreaChat.setColumns(20);
        textAreaChat.setRows(5);
        jScrollPane1.setViewportView(textAreaChat);

        jTextField1.setText("Escribe algo...");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        ListaUsuarios.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(ListaUsuarios);

        LabelUsuarios.setText("Usuarios:");

        btnEnviar.setText("Enviar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(LabelUsuarios)
                                .addGap(0, 90, Short.MAX_VALUE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEnviar)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(LabelUsuarios)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)))
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEnviar))
                .addGap(21, 21, 21))
        );

        //jTabbedPane1.addTab("Chat", jPanel3);

        LabelSeleccioneArchivo.setText("Seleccione un archivo : ");

        ListaMisArchivos.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(ListaMisArchivos);

        publico.setText("Público");

        privado.setText("Privado");

        labelEstablecerP.setText("Establecer permisos: ");
        labelEstablecerP.setVisible(false);

        btnPermisos.setText("Aceptar");
        btnPermisos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
            }
        });

        LabelCambiosOk.setText("Los cambios se han efectuado correctamente ");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LabelCambiosOk)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(labelEstablecerP)
                        .addGap(18, 18, 18)
                        .addComponent(publico)
                        .addGap(90, 90, 90)
                        .addComponent(privado, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(LabelSeleccioneArchivo)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(132, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnPermisos)
                .addGap(61, 61, 61))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LabelSeleccioneArchivo))
                .addGap(38, 38, 38)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(publico)
                    .addComponent(privado)
                    .addComponent(labelEstablecerP))
                .addGap(60, 60, 60)
                .addComponent(LabelCambiosOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                .addComponent(btnPermisos)
                .addGap(59, 59, 59))
        );

        jTabbedPane1.addTab("Configurar Permisos", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 606, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoTextoFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTextoFileNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoTextoFileNameActionPerformed

    private void btnOtraFuenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOtraFuenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnOtraFuenteActionPerformed

    private void campoTextoRutaArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTextoRutaArchivoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoTextoRutaArchivoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void btnPermisosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPermisosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPermisosActionPerformed

    private void btnExaminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExaminarActionPerformed
        jFrame1.pack();
        jFrame1.setVisible(true);
    }//GEN-LAST:event_btnExaminarActionPerformed

    private void jFileChooser2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser2ActionPerformed
        JFileChooser selectorArchivo = (JFileChooser)evt.getSource();
        String command = evt.getActionCommand();
        if (command.equals(JFileChooser.APPROVE_SELECTION)){
            File archivoSeleccionado = selectorArchivo.getSelectedFile();
            
            campoTextoRutaArchivo.setText(archivoSeleccionado.getAbsolutePath());
            jFrame1.setVisible(false);
        }else if (command.equals(JFileChooser.CANCEL_SELECTION)){
            JOptionPane.showMessageDialog(this,"Selecciona un archivo ...");
            
        }
    }//GEN-LAST:event_jFileChooser2ActionPerformed

        
     public void show() {
        pack();
        super.show();
    }

    // End of variables declaration//GEN-END:variables
}
