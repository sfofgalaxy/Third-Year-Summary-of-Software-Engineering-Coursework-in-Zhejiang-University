/**
 * 用于启动服务器和两个游戏线程
 */
import client.clientThread;
import server.server;
public class Init {
    final static int PORT = 5860;

    /**
     * 用于启动服务器和两个游戏线程
     * @param argv
     */
    public static void main(String[] argv) {
        //第一个构造函数中服务器中有调用jdbc，如果需要调用需创建数据库且更改端口、账户、密码、数据库名，需要导入jar包
        //如不需要则注释掉server中的数据库部分
        new server(PORT);

        clientThread c1 = new clientThread();
        clientThread c2 = new clientThread();
    }
}
