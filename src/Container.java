import Model.Fase;

import javax.swing.*;
import java.awt.*;

public class Container extends JFrame {
    public Container(){
        Fase fase = new Fase();
        add(fase);
        setContentPane(fase);
        fase.setPreferredSize(new Dimension(1024, 768));
        pack();
        setTitle("Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // define botão de fechar.
        setLocationRelativeTo(null); // define ponto inicial da tela (meio)
        setVisible(true);
        SwingUtilities.invokeLater(fase::inicializar);
    }
    public static void main(String[] args) {
        new Container();
    }

}
