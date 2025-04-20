import Model.Fase;
import Model.Player;

import javax.swing.*;
import java.awt.*;

public class Jogo extends JFrame {
    private Fase fase;
    public Jogo(){
        this(false);
    }
    public Jogo(boolean isHost){
        fase = new Fase();
        fase.setHost(isHost);
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
        new Jogo();
    }
    public Fase getFase(){
        return fase;
    }

    public Player getPlayer1(){
        return fase.getPlayer();
    }
    public Player getPlayer2(){
        return fase.getPlayer2();
    }

}
