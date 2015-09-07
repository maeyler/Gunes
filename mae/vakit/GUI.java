package mae.vakit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class GUI implements Runnable {

    static final String 
        MSG = "Günümüz böyle geçiyor", FILE = "Vakit.txt",
        TITLE = " için güneþ saati -- ", VER = "V2.0",
        TIP = "<HTML>Mavi: sabah-akþam <BR>Sarý: öðle-ikindi "
             +"<BR>Siyah: yatsý <BR>Kýrmýzý: kerahat";
    static final Toolkit TK = Toolkit.getDefaultToolkit();
    static final boolean 
        WIDE = TK.getScreenResolution()>100 || TK.getScreenSize().width>1600;
    static final int
        K = WIDE? 1 : 2,
        W = 720/K,  //half width
        GAP = WIDE? 15 : 10,
        M = WIDE? 135 : 90, //used in cosine curve
        H1 = WIDE? 300 : 200, 
        H2 = WIDE? 45 : 30, 
        DELTA = WIDE? 12 : 8, 
        SIZE = WIDE? 18 : 12;
    static final Font 
        LARGE = new Font("Dialog", 0, 2*SIZE),
        NORM  = new Font("Dialog", 0, SIZE),
        MONO  = new Font("Monospaced", 1, SIZE),
        BOLD  = new Font("Dialog", 1, SIZE);
    static final Color 
        BLACK = new Color(0, 0, 0), 
        BLUE  = new Color(130, 175, 255), //145, 190, 240),
        RED   = new Color(255, 0, 0), 
        NOON  = new Color(255, 250, 120), //yellow
        DARK  = new Color(235, 215, 0);  //dark yellow
    static final int[] curve = new int[W];
    static final String[] d2s = new String[24];
    static { 
        UIManager.put("ToolTip.font", NORM); 
        // String[] used in drawClock()
        for (int i=0; i<10; i++) d2s[i] = "0"+i;
        for (int i=10; i<24; i++) d2s[i] = ""+i;
        // cosine curve used in drawCurve()
        for (int d=0; d<W; d++)
           curve[d] = H1 - (int)Math.round(M*Math.cos(Math.PI*d/W)); 
        // for Java 6 
        java.util.TimeZone.setDefault(Location.DEFAULT.zone);
    }

    final JFrame frm = new JFrame(TITLE+VER);
    final Panel pan = new Panel();
    final JLabel lab = new JLabel(" ");  //shows time
    JComboBox menuD;  //<String> menuD;
    JComboBox menuL;  //<Location> menuL;
    JComboBox menuM;  //<Method> menuM;
    final JButton but1 = new JButton();   //toggle run/stop
    final JButton but2 = new JButton();   //toggle fast/pause
    final Color[] col = new Color[W];
    final int[] shade = new int[W];
    final Vakit V = new Vakit(this);
    Thread thd;
    int state = 1;  // s<0 -> fast,  s=0 -> clock,  s>0 count down
    int x;  // pixels since noon  -W<x<W  -- K*x in minutes
    String alfa; // altitude -- uses C0, C1
    
    public GUI() {
        //V.trace = true;
        Ear ear = new Ear();
        JPanel cp = new JPanel(new BorderLayout(GAP/2, GAP/2));
        cp.setBorder(new EmptyBorder(GAP, GAP, GAP/2, GAP)); 
        pan.setPreferredSize(new Dimension(2*W, H1+H2+DELTA+1));
        pan.addMouseListener(ear);
        cp.add(pan, "Center"); 
        
        lab.setFont(LARGE);
        //lab.setToolTipText("Gerçek saat");
        but1.setFont(NORM);
        but1.setToolTipText("Saat: Baþla/Dur -- (ESC)");
        but1.addActionListener(ear);
        but1.addKeyListener(ear);
        but2.setFont(MONO);
        but2.setToolTipText("Hýzlý Hareket -- (nokta)");
        but2.addActionListener(ear);
        but2.addKeyListener(ear);
        
        Dimension dim = new Dimension(12*GAP, 2*GAP);
        Reader rdr = new Reader(FILE);
        // read days
        if (rdr.dayA!=null && rdr.dayA.length>1) {
            menuD = new JComboBox(rdr.dayA);  //<String>(rdr.dayA);
            menuD.setFont(BOLD);
            //menuD.setEditable(true);
            menuD.addActionListener(ear);
            menuD.setMaximumSize(dim);
            menuD.addKeyListener(ear);
            menuD.setToolTipText("GÜN seçimi");
        }
        // read locations
        if (rdr.locA!=null && rdr.locA.length>1) {
            menuL = new JComboBox(rdr.locA);  //<Location>(rdr.locA);
            menuL.setFont(BOLD);
            menuL.addActionListener(ear);
            menuL.setMaximumSize(dim);
            menuL.addKeyListener(ear);
            menuL.setToolTipText("YER seçimi");
            V.setLocation((Location)menuL.getItemAt(0));
        } 
        // read methods
        if (rdr.metA!=null && rdr.metA.length>1) {
            menuM = new JComboBox(rdr.metA);  //<Method>(rdr.metA);
            menuM.setFont(BOLD);
            menuM.addActionListener(ear);
            menuM.setMaximumSize(dim);
            menuM.addKeyListener(ear);
            menuM.setToolTipText("YÖNTEM seçimi");
            V.setMethod((Method)menuM.getItemAt(0));
        }
        
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(Box.createHorizontalStrut(GAP));
        p2.add(lab);
        p2.add(Box.createHorizontalGlue());
        if (menuD!=null) p2.add(menuD);
        p2.add(Box.createHorizontalStrut(GAP));
        if (menuL!=null) p2.add(menuL);
        else V.setLocation(Location.DEFAULT);
        p2.add(Box.createHorizontalStrut(GAP));
        if (menuM!=null) p2.add(menuM);
        else V.setMethod(Method.DEFAULT);
        p2.add(Box.createHorizontalStrut(M/2));
        p2.add(but1);
        p2.add(Box.createHorizontalStrut(GAP));
        p2.add(but2);
        p2.add(Box.createHorizontalStrut(GAP));
        p2.setToolTipText(TIP);
        cp.add(p2, "South"); 

        frm.setContentPane(cp);
        frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frm.setLocation(263, 0);  frm.setResizable(false);
        frm.addWindowListener(ear);
        
        doTitle(); setState(0); setCurrentTime();  // initialize data
        //lab.setText(new File(FILE).exists()+" "+System.getProperty("user.dir"));
        frm.pack(); frm.setVisible(true); run();
    }
    void fillColor(int x2, Color c) {
        for (; x<x2; x++) col[x] = c;
    }
    void graded(int x2, Color c1, Color c2) {
        float[] f1 = {0,0,0,0}; c1.getComponents(f1);
        float[] f2 = {0,0,0,0}; c2.getComponents(f2);
        float[] d = {0,0,0,0}; 
        int x1 = x;
        for (int k=0; k<3; k++)
            d[k] = (f2[k] - f1[k])/(x2 - x1);
        for (; x<x2; x++) {
            float[] c = {0,0,0}; 
            for (int k=0; k<3; k++)
                c[k] = f1[k] + d[k]*(x - x1);
            col[x] = new Color(c[0], c[1], c[2]);
        }   
    }
    void doColors() {
        x = 0;  // field used in calculation
        fillColor(2+V.ogle()/K, RED);
        fillColor(V.ikindi()/K, NOON);
        graded((V.ikindi()+80)/K, NOON, DARK);
        fillColor((V.sunset()-40)/K, DARK);
        graded(V.sunset()/K, DARK, RED);
        fillColor(V.aksam()/K, RED);
        graded(-V.imsak()/K, BLUE, Color.BLACK); //yatsi()
        //System.out.printf("%s: %s %s\n", VER, V.aksam(), -V.imsak());
        fillColor(W, Color.BLACK);
        shadowLength(); setMinute(0);  // noon
    }
    void shadowLength() {
        for (int i=0; i<W; i++) {
            float a = V.altitude(K*i);
            int s = Math.round(H2/V.tan(a));
            shade[i] = (s<0 || s>W ? W : s); // if too large, clip
        }
    }
    void doTitle() {
        frm.setTitle(V.loc+TITLE+VER);
    }
    public void setDate(String s) { 
        SimpleDateFormat DATE = new SimpleDateFormat("dd/MM/yy");
        DATE.setTimeZone(V.loc.zone);
        try {
            V.setDate(DATE.parse(s));
            setState(120); 
        } catch (ParseException ex) {
            setCurrentTime();
        }
    }
    public void setMinute(int m) {
        x = m/K;  // pixels
        if (x < -W) x += 2*W;
        if (x >= W) x -= 2*W;
        long t1 = Math.round((V.noonM + m + 0.5)*60);
        V.date.setTime(t1*1000);
        alfa = Math.round(V.altitude(m))+"°";
        pan.repaint(); 
    }
    public void setCurrentTime() {
        long t = System.currentTimeMillis();
        double d = Timer.toJulian(t);
        if (V.day != (int)d) {
            System.out.printf("setCurrentTime: %s %s %n", V.day, d);
            V.setDate(d, true); //V.report(); 
        }
        int m = (int)Math.round(t/60/1000 - V.noonM);
        setMinute(m); 
        if (menuD.getSelectedIndex() > 0)
            menuD.setSelectedIndex(0);
    }
    public void setState(int s) {
        if (state == s) return;
        state = s;
        but1.setText(s == 0? "\u25A0" : "\u25BA");
        but2.setText(s < 0? "||" : ">>");
        lab.setForeground(s == 0? Color.BLACK : Color.RED);
        if (s == 0) setCurrentTime();
        if (thd != null) thd.interrupt();
    }
    public void stop() {
        thd = null;
    }
    public void run() {  // Model
        if (thd == null) {
            thd = new Thread(this); thd.start(); return;
        }
        if (thd != Thread.currentThread())
            throw new RuntimeException("Already running");
        System.out.println("Start GUI"); int c = 4;
        String last = "00:00"; // last hour:minute written on screen
        while (thd != null) try {
            if (state == -1 /*fast*/) {
                if (x < W-3) setMinute(K*x+2); 
                else setState(0);
                lab.setText(V.date.HHmm()); 
            } else if (state == 1) {
                setState(0);
            } else if (state > 0) {
                state--; 
                if (state%4 == 3) { // every second
                    String t = ((state/4%2) == 0? ". " : " .");
                    lab.setText(V.date.HHmm()+t); //motion
                }
            } else if (state == 0) { // clock mode
                c--; if (c > 0) continue; // every second
                c = 4;
                V.date.setTime(System.currentTimeMillis());
                String s = V.date.HHmmss(); 
                lab.setText(s); //show seconds
                String hhmm = s.substring(0, 5);
                if (!hhmm.equals(last)) {
                    setCurrentTime();
                    last = hhmm;
                    //System.out.println("setCurrentTime "+last);
                }
            }
            Thread.sleep(250); // 1/4 sec
        } catch (InterruptedException e) {
            c = 1; //System.out.println("run: "+e);
        }
    }
    public String toString() {
        return V.toString();
    }
    
    class Panel extends JPanel {  // View
        void line(Graphics g, int i, Color c) {
            g.setColor(c);
            g.drawLine(W+i, H1, W+i, H1+H2);
            g.drawLine(W-i, H1, W-i, H1+H2);
        }
        void drawTime(Graphics g) {
            g.setColor(Color.BLACK); g.setFont(NORM);
            int y = 60/K;
            if (state != 0 /*fast*/) g.drawString(MSG, 600/K, y);
            int[] a = {W+x-DELTA, W+x, W+x+DELTA};
            int[] b = {H1+H2+DELTA+1, H1+H2+1, H1+H2+DELTA+1};
            g.fillPolygon(a, b, 3);
            g.setColor(Color.YELLOW); g.setFont(LARGE);
            g.drawString(V.date.ddMMyyyy(), GAP, y); 
            g.drawString(V.loc.toString(), GAP, 2*y);
            g.drawString(alfa, 2*W-90/K, y);
            for (int i=0; i<W; i++)  // draw colors
                line(g, i, col[i]);
            g.drawLine(0, H1, 0, H1+H2); // missing line at the left
        }
        void drawCurve(Graphics g) {
            int down = curve[(int)V.sunset/K+DELTA] - DELTA - H1; //
            g.setColor(Color.GRAY);
            int x1 = 0; int y1 = curve[x1] - down;
            while (x1 < W) {
                int x2 = x1+8; int y2 = curve[x2] - down;
                g.drawLine(W+x1, y1, W+x2, y2);
                g.drawLine(W-x1, y1, W-x2, y2);
                if (y2 > H1) break;
                x1 = x2; y1 = y2;
            }
            drawClock(g, false, down); 
            int y = curve[Math.abs(x)] - down;
            //System.out.printf("x=%s y=%s \n", x, y);
            g.setColor(NOON); 
            g.fillOval(W+x-DELTA/2, y-DELTA/2, DELTA, DELTA);
        }
        void drawClock(Graphics g, boolean night, int down) {
            if (night) g.setColor(Color.WHITE); 
            else g.setColor(Color.BLACK);
            g.setFont(NORM);
            int min = V.twelve - 12*60; //12 hours
            int c = 0; // start at 12am
            for (int i=min; i<-min; i+=60) {
                int x = W + i/K;
                int y = H1 - 4/K;
                if (!night) {
                    int k = Math.min(W-1, Math.abs(i/K));
                    y = (curve[k] - down);
                    if (y >= H1) y = H1 - 4/K;
                }
                g.drawString(d2s[c%24], x-12/K, y-10/K);
                g.fillRect(x-1, y-1, 4/K, 4/K); 
                c++; //if (c==12) System.out.printf("x=%s y=%s \n", x, y);
            }
            //System.out.printf("min=%s count=%s \n", min, c);
        }
        void drawShadow(Graphics g) {
            int z = V.ogle()/K;
            int d = (x>z? -1 : 1);  // direction of the shadow
            int x1 = W+1 + d*z;
            int x2 = x1+ d*shade[Math.abs(x)];
            int y = H1+H2;
            g.drawLine(x1, y, x2, y); 
            g.drawLine(x1, y-1, x2, y-1); 
        }
        protected void paintComponent(Graphics g) {
            //g.setColor(pan.getBackground());
            g.clearRect(0, 0, 2*W+GAP, H1+2*H2);
            if (Math.abs(K*x) < V.aksam()) { // day
                g.setColor(BLUE);
                g.fillRect(0, 0, 2*W, H1);
        try { // protection against unexpected errors
                drawCurve(g); drawTime(g); drawShadow(g);  
        } catch (Exception e) {
            System.out.printf("paint: %s \n", e);
            e.printStackTrace();
        }
            } else { // night
                Color c = col[Math.abs(x)];
                //System.out.printf("%s: x=%s %s\n", VER, x, c);
                g.setColor(c);
                g.fillRect(0, 0, 2*W, H1);
                drawClock(g, true, 0);
                drawTime(g); 
            }
            //System.out.printf("paintComponent \n");
        }
    }

    class Ear extends WindowAdapter  // Control
        implements ActionListener, MouseListener, KeyListener {
        void resume(int m) {
                if (state == 0) 
                    setCurrentTime();
                else {
                    setState(120); setMinute(m); 
                }
        }
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            int m = K*x;
            if (src == but1) { // toggle
                setState(state == 0? 120 : 0);
            } else if (src == but2) {
                setState(state < 0? 120 : -1);
            } else if (src == menuD) {
                int j = menuD.getSelectedIndex();
                setDate((String)menuD.getItemAt(j)); 
                setMinute(m); 
            } else if (src == menuL) {
                int j = menuL.getSelectedIndex();
                Location a = (Location)menuL.getItemAt(j);
                System.out.println(a+"  "+V.meth);
                V.setLocation(a); resume(m);
            } else if (src == menuM) {
                int j = menuM.getSelectedIndex();
                Method d = (Method)menuM.getItemAt(j);
                System.out.println(V.loc+"  "+d);
                V.setMethod(d); resume(m);
            } else {
                System.out.println(src.getClass());
            }
        }
        public void keyTyped(KeyEvent e) { 
            char c = e.getKeyChar();
            if (c == KeyEvent.VK_ESCAPE)  
                but1.doClick();
            else if (c == KeyEvent.VK_PERIOD) 
                but2.doClick();
        }
        public void keyPressed(KeyEvent e) { 
            if (state <= 0) return;
            int m = K*x; // works only in Pause mode
            int c = e.getKeyCode();
            if (c == KeyEvent.VK_LEFT) m -= K;
            else if (c == KeyEvent.VK_RIGHT) m += K;
            else return;
            setMinute(m); setState(120); 
        }
        public void keyReleased(KeyEvent e) { }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) {
            setMinute(K*(e.getPoint().x - W));
            if ((e.getModifiers()&e.SHIFT_MASK) > 0) 
                setState(0);
            else if (state >= 0) 
                setState(120);
        }
        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void windowClosed(WindowEvent e) {
            stop(); //the thread
        }
    }   

    public static void main(String[] args) {
        //Vakitler.main(); 
        new GUI(); 
    }
}
