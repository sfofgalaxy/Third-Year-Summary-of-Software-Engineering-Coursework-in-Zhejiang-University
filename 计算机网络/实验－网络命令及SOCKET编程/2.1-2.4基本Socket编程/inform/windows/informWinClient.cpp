/*
 *	本程序的主要目的在于说明socket编程的基本过程，所以服务器/客户端的交互过程非常简单，
 *  只是由客户端向服务器传送一个学生信息的结构。
 */
//informWinClient.cpp：参数为serverIP name age
#include <stdio.h>
#include <winsock2.h>
#include <Windows.h>

#define SERVER_PORT	5860 //侦听端口

enum OPTION{
	CONNECT,DISCONNECT,GET_TIME,GET_NAME,GET_CLIENTS,SEND_MESSAGE,EXIT;
};

//客户端向服务器传送的结构：
struct student
{
	char name[32];
	int age;
};

void main()
{
	WORD wVersionRequested;
	WSADATA wsaData;
	int ret;
	SOCKET sClient; //连接套接字
	struct sockaddr_in saServer;//地址信息
	struct student stu;
	char *ptr = (char *)&stu;
	BOOL fSuccess = TRUE;
	char server_IP[16];
	BOOL isconnected = FALSE;
	enum OPTION option;
	int flag;

	 
/*	if(argc != 4)
	{
		printf("usage: informWinClient serverIP name age\n");
		return;
	}*/
	
	//WinSock初始化：
	wVersionRequested = MAKEWORD(2, 2);//希望使用的WinSock DLL的版本
	ret = WSAStartup( wVersionRequested, &wsaData );
	if (ret != 0)
	{
		printf("WSAStartup() failed!\n");
		return;
	}
	//确认WinSock DLL支持版本2.2：
	if (LOBYTE(wsaData.wVersion) != 2 || HIBYTE( wsaData.wVersion ) != 2 )
	{
		WSACleanup();
		printf("Invalid Winsock version!\n");
		return;
	}

	//创建socket，使用TCP协议：
	sClient = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if(sClient == INVALID_SOCKET)
	{
		WSACleanup();
		printf("socket() failed!\n");
		return;
	}
	
	while(1){
		printf("Please choose the option:\n");
		if(isconnected == FALSE){
			printf("1.connect the server\n2.exit\n");
			scanf("%d",&flag);
			if(flag==1){
				printf("Please input the server_IP:\n");
				scanf("%s",server_IP);
					//构建服务器地址信息：
				saServer.sin_family = AF_INET;//地址家族
				saServer.sin_port = htons(SERVER_PORT);//注意转化为网络字节序
				saServer.sin_addr.S_un.S_addr = inet_addr(server_IP);
			}
			else if(flag==2){
				return;
			}
		}
		else{
			printf("1.disconnect\n2.get the time\n3.get the name\n");
			printf("4.get the clients list\n5.sned message\n6.exit\n");
			scanf("%d",&option);	
			switch(option){
				case DISCONNET:
					break;
				case GET_TIME:
					break;
				case GET_NAME:
					break;
				case GET_CLIENTS:
					break;
				case SEND_MESSAGE:
					break;
				case EXIT:
					return;
				default:
					break;
			}
		}
		
	}
	
	

	//saServer.sin_addr.S_un.S_addr = inet_addr(argv[1]);


	//连接服务器：
	ret = connect(sClient, (struct sockaddr *)&saServer, sizeof(saServer));
	if (ret == SOCKET_ERROR)
	{
		printf("connect() failed!\n");
		closesocket(sClient);//关闭套接字
		WSACleanup();
		return;
	}

	//按照预定协议，客户端将发送一个学生的信息：
	strcpy(stu.name, argv[2]);
	stu.age = atoi(argv[3]);
	ret = send(sClient, (char *)&stu, sizeof(stu), 0);
	if(ret == SOCKET_ERROR)
	{
		printf("send() failed!\n");
	}
	else
		printf("student info has been sent!\n");

	closesocket(sClient);//关闭套接字
	WSACleanup();
}
