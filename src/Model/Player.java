package Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x,y;
    private int dx,dy;
    private Image imagem;
    private int altura=64, largura=64;
    private int velocidade=3;
    private List<Tiro> tiros;
    private boolean isLocal;
    private boolean isVisible;
    private int hp;
    private String projectile;
    private int faseHeight, faseWidth;

    public Player(String path, boolean local, String pathProjectile){
        this.imagem =  new ImageIcon(path).getImage();
        this.projectile = pathProjectile;
        this.isLocal = local;
        tiros = new ArrayList<Tiro>();
        this.isVisible = true;
        this.hp = 3;
        if(isLocal){
            this.y = faseHeight - this.altura;
        }else{
            this.y = 10;
        }
        this.x = faseWidth/2;
//        this.faseHeight = faseHeight;
//        this.faseWidth = faseWidth;
    }
    public void load(){
        imagem = imagem.getScaledInstance(64, 64, Image.SCALE_FAST);
    }
    public void update(){
        x += dx;
        y += dy;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (y>=faseHeight-altura) y = faseHeight-altura;
        if (x>=faseWidth-largura) x = faseWidth-largura;
        if(isLocal && y<faseHeight/2) y = faseHeight/2;
        if(!isLocal && y>=faseHeight/2) y = faseHeight/2;
    }
    public void tiro(){
        System.out.println("Y:"+y+" X:"+x+"\n");
        System.out.println("Altura:"+faseHeight+" Largura"+faseWidth+"\n");
        System.out.println("Metade:"+faseHeight/2+ " | "+faseWidth/2+"\n");
        int offsetX = x + (largura / 2) - 4;
        int offsetY;
        if(isLocal){
            offsetY = y - 20;
        }else{
            offsetY = y + 40;
        }
        this.tiros.add(new Tiro(offsetX,offsetY, projectile,faseHeight,faseWidth,isLocal));
    }
    public Rectangle getBounds(){
        return new Rectangle(x,y,largura,altura);
    }
    public void keyPressed(KeyEvent e){
        int codigo = e.getKeyCode();
        if(codigo == KeyEvent.VK_F){
            tiro();
        }
        if(codigo == KeyEvent.VK_W){
            dy = -velocidade;
        }
        if(codigo == KeyEvent.VK_S){
            dy = velocidade;
        }
        if(codigo == KeyEvent.VK_A){
            dx = -velocidade;
        }
        if(codigo == KeyEvent.VK_D){
            dx = velocidade;
        }
    }
    public void keyRelease(KeyEvent e){
        int codigo = e.getKeyCode();
        if(codigo == KeyEvent.VK_W){
            dy = 0;
        }
        if(codigo == KeyEvent.VK_S){
            dy = 0;
        }
        if(codigo == KeyEvent.VK_A){
            dx = 0;
        }
        if(codigo == KeyEvent.VK_D){
            dx = 0;
        }
    }
    public void setFaseDimensoes(int altura, int largura) {
        this.faseHeight = altura;
        this.faseWidth = largura;
        setX(faseWidth/2);
        if(isLocal){
            setY(faseHeight - this.altura);
        }else{
            setY(10);
        }
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public Image getImagem(){
        return imagem;
    }

    public List<Tiro> getTiros() {
        return tiros;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
}
