package Model;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Estrelas {
    private int x,y;
    private Image imagem;
    private int largura, altura;
    private boolean isVisible;
    public static int velocidade = 1;
    public int faseHeigth, faseWidth;
    public Estrelas(int x, int y, int heigth, int width){
        this.x = x;
        this.y = y;
        this.imagem = new ImageIcon("images/stars_1.png").getImage();
        this.isVisible = true;
        this.faseHeigth = heigth;
        this.faseWidth = width;
    }
    public void load(){
        this.largura = this.imagem.getWidth(null);
        this.altura = this.imagem.getHeight(null);
    }

    public void update(){
        if(y>faseHeigth){
            this.y = altura;
            Random randomY = new Random();
            int m = randomY.nextInt(500);
            this.y = m+faseHeigth;
            Random  randomX = new Random();
            int n = randomX.nextInt(1920);
            this.x = n+0;
        }else{
            this.y += velocidade;
        }
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
        Estrelas.velocidade = velocidade;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public Image getImage() {
        return imagem;
    }
}
