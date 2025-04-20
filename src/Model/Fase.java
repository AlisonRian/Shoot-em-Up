package Model;

import network.ClientNetwork;
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
    private int alturaVisivel, larguraVisivel;
    private ClientNetwork network;
    private volatile boolean lastShot = false;

    private long lastReceiveTime = -1;
    private int lastReceivedX, lastReceivedY;
    private float velocityX = 0f, velocityY = 0f;

    public Fase() {
        setFocusable(true);
        setDoubleBuffered(true);

        fundo = new ImageIcon("Shoot-em-Up/images/background.png").getImage();

        // Jogador local
        player = new Player("Shoot-em-Up/images/nave1.png", true, "Shoot-em-Up/images/projectile_1.png");
        player.load();

        // Jogador remoto
        player2 = new Player("Shoot-em-Up/images/nave2_edit.png", false, "Shoot-em-Up/images/projectile_2.png");
        player2.load();

        inicializar();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                alturaVisivel = getHeight();
                larguraVisivel = getWidth();
                player.setFaseDimensoes(alturaVisivel, larguraVisivel);
                player2.setFaseDimensoes(alturaVisivel, larguraVisivel);
            }
        });

        // Inicializa rede
        try {
            network = new ClientNetwork();
            new Thread(this::networkLoop).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        addKeyListener(new Teclado());
        timer = new Timer(10, this);
        timer.start();
        inicializarEstrelas();
    }

    public void inicializar() {
        alturaVisivel = getHeight() > 0 ? getHeight() : 768;
        larguraVisivel = getWidth() > 0 ? getWidth() : 1024;
        player.setFaseDimensoes(alturaVisivel, larguraVisivel);
        player2.setFaseDimensoes(alturaVisivel, larguraVisivel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(fundo, 0, 0, getWidth(), getHeight(), null);

        for (Estrelas s : estrelas) {
            s.load();
            g2.drawImage(s.getImage(), s.getX(), s.getY(), this);
        }

        if (player.isVisible()) {
            g2.drawImage(player.getImagem(), player.getX(), player.getY(), this);
            player.getTiros().forEach(m -> {
                m.load();
                g2.drawImage(m.getImage(), m.getX(), m.getY(), this);
            });
        }

        if (player2.isVisible()) {
            g2.drawImage(player2.getImagem(), player2.getX(), player2.getY(), this);
            player2.getTiros().forEach(m -> {
                m.load();
                g2.drawImage(m.getImage(), m.getX(), m.getY(), this);
            });
        }
    }

    private void inicializarEstrelas() {
        estrelas = new ArrayList<>();
        int alt = getHeight() > 0 ? getHeight() : 768;
        int larg = getWidth() > 0 ? getWidth() : 1024;
        for (int i = 0; i < 5; i++) {
            int y = (int) (Math.random() * alt + 700);
            int x = (int) (Math.random() * larg);
            estrelas.add(new Estrelas(x, y, alt, larg));
        }
    }

    private void checarColisoes(Player p, Player enemy) {
        Rectangle eBounds = enemy.getBounds();
        for (Tiro t : p.getTiros()) {
            if (t.getBounds().intersects(eBounds)) {
                enemy.setHp(enemy.getHp() - 1);
                if (enemy.getHp() <= 0) enemy.setVisible(false);
                t.setVisible(false);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player.update();

        extrapolateRemote( System.currentTimeMillis() );

        estrelas.removeIf(s -> !s.isVisible());
        estrelas.forEach(Estrelas::update);

        player.getTiros().removeIf(t -> !t.isVisible());
        player.getTiros().forEach(Tiro::update);
        player2.getTiros().removeIf(t -> !t.isVisible());
        player2.getTiros().forEach(Tiro::update);

        checarColisoes(player, player2);
        checarColisoes(player2, player);

        try {
            network.sendState(player, lastShot);
            lastShot = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        repaint();
    }

    /**
     * Extrapola a posição do player remoto com base na última velocidade conhecida.
     */
    private void extrapolateRemote(long currentTime) {
        if (lastReceiveTime <= 0) return;
        float dt = (currentTime - lastReceiveTime) / 1000f;
        // Limita extrapolação a 0.5s para evitar grandes saltos
        dt = Math.min(dt, 0.5f);
        int extrapX = (int) (lastReceivedX + velocityX * dt);
        int extrapY = (int) (lastReceivedY + velocityY * dt);

        // Ajusta Y invertido conforme lógica original
        int invertedY = alturaVisivel - extrapY - player2.getAltura();
        player2.setX(extrapX);
        player2.setY(invertedY);
    }

    private void networkLoop() {
        while (true) {
            try {
                String data = network.receiveState();
                String[] p = data.split(";");
                int x = Integer.parseInt(p[0]);
                int receivedY = Integer.parseInt(p[1]);
                boolean shot = Boolean.parseBoolean(p[2]);
                long receiveTime = System.currentTimeMillis();

                // Atualiza velocidades para extrapolação
                if (lastReceiveTime > 0) {
                    float dt = (receiveTime - lastReceiveTime) / 1000f;
                    if (dt > 0) {
                        velocityX = (x - lastReceivedX) / dt;
                        velocityY = (receivedY - lastReceivedY) / dt;
                    }
                }
                lastReceiveTime = receiveTime;
                lastReceivedX = x;
                lastReceivedY = receivedY;

                SwingUtilities.invokeLater(() -> {
                    // Ajusta posição direta ao receber
                    player2.setX(x);
                    int invertedY = alturaVisivel - receivedY - player2.getAltura();
                    player2.setY(invertedY);
                    if (shot) player2.tiro();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class Teclado extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            if (e.getKeyCode() == KeyEvent.VK_F) lastShot = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyRelease(e);
        }
    }
}
