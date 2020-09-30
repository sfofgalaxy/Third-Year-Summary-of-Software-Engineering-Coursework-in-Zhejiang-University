/**
 * 服务端用来创建服务器
 * @author pzf
 * @date 2020-1-14
 * @version 1.0
 */
package server;

import jdbc.ConnMySQL;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.sql.*;

public class server {
    /**
     * 用来启动服务端，启动后提示已启动，等待两个客户端线程来连接。连接完成后正式开始游戏
     */
    public server(int PORT) {
        new Thread(() -> {
            Socket socket1 = null;
            Socket socket2 = null;
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                System.out.println("服务器已启动！\n");
                while (true) {
                    socket1 = serverSocket.accept();
                    System.out.println("玩家1正在匹配！");
                    socket2 = serverSocket.accept();
                    System.out.println("玩家2正在匹配！\n开始对局！");
                    new Thread(new StartGame(socket1, socket2)).start();
                }
            } catch (IOException | SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    socket1.close();
                    socket2.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
/**
 *该类用来进行游戏的开始的启动，其接口为Runnable
 * 进行三轮对战
 */
class StartGame implements Runnable {
    private Socket socket1;
    private Socket socket2;
    private Connection conn;
    Statement stat;
    private short turn = 1;//当前哪个线程在行动
    /**
     * 重写接口内的方法
     */
    @Override
    public void run() {
        try {
            DataInputStream Player1Req = new DataInputStream(socket1.getInputStream());
            DataInputStream Player2Req = new DataInputStream(socket2.getInputStream());
            DataOutputStream Player1Res = new DataOutputStream(socket1.getOutputStream());
            DataOutputStream Player2Res = new DataOutputStream(socket2.getOutputStream());
            //计数猜的次数
            int count = 0;
            //向客户端里发送0，确保已经匹配到对手
            Player1Res.writeInt(0);
            Player2Res.writeInt(0);
            //生成三个随机数向两个客户端发送3个数
            Random ran = new Random();
            int[] numbers = new int[3];
            System.out.print("随机数为:");
            //一共进行三轮
            int ROUND = 3;
            for(int i = 0; i < ROUND; i++)
            {
                numbers[i] = ran.nextInt(100);
                System.out.print(" "+numbers[i]);
                Player1Res.writeInt(numbers[i]);
                Player2Res.writeInt(numbers[i]);
            }
            String InsertSQL = "insert into guessnum(number1,number2,number3) values("
                    +numbers[0]+","+numbers[1]+","+numbers[2]+")";
            stat.execute(InsertSQL);
            System.out.println(" 此轮数字成功加入数据库表中");
            //正式告诉两线程即将开始游戏，1先开始猜数
            Player1Res.writeInt(-1);//-1代表先让1开始
            Player2Res.writeInt(0);
            //用户获取返回结果，从而判断是否猜对以及猜大猜小
            int ReturnValue;
            //开始循环获取结果值
            while(true) {
                count++;
                //如果1在行动那么如下：
                if(turn==1){
                    ReturnValue = Player1Req.readInt();//返回1猜的数字
                    System.out.println("Player1刚刚猜了:" + ReturnValue);
                    //给Player2发消息
                    Player2Res.writeInt(ReturnValue);
                    turn=2;//转移行动权
                }
                else {
                    ReturnValue = Player2Req.readInt();
                    System.out.println("Player2刚刚猜了:" + ReturnValue);
                    Player1Res.writeInt(ReturnValue);
                    turn=1;//转移行动权
                }
                if (count>=100)break;
            }
            Player1Req.close();
            Player2Req.close();
            Player2Res.close();
            Player2Res.close();
            socket1.close();
            socket2.close();
        }
        catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造函数根据两个游戏客户端的socket
     * @param socket1 第一个客户端的socket
     * @param socket2 第二个客户端的socket
     * @throws SQLException
     */
    StartGame(Socket socket1, Socket socket2) throws SQLException {
        conn = new ConnMySQL().getConnect();
        stat = conn.createStatement();
        String checkTable="show tables like \"guessnum\"";//用于检测是否存在guessnum的语句
        String CreateTableSQL = "create table GuessNum(gamenum int primary key auto_increment, number1 int, number2 int, number3 int)";

        //SQL查询语句,表不存在则创建表guessnum
        ResultSet resultSet=stat.executeQuery(checkTable);
        if (!resultSet.next()) {
            if(stat.executeUpdate(CreateTableSQL)==0)
                System.out.println("成功创建表:guessnum");
        }
        this.socket1 = socket1;
        this.socket2 = socket2;
    }

}