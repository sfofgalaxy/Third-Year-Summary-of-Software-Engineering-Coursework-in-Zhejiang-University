/**
 * 客户端线程
 * Init中调用两次，实例化两个客户端线程进行运行
 */
package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class clientThread extends JFrame implements ActionListener {
    private JFrame frame = new JFrame("Guess Number!");
    private JButton Button1 = new JButton("Start Playing!");
    private JButton Button2 = new JButton("*Rules*");
    private JButton Button3 = new JButton("Exit >_<");
    public clientThread(){
        frame.setSize(500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        //设置label的位置、大小，label大小为图片的大小
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon img = new ImageIcon("./src/main/resources/client.jpg");
                img.paintIcon(this, g, 115, 0);
            }
        };
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
        panel.setLayout(null);

        //设置三个按钮
        Button1.setBounds(100,275,300,80);
        Button1.setContentAreaFilled(false);
        Button1.setFont(new Font("Consolas",Font.BOLD,15));
        panel.add(Button1);
        Button2.setBounds(150,400,200,50);
        Button2.setFont(new Font("Consolas",Font.BOLD,20));
        Button2.setContentAreaFilled(false);
        panel.add(Button2);
        Button3.setBounds(150,550,200,50);
        Button3.setFont(new Font("Consolas",Font.BOLD,20));
        Button3.setContentAreaFilled(false);
        panel.add(Button3);
        Button1.addActionListener(this);
        Button2.addActionListener(this);
        Button3.addActionListener(this);
    }

    /**
     * 点击事件，可以点击匹配、规则和退出
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== Button1){
            Button1.setEnabled(false);
            Button1.setText("Waiting for opponent to start...");
            Button1.setForeground(Color.RED);
            Button1.setContentAreaFilled(true);
            Button1.setBackground(Color.YELLOW);
            frame.repaint();

            try {
                Socket connection = new Socket("127.0.0.1", 5860);
                new Thread(()-> {
                    try {
                        DataInputStream signal = new DataInputStream(connection.getInputStream());
                        int match = signal.readInt();
                        if (match == 0) {
                            new StartPlaying(connection);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();


            }
            catch (IOException E) {
                System.out.println("无法连接");
            }

        }
        if(e.getSource()== Button2) {
            String title = new String("规则");
            JOptionPane.showMessageDialog(this,
                    "点击start开始匹配，若第二个客户端连入，则匹配成功开始进行游戏。" +
                            "两人轮流对一个0-99随机数进行猜数，会给出提示是否正确、偏大、偏小，直至胜利。");
        }
        if(e.getSource()== Button3){
            System.exit(0);
        }
    }
}
