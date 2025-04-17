package Model;

import javax.swing.*;
import java.awt.*;

public class Tiro {
    private Image image;
    private int x, y;
    private int largura, altura;
    private boolean isVisible; // controla a visibilidade do tiro na tela
    private static int velocidade = 2; // velocidade do tiro
    private int alturaPainel;
    private int larguraPainel;
    private boolean isLocal;

    public Tiro(int x, int y, String path, int alturaP, int larguraP, boolean local){
        this.image = new ImageIcon(path).getImage();
        this.x = x;
        this.y = y;
        isVisible = true;
        this.alturaPainel = alturaP;
        this.larguraPainel = larguraP;
        this.isLocal = local;
    }

    public void load(){
        this.largura = image.getWidth(null);
        this.altura = image.getHeight(null);
    }
    public void update(){
        if(isLocal){
            this.y -= velocidade;
            if(this.y < 0){
                isVisible = false;
            }
        }else{
            this.y += velocidade;
            if(this.y > alturaPainel){
                isVisible = false;
            }
        }
    }

    public Rectangle getBounds(){
        return new Rectangle(x,y,largura,altura);
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public static int getVelocidade() {
        return velocidade;
    }

    public static void setVelocidade(int velocidade) {
        Tiro.velocidade = velocidade;
    }

    public Image getImage() {
        return image;
    }
}
