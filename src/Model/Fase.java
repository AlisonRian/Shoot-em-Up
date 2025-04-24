package Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Fase extends JPanel implements ActionListener {
    private Image fundo;
    private Player player, player2;
    private Timer timer;
    private List<Estrelas> estrelas;
    int alturaVisivel, larguraVisivel;
    private boolean isHost;
    
    public Fase(){
        setFocusable(true);
        setDoubleBuffered(true);
        ImageIcon referencia = new ImageIcon("images/background.png"); // Pega a referencia do background
        fundo = referencia.getImage(); // define o a imagem pro fundo

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Verifica se a tela é redimensionada, se for atualiza os valores de
                // Altura e Largura em cada player
                alturaVisivel = getHeight();
                larguraVisivel = getWidth();
                if (player != null) {
                    player.setFaseDimensoes(alturaVisivel, larguraVisivel);
                }
                if (player2 != null) {
                    player2.setFaseDimensoes(alturaVisivel, larguraVisivel);
                }
            }
        });

        player = new Player("images/nave1.png", true, "images/projectile_1.png");
        player.load();

        player2 = new Player("images/nave2_edit.png", false, "images/projectile_2.png");
        player2.load();

        addKeyListener(new Teclado());

        timer = new Timer(5, this);
        timer.start();
        inicializarEstrelas();
    }

    public void inicializar(){
        alturaVisivel = getHeight();
        larguraVisivel = getWidth();
        
        // Initialize player positions
        player.setFaseDimensoes(alturaVisivel, larguraVisivel);
        player2.setFaseDimensoes(alturaVisivel, larguraVisivel);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graficos = (Graphics2D) g;
        // Exibe a imagem de fundo no painel;
        graficos.drawImage(fundo, 0, 0, getWidth(), getHeight(), null);
        
        // Exibir mensagem de vitória se um jogador venceu
        if(player.isVisible() && !player2.isVisible()){
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Player 1 Wins!", 50, 100);
        } else if(!player.isVisible() && player2.isVisible()){
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Player 2 Wins!", 50, 100);
        }

        // Percorre o array exibindo a estrelas;
        for (Estrelas s : estrelas) {
            s.load();
            graficos.drawImage(s.getImage(), s.getX(), s.getY(), this);
        }
        
        // Se o player ainda estiver vivo, exibe o player;
        if(player.isVisible()){
            Image hp1; Image barra1;
            hp1 = new ImageIcon("images/health.png").getImage();
            barra1 = new ImageIcon("images/health_bar.png").getImage();
            int bLargura = 20; int bAltura = 10;
            int xInicial = larguraVisivel-112; int yInicial = alturaVisivel-22;
            int soma = 0;
            graficos.drawImage(hp1, larguraVisivel-114 , alturaVisivel-34, 104, 24, this);
            for(int i=0; i<player.getHp(); i++){
                graficos.drawImage(barra1, xInicial+soma, yInicial, bLargura, bAltura, this);
                soma+=20;
            }
            player.draw(g);
            List<Tiro> tiros = player.getTiros();
            for (Tiro m : tiros) {
                m.load();
                m.draw(g);
            }
        }
        
        if(player2.isVisible()){
            Image hp2; Image barra2;
            hp2 = new ImageIcon("images/health.png").getImage();
            barra2 = new ImageIcon("images/health_bar.png").getImage();
            int bLargura2 = 20; int bAltura2 = 10;
            int xInicial2 = 10+2; int yInicial2 = 22;
            int soma2 = 0;
            graficos.drawImage(hp2, 10, 10, 104, 24, this);
            for(int i=0; i<player2.getHp(); i++){
                graficos.drawImage(barra2, xInicial2+soma2, yInicial2, bLargura2, bAltura2, this);
                soma2+=20;
            }
            player2.draw(g);
            List<Tiro> tiros2 = player2.getTiros();
            for (Tiro m : tiros2) {
                m.load();
                m.draw(g);
            }
        }

        g.dispose();
    }
    
    public void inicializarEstrelas(){
        // Inicializa as estrelas em posições aleátorias e armazena em um array;
        int coordenadas[] = new int[5];
        estrelas = new ArrayList<Estrelas>();
        for(int i=0; i<coordenadas.length; i++){
            int altura = this.getHeight() > 0 ? this.getHeight() : 768;
            int largura = this.getWidth() > 0 ? this.getWidth() : 1024;
            int y = (int) (Math.random() * altura + 700);
            int x = (int) (Math.random() * largura);
            estrelas.add(new Estrelas(x, y, this.getHeight(), this.getWidth()));
        }
    }
    
    public void checarColisoes(Player p, Player enemy){
        // Verifica se algum disparo atingiu o player e reduz o HP;
        Rectangle enemyBounds = enemy.getBounds();
        Rectangle formaTiro;
        List<Tiro> tiros = p.getTiros();
        int hp;
        for(int i=0; i<tiros.size(); i++){
            Tiro temp = tiros.get(i);
            formaTiro = temp.getBounds();
            if(formaTiro.intersects(enemyBounds)){
                hp = enemy.getHp();
                hp--;
                enemy.setHp(hp);
                if(hp<=0){
                    enemy.setVisible(false);
                }
                temp.setVisible(false);
            }
        }
    }
    
    public void checarColisoesVisual(Player p, Player enemy){
        // Verifica se algum disparo atingiu o player e reduz o HP;
        Rectangle enemyBounds = enemy.getBounds();
        Rectangle formaTiro;
        List<Tiro> tiros = p.getTiros();
        for(int i=0; i<tiros.size(); i++){
            Tiro temp = tiros.get(i);
            formaTiro = temp.getBounds();
            if(formaTiro.intersects(enemyBounds)){
                temp.setVisible(false);
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        player2.update();
        player.update();
        
        for(int j=0; j<estrelas.size(); j++){
            Estrelas s = estrelas.get(j);
            if(s.isVisible()){
                s.update();
            } else {
                estrelas.remove(j);
            }
        }
        
        List<Tiro> tiros = player.getTiros();
        for(int i=0; i<tiros.size(); i++){
            Tiro m = tiros.get(i);
            if(m.isVisible()){
                m.update();
            } else {
                tiros.remove(i);
            }
        }
        
        List<Tiro> tiros2 = player2.getTiros();
        for(int i=0; i<tiros2.size(); i++){
            Tiro m = tiros2.get(i);
            if(m.isVisible()){
                m.update();
            } else {
                tiros2.remove(i);
            }
        }
        
        if(isHost){
            checarColisoes(player, player2);
            checarColisoes(player2, player);
        } else {
            checarColisoesVisual(player, player2);
            checarColisoesVisual(player2, player);
        }
        
        repaint();
    }

    public Player getPlayer(){
        return player;
    }
    
    public Player getPlayer2(){
        return player2;
    }
    
    private class Teclado extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e){
            if(isHost){
                player.keyPressed(e);
            } else {
                player2.keyPressed(e);
            }
        }
        
        @Override
        public void keyReleased(KeyEvent e){
            if(isHost){
                player.keyRelease(e);
            } else {
                player2.keyRelease(e);
            }
        }
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}