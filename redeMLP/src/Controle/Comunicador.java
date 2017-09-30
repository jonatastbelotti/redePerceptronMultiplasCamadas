package Controle;

import javax.swing.JButton;
import javax.swing.JTextArea;

/**
 *
 * @author Jônatas Trabuco Belotti [jonatas.t.belotti@hotmail.com]
 */
public abstract class Comunicador {
  private static JTextArea jTxtLog = null;
  private static JButton jBtnTestar = null;

  public static void setCampo(JTextArea campo) {
    jTxtLog = campo;
  }
  
  public static void setBotao(JButton botao) {
    jBtnTestar = botao;
  }
  
  public static void iniciarLog(String texto){
    if (jTxtLog != null) {
      jTxtLog.setText(texto);
    }
  }
  
  public static void addLog(String texto) {
    if (jTxtLog != null) {
      jTxtLog.append("\n" + texto);
      jTxtLog.setCaretPosition(jTxtLog.getText().length());
    }
  }
  
  public static void setEnabledBotao(boolean valor) {
    if (jBtnTestar != null) {
      jBtnTestar.setEnabled(valor);
    }
  }

}
