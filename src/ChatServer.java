import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JOptionPane;
 
public class ChatServer{
	
	static ConnectFile connectFiles;	
    static HashMap<String, Socket> connectedUser = new HashMap<String, Socket>();//хранилище, имя хоста и соответсвующий ему сокет
    static Socket ClientSocket = null; //клиентский сокет для сообщений
    private static ServerSocket serverSocket;	//создаем сокет сервера для сообщений
    private static String username = null;      //имя хоста
    public static String userFile = null;	//имя хоста которому будтп ередан файл
    private static PrintWriter output;
    
    //константы ключевых слов
    private static final String PUBLICMESSAGE = "PUBLICMESSAGE";
    private static final String PRIVATE = "PRIVATE";
    private static final String IPRIVATE = "IPRIVATE";
    private static final String ONLINE = "ONLINE";
    private static final String OFFLINE = "OFFLINE";
    private static final String HOST = "192.168.1.1";
 
    //создает скелет сервера RMI для возможности подключение клиентов
    public ChatServer(){
        try{
        	ChatImpl csi = new ChatImpl(this);
            Naming.rebind("rmi://"+"192.168.1.1"+":1099/ChatService", csi);//привязка имени к обьекту, создание скелета
        }
        catch(java.rmi.ConnectException ce)
        {
            JOptionPane.showMessageDialog(null, "Trouble : Please run rmiregistry.", "Connect Exception", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        catch (IOException ioe)
        {
            JOptionPane.showMessageDialog(null, "Trouble : Please run rmiregistry.", "Exception", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Trouble : Please run rmiregistry.", "Exception", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
 
    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    
    //Запускает создание скелета сервера RMI
    //Создает поток для работы с файлами
    //Вызывает processConnection для приема клиентов
    public static void main (String[] args) throws IOException{
    	 connectFiles = new ConnectFile();		//создаем обьект класса для приема файлов
    	 connectFiles.start();					//и второй поток для него
    	 try{
    		 ChatServer cs = new ChatServer();
    		 cs.processConnection(4444);
    	 }catch (ArrayIndexOutOfBoundsException ae){
    		 JOptionPane.showMessageDialog(null, "Please insert the port", "ATTENTION", JOptionPane.INFORMATION_MESSAGE);
    		 System.exit(-1);
    	 }
    }
 
    //Создает серверный сокет для сообщений
    //В цикле ожидает подключения новых клиентов
    private void processConnection(int port){
    	try{
    		serverSocket = new ServerSocket(port);		//создаем серверный сокет
    		System.out.println("Server is running on port " + port + " .... ");
    	}catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "Could not listen port " + port, "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
 
    	try{
    		while (true){
    			addClient(serverSocket.accept());	//срвер ждет клиентов
    			String username = getUsername();	
                sendPublicMessage(PUBLICMESSAGE, "SERVER", "[" + username + "] is now online");//сообщение в чат о новом клиенте
            }
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "Could not accept connection.", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
 
    //заполняем переменную username
    public void connect(String username){
        this.username = username;
    }
 
    //возвращаем переменную username
    public String getUsername(){
        return username;
    }
 
    //возвращает список имен подключенных клиентов
    public ArrayList getClientList(){
    	ArrayList myUser = new ArrayList();
        Iterator i = connectedUser.keySet().iterator();
        String user = null;
 
        while(i.hasNext()){
        	user = i.next().toString();
            myUser.add(user);
        }
        return myUser;
    }
 
    //добавляет клиентов в список подключенных HashMap(имя, сокет)
    public void addClient(Socket clientSocket) throws RemoteException{
        connectedUser.put(getUsername(), clientSocket);		
        sendPublicMessage(ONLINE, getUsername(), "CLIENT");
    }
 
    //Трансляция публичного сообщения
    public void sendPublicMessage(String keyword, String username, String message) throws RemoteException{
        Iterator i = connectedUser.keySet().iterator();
        String user = null;
        while(i.hasNext()){
        	user = i.next().toString();		//получаем имя клиентов
            try{
                ClientSocket =  connectedUser.get(user);	//создаем сокет, и передаем сокет юзера, для передачи ему сообщения
                output = new PrintWriter(ClientSocket.getOutputStream(), true);//создаем поток для отправки и привязываем к сокету
                writeFile(keyword + "***" + username + "***" + message);//пишем в файл сервера
                output.println(keyword + "***" + username + "***" + message);//отправляем поток
                output.flush();//закрываем поток
                
            }catch(IOException ioe){
                connectedUser.remove(user);
                sendPublicMessage(OFFLINE, user, user + " has been left the conversation");
            }
        }
    }
    
   //Пишет в userFile имя юзера которому будет передан файл
    public void sendFileUser(String user)throws RemoteException{
    	userFile=null;
    	Iterator i = connectedUser.keySet().iterator();
        String next="";
        while(i.hasNext()){
        	next=i.next().toString();
        	if(next.equals(user)){
        		userFile=user;
        	}
        }
    }
    
    //Трансляция приватного сообщения
    public void sendPrivateMessage(String userPrivate, String username, String message) throws RemoteException{
        Iterator i = connectedUser.keySet().iterator();
        String next="";
        while(i.hasNext()){
        	next=i.next().toString();
            try{
                if(next.equals(userPrivate)){					//отправляем получателю
                	ClientSocket =  connectedUser.get(userPrivate);
                	output = new PrintWriter(ClientSocket.getOutputStream(), true);
                	writeFile(username + "***" + userPrivate + "***" + message);
                	output.println(PRIVATE + "***" + username + "***" + message);
                	output.flush();
                }
                else if(next.equals(username)){					//отправляем отправителю
                	ClientSocket =  connectedUser.get(username);
                	output = new PrintWriter(ClientSocket.getOutputStream(), true);
                	writeFile(username + "***" + userPrivate + "***" + message);
                	output.println(IPRIVATE + "***" + userPrivate + "***" + message);
                	output.flush();
                }
            }catch(IOException ioe){
                connectedUser.remove(userPrivate);
                sendPublicMessage(OFFLINE, userPrivate, userPrivate + " has been left the conversation");
            }
        }
    }
 
    //Исключает клиента из списка, и пишет в чат об этом
    public void disconnect(String username) throws RemoteException{
    	sendPublicMessage(OFFLINE, username, username + " has been left the conversation");
        sendPublicMessage(PUBLICMESSAGE, "SERVER", username + " has been left the conversation");
        writeFile(OFFLINE + username + " has been left the conversation");
    	connectedUser.remove(username);
    }
    
    //Записывает историю сообщений
    public void writeFile(String str){
    	Date d = new Date();
    	SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //формат времени
    	PrintStream printStream;
		try {
			printStream = new PrintStream(new FileOutputStream("C://History.txt", true), true);
			printStream.println(format1.format(d)+"  "+str); //дописывает время
			printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
   
}

//класс реализующий поток для принятия файлов
class ConnectFile extends Thread{
@SuppressWarnings("resource")
	public void run(){		//метод run запускаеться при вызове класса, в методе main класса ChatServer
		ServerSocket ss=null;
		int portin = 2155;
		try {
			ss = new ServerSocket(portin);	//создаем серверный сокет
		} catch (IOException e3) {
			e3.printStackTrace();
		}

		Socket socketin=null;
		System.out.println("Wait connect...port"+portin);
		while(true){		//цикл для принятия файлов
			try {
				socketin = ss.accept();	//ожидание клиентов
				int portout = 2154;
			    Socket socketout = null;
			    ChatServer ch= new ChatServer(); //|Создаем сокет для перенаправления потока байт другому клиенту
			    //передаем IP клиента, который берм по имени с Hashmap и порт для отправки на сервере и приеме на клиенте(2154)
				socketout = new Socket(ChatServer.connectedUser.get(ChatServer.userFile).getInetAddress(),portout);
					
				InputStream in = socketin.getInputStream(); //создаем Input поток и привязываем к сокету
                DataInputStream din = new DataInputStream(in);//переобразуем поток в передачу потока байтов
                OutputStream out = socketout.getOutputStream();
                DataOutputStream dout = new DataOutputStream(out);
                
                int filesCount = din.readInt();//получаем количество файлов от отправителя
                dout.writeInt(filesCount);//отправляем количество байтов получателю
                
                for(int j = 0; j<filesCount; j++){
                	long fileSize = din.readLong(); // получаем размер файла
                    dout.writeLong(fileSize);//отсылаем размер файла  
                    String fileName = din.readUTF(); //прием имени файла
                    dout.writeUTF(fileName); //отправляем именя файла

                    byte[] buffer = new byte[64*1024];//создаем буфер по 64 байта 
                    int count, total = 0;
                    while ((count = din.read(buffer)) != -1){               
                        total += count;
                        dout.write(buffer, 0, count);
                        if(total == fileSize){  //сравниваем все ли байты записаны
                        	break;
                        }
                    } 
                    dout.close();//закрываем потоки
                    din.close();
                    in.close();
                    out.close();
                }
			}catch (IOException e) {
		e.printStackTrace();
			}
		}      
	}
}