package Model;


import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x,y; // Posição do player no mapa;
    private int dx,dy;
    private Image imagem; // Imagem do player;
    private int altura=64, largura=64; // Altura e largura do sprite;
    private int velocidade=3; // Velocidade de movimento do player;
    private List<Tiro> tiros; // Armazena os tiros disparados pelo player temporariamente;
    private boolean isLocal; // Saber se o player é o Host ou Client;
    private boolean isVisible; // Define a visibilidade do jogador;
    private int hp; // quantidade de disparos necessários para ser derrotado;
    private String projectile; // Path do sprite do disparo;
    private int faseHeight, faseWidth; // Altura e Largura da tela;
    private long ultimoTiro; // para armazenar o timestamp do último tiro

    public Player(String path, boolean local, String pathProjectile){
        this.imagem =  new ImageIcon(path).getImage();
        this.projectile = pathProjectile;
        this.isLocal = local;
        tiros = new ArrayList<Tiro>();
        this.isVisible = true;
        this.hp = 5;
        if(isLocal){
            this.y = faseHeight - this.altura;
        }else{
            this.y = 10;
        }
        this.x = faseWidth/2;
    }
    public void load(){
        imagem = imagem.getScaledInstance(64, 64, Image.SCALE_FAST);
    }
    public void update(){
        x += dx; // Atualiza a posição no eixo X a partir da tecla pressionada;
        y += dy; // Atualiza a posição no eixo Y a partir da tecla pressionada;

        // Utilizado para garantir que o player não possa ultrapassar os limites da tela.
        // Se ele tenta ultrapassar é teleportado para o limite permitido;;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (y>=faseHeight-altura) y = faseHeight-altura;
        if (x>=faseWidth-largura) x = faseWidth-largura;
        if(isLocal && y<faseHeight/2) y = faseHeight/2;
        if(!isLocal && y>=faseHeight/2) y = faseHeight/2;
    }
    public void tiro(){
        long agora = System.currentTimeMillis(); // Pega o momento em que o tiro foi disparo;
        int offsetX = x + (largura / 2) - 4; // Define o meio da nave no eixo X;
        int offsetY;
        if(isLocal){ // Utilizado para definir o local da nave no eixo Y onde sai o disparo;
            offsetY = y - 20;
        }else{
            offsetY = y + 40;
        }

        long intervaloTiro = 150;
        if (agora - ultimoTiro >= intervaloTiro) { // Utilizado para dar um "delay" entre cada disparo;
            this.tiros.add(new Tiro(offsetX,offsetY, projectile,faseHeight,faseWidth,isLocal));
            ultimoTiro = agora;
        }
    }
    public Rectangle getBounds(){
        // Cria um retangulo nos limites do sprite que é utilizado para definir as colisões;
        return new Rectangle(x,y,largura,altura);
    }
    public void keyPressed(KeyEvent e){
        int codigo = e.getKeyCode();
        if(codigo == KeyEvent.VK_F){
            tiro(); // Ao pressionar a tecla F, a nave dispara;
        }
        if(codigo == KeyEvent.VK_W){
            // Ao pressionar a tecla W, a nave perde a quantidade de VELOCIDADE(3) no eixo Y,
            // Fazendo com que a nave se movimento para cima;
            dy = -velocidade;
        }
        if(codigo == KeyEvent.VK_S){
            // Ao pressionar a tecla S, a nave ganha 3 no eixo Y,
            // Fazendo com que a nave se movimento para baixo;
            dy = velocidade;
        }
        if(codigo == KeyEvent.VK_A){
            // Ao pressionar a tecla A, a nave perde 3 no eixo X,
            // Fazendo com que a nave se movimento para a esquerda;
            dx = -velocidade;
        }
        if(codigo == KeyEvent.VK_D){
            // Ao pressionar a tecla D, a nave ganha 3 no eixo X,
            // Fazendo com que a nave se movimento para a direita;
            dx = velocidade;
        }
    }
    public void keyRelease(KeyEvent e){
        int codigo = e.getKeyCode();
        // No momento em que uma tecla para de ser pressionada, a posição do jogador para de ser atualizada;
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
        // Método utilizado para definir dinamicamente as dimensões da tela, quando a tela é expandida,
        // ou redimensionada, sua altura e largura mudam;
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

    public int getAltura() {
        return altura;
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
