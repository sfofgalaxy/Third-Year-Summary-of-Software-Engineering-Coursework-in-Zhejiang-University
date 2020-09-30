/**
 * 用于启动游戏后进行处理
 */
package client;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class StartPlaying extends JFrame implements ActionListener{
    //两玩家对战三轮,所猜数字为2位数
    private final int ROUND=3;
    private DataInputStream receive;
    private DataOutputStream send;
    //定义框架、面板、按钮、标签
    private JButton[] NumberCharButton;
    private JPanel panel;
    private JLabel[] ScorcesLabel = {new JLabel("0"), new JLabel("0")};//分数的标签
    private JTextArea GuessNumLog = new JTextArea("猜数记录: \n");

    //定义猜的次数、字符
    private int GuessCount;
    //记录个人数据
    private int MyGuessNum=0;//初始我的猜测数
    private int[] numbers = new int[ROUND];//三轮的数字
    private int CurrentRound = 0;//记录当前第几轮
    private boolean myturn=false;//当前是否轮到我
    private int[] scores = new int[2];//二人的分数
    private char[] answer;//存放答案的字符
    private int turn=0;//用于记录第几次点数字，一次能点两个数字，因此偶数次点完数字应该进行计算两位数

    StartPlaying(Socket socket) {
        //初始化开始
        //创建输入输出流
        //定义套接字和输入输出流
        try {
            this.receive = new DataInputStream(socket.getInputStream());
            this.send = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e) {
           e.printStackTrace();
        }

        //创建数字的button
        NumberCharButton=new JButton[10];
        JFrame frame = new JFrame("Playing...");
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon img = new ImageIcon("./src/main/resources/playing.jfif");
                img.paintIcon(this, g, 340, 165);
            }
        };
        //设置button的位置
        panel.setPreferredSize(new Dimension(1020, 725));
        GuessCount =0;

        //设置整个框架大小和其他基本参数
        frame.setLayout(new BorderLayout());
        frame.setSize(1324, 726);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.add(panel, BorderLayout.CENTER); //加入框架
        panel.setLayout(null);

        JPanel p2 = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon img = new ImageIcon("./src/main/resources/GuessNumLog.png");
                img.paintIcon(this, g, 0, 0);
            }
        };
        p2.setPreferredSize(new Dimension(300, 726));
        p2.setLayout(null);
        //初始化计分板
        frame.add(p2, BorderLayout.EAST);
        ScorcesLabel[0].setBounds(85,30,50,40);
        ScorcesLabel[0].setFont(new Font("",Font.BOLD,30));
        ScorcesLabel[0].setForeground(Color.RED);
        ScorcesLabel[1].setBounds(175,30,50,40);
        ScorcesLabel[1].setFont(new Font("",Font.BOLD,30));
        ScorcesLabel[1].setForeground(Color.RED);
        //两个玩家的名字
        JLabel[] players = {new JLabel("you    :"), new JLabel("opponent")};
        players[0].setBounds(75,5,60,40);
        players[0].setFont(new Font("",Font.BOLD,15));
        players[0].setForeground(Color.BLACK);
        players[1].setBounds(155,5,75,40);
        players[1].setFont(new Font("",Font.BOLD,15));
        players[1].setForeground(Color.BLACK);
        p2.add(ScorcesLabel[0]);
        p2.add(ScorcesLabel[1]);
        p2.add(players[0]);
        p2.add(players[1]);
        GuessNumLog.setBounds(50, 80, 500, 700);
        GuessNumLog.setFont(new Font("",Font.BOLD,15));
        GuessNumLog.setForeground(Color.BLACK);
        GuessNumLog.setOpaque(false);
        GuessNumLog.setEditable(false);
        p2.add(GuessNumLog);
        //初始化按钮的位置以及基本参数
        for(int i = 0; i < 10; i++){
            String[] numberChars = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
            NumberCharButton[i]=new JButton(numberChars[i]);
            NumberCharButton[i].setEnabled(false);
            NumberCharButton[i].setFont(new Font("Consolas",Font.BOLD,20));
            NumberCharButton[i].setForeground(Color.DARK_GRAY);
            NumberCharButton[i].setBackground(Color.lightGray);
            NumberCharButton[i].addActionListener(this);
        }
        //将一个个BUTTON加入到panel中
        //一共10个button，从0-9
        //每个玩家点击两次，代表两位数字
        for(int i = 0; i < 3; i++){
            NumberCharButton[i].setBounds(500+100*i, 220, 100, 50);
            panel.add(NumberCharButton[i]);
        }
        for(int i = 3;i < 6; i++){
            NumberCharButton[i].setBounds(500+100*(i-3), 320, 100, 50);
            panel.add(NumberCharButton[i]);
        }
        for(int i = 6; i < 10; i++){
            NumberCharButton[i].setBounds(450+100*(i-6), 420, 100, 50);
            panel.add(NumberCharButton[i]);
        }
        //初始化界面结束
        //开始游戏开始前服务器和客户端的准备
        //获取服务器随机到的三轮的数字
        try {
            for(int i = 0; i < ROUND; i++) {
                numbers[i] = receive.readInt();
            }
        }
        catch (IOException no_word) {
            no_word.printStackTrace();
        }

        //初始化两个标签用于显示用户点击的数字
        answer = String.valueOf(numbers[CurrentRound]).toCharArray();
        frame.repaint();

        try {
            //判断谁是第一个
            int GuessNum = receive.readInt();
            //如果收取结果不正确或者为-1则轮到我了修改myturn=1
            //由于服务端初始化时确定第一个游戏时发送-1，为0时不是第一个游戏
            if(GuessNum==0){
                myturn = false;
            }
            else if (GuessNum==-1) {
                myturn = true;
                enable_btn(myturn);
            }
            //正式开始游戏，获取服务器传来的另一个游戏线程的消息
            while (true){
                //获取刚刚另一个线程猜的结果通过服务端发来的结果
                GuessNum = receive.readInt();
                //如果不相等则轮到我进行猜测
                if (GuessNum!=numbers[CurrentRound]) {
                    myturn = true;//将我的回合改成true
                    enable_btn(myturn);
                }
                CheckAnswerOfOpponent(GuessNum);
            }
        }
        catch (IOException conn) {
            conn.printStackTrace();
        }
    }

    /**
     * 用来判断你是否猜对并且加入自己的猜测到记录中
     * 若猜对则获得分数并开始下一局
     * @param event 事件、这里就是鼠标点击
     */
    public void actionPerformed(ActionEvent event) {
        if(turn%2==0)MyGuessNum=0;
        //侦测是否10个按钮有事件
        //其中turn是猜测两位数的长度，即两位后break退出
        for(int i = 0;i < 10;i++) {
                if (NumberCharButton[i] == event.getSource()) {
                    MyGuessNum = MyGuessNum * 10 + i;
                    turn++;
                    //如果大于两轮退出
                    break;
                }
        }
        //如果是第一次那么直接退出，第二次则继续进行
        if(turn%2==1)return;
        turn=0;
        //向服务端发送刚刚猜的数字
        try {
            send.writeInt(MyGuessNum);
            GuessNumLog.append("\n" + "你猜测: " + MyGuessNum);
            System.out.println("发送刚刚猜的数字："+MyGuessNum);

            //如果猜对了
            if(MyGuessNum==numbers[CurrentRound]) {
                myturn=false;
                enable_btn(myturn);
                //增加分数，显示计分
                scores[0]++;
                ScorcesLabel[0].setText(""+scores[0]);
                //如果是前两句则进行下一回合，否则直接结束游戏
                if(CurrentRound<ROUND-1) {
                    new Thread(() -> {
                        JOptionPane.showMessageDialog(this, "恭喜你猜对了！开始下一回合吧！");
                        dispose();
                    }).start();
                    NextRound();
                } else {
                    EndGame();
                }
            } else {
                //如果猜错猜的计数增加，并关闭自己的button
                GuessCount++;
                myturn=false;
                enable_btn(myturn);
                if(MyGuessNum>numbers[CurrentRound]) GuessNumLog.append(" 猜大了");
                else GuessNumLog.append(" 猜小了");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //如果正确就开始下一轮或结束比赛触发EndGame

    }

    /**
     *用来判断对手是否猜测的正确，并写入记录
     * 如果正确结束并加分
     * @param GuessNum 对手猜的数字
     */
    public void CheckAnswerOfOpponent(int GuessNum) {
        //判断对手是否正确，并加入记录
        GuessNumLog.append("\n" + "对手猜测 " + GuessNum);
        if(GuessNum<numbers[CurrentRound]){
            GuessCount++;
            GuessNumLog.append(" 猜小了");
        }
        else if(GuessNum>numbers[CurrentRound]){
            GuessCount++;
            GuessNumLog.append(" 猜大了");
        }
        else{
            scores[1] ++;
            ScorcesLabel[1].setText(""+scores[1]);
            GuessNumLog.append(" 猜测了"+GuessCount+",猜对了");
            myturn = true;
            enable_btn(myturn);
            if(CurrentRound<2) {
                new Thread(() -> {
                    JOptionPane.showMessageDialog(this, "太遗憾了！你对手赢了，再来一局！");
                    dispose();
                }).start();
                NextRound();
            } else {
                EndGame();
            }
        }
    }

    /**
     * 用来将所有按钮enable或者disable,取决于参数
     * @param my_turn 为true时enable，为false则disable
     */
    public void enable_btn(boolean my_turn) {
        if (my_turn) {//开启按钮
            for (int i = 0; i <10; i++) {
                NumberCharButton[i].setEnabled(true);
                NumberCharButton[i].setForeground(Color.PINK);
                NumberCharButton[i].setContentAreaFilled(false);
            }
            panel.repaint();
        }
        else {
            for (JButton btn : NumberCharButton) {
                btn.setEnabled(false);
                btn.setForeground(Color.DARK_GRAY);
                btn.setBackground(Color.lightGray);
            }
        }
    }

    /**
     * 下一回合，刷新所有内容
     */
    public void NextRound() {
        CurrentRound++;
        GuessCount = 0;
        answer = String.valueOf(numbers[CurrentRound]).toCharArray();
        GuessNumLog.setText("记录:\n");
        return;
    }

    /**
     * 结束游戏，判断你和对手分数谁多
     */
    public void EndGame() {
        if(scores[0]<scores[1]) {
            new Thread(()-> {
                JOptionPane.showMessageDialog(this, "你输了，再来一局！");
                dispose();
                System.exit(0);
            }).start();
        } else if(scores[0]>scores[1]) {
            new Thread(()-> {
                JOptionPane.showMessageDialog(this, "恭喜你赢了，再来一局！");
                dispose();
                System.exit(0);
            }).start();
        } else {
            new Thread(()-> {
                JOptionPane.showMessageDialog(this, "平局！");
                dispose();
                System.exit(0);
            }).start();
        }
    }


}

