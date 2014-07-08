import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JOptionPane;
 
public class ChatServer{
	
	static ConnectFile connectFiles;	
    static HashMap<String, Socket> connectedUser = new HashMap<String, Socket>();//���������, ��� ����� � �������������� ��� �����
    static Socket ClientSocket = null; //���������� ����� ��� ���������
    private static ServerSocket serverSocket;	//������� ����� ������� ��� ���������
    private static String username = null;      //��� �����
    public static String userFile = null;	//��� ����� �������� ����� ������ ����
    private static PrintWriter output;
    
    //��������� �������� ����
    private static final String PUBLICMESSAGE = "PUBLICMESSAGE";
    private static final String PRIVATE = "PRIVATE";
    private static final String IPRIVATE = "IPRIVATE";
    private static final String ONLINE = "ONLINE";
    private static final String OFFLINE = "OFFLINE";
    private static final String HOST = "192.168.1.1";
 
    //������� ������ ������� RMI ��� ����������� ����������� ��������
    public ChatServer(){
        try{
        	ChatImpl csi = new ChatImpl(this);
            Naming.rebind("rmi://"+"192.168.1.1"+":1099/ChatService", csi);//�������� ����� � �������, �������� �������
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
    
    //��������� �������� ������� ������� RMI
    //������� ����� ��� ������ � �������
    //�������� processConnection ��� ������ ��������
    public static void main (String[] args) throws IOException{
    	 connectFiles = new ConnectFile();		//������� ������ ������ ��� ������ ������
    	 connectFiles.start();					//� ������ ����� ��� ����
    	 try{
    		 ChatServer cs = new ChatServer();
    		 cs.processConnection(4444);
    	 }catch (ArrayIndexOutOfBoundsException ae){
    		 JOptionPane.showMessageDialog(null, "Please insert the port", "ATTENTION", JOptionPane.INFORMATION_MESSAGE);
    		 System.exit(-1);
    	 }
    }
 
    //������� ��������� ����� ��� ���������
    //� ����� ������� ����������� ����� ��������
    private void processConnection(int port){
    	try{
    		serverSocket = new ServerSocket(port);		//������� ��������� �����
    		System.out.println("Server is running on port " + port + " .... ");
    	}catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "Could not listen port " + port, "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
 
    	try{
    		while (true){
    			addClient(serverSocket.accept());	//����� ���� ��������
    			String username = getUsername();	
                sendPublicMessage(PUBLICMESSAGE, "SERVER", "[" + username + "] is now online");//��������� � ��� � ����� �������
            }
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "Could not accept connection.", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
 
    //��������� ���������� username
    public void connect(String username){
        this.username = username;
    }
 
    //���������� ���������� username
    public String getUsername(){
        return username;
    }
 
    //���������� ������ ���� ������������ ��������
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
 
    //��������� �������� � ������ ������������ HashMap(���, �����)
    public void addClient(Socket clientSocket) throws RemoteException{
        connectedUser.put(getUsername(), clientSocket);		
        sendPublicMessage(ONLINE, getUsername(), "CLIENT");
    }
 
    //���������� ���������� ���������
    public void sendPublicMessage(String keyword, String username, String message) throws RemoteException{
        Iterator i = connectedUser.keySet().iterator();
        String user = null;
        while(i.hasNext()){
        	user = i.next().toString();		//�������� ��� ��������
            try{
                ClientSocket =  connectedUser.get(user);	//������� �����, � �������� ����� �����, ��� �������� ��� ���������
                output = new PrintWriter(ClientSocket.getOutputStream(), true);//������� ����� ��� �������� � ����������� � ������
                writeFile(keyword + "***" + username + "***" + message);//����� � ���� �������
                output.println(keyword + "***" + username + "***" + message);//���������� �����
                output.flush();//��������� �����
                
            }catch(IOException ioe){
                connectedUser.remove(user);
                sendPublicMessage(OFFLINE, user, user + " has been left the conversation");
            }
        }
    }
    
   //����� � userFile ��� ����� �������� ����� ������� ����
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
    
    //���������� ���������� ���������
    public void sendPrivateMessage(String userPrivate, String username, String message) throws RemoteException{
        Iterator i = connectedUser.keySet().iterator();
        String next="";
        while(i.hasNext()){
        	next=i.next().toString();
            try{
                if(next.equals(userPrivate)){					//���������� ����������
                	ClientSocket =  connectedUser.get(userPrivate);
                	output = new PrintWriter(ClientSocket.getOutputStream(), true);
                	writeFile(username + "***" + userPrivate + "***" + message);
                	output.println(PRIVATE + "***" + username + "***" + message);
                	output.flush();
                }
                else if(next.equals(username)){					//���������� �����������
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
 
    //��������� ������� �� ������, � ����� � ��� �� ����
    public void disconnect(String username) throws RemoteException{
    	sendPublicMessage(OFFLINE, username, username + " has been left the conversation");
        sendPublicMessage(PUBLICMESSAGE, "SERVER", username + " has been left the conversation");
        writeFile(OFFLINE + username + " has been left the conversation");
    	connectedUser.remove(username);
    }
    
    //���������� ������� ���������
    public void writeFile(String str){
    	Date d = new Date();
    	SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); //������ �������
    	PrintStream printStream;
		try {
			printStream = new PrintStream(new FileOutputStream("C://History.txt", true), true);
			printStream.println(format1.format(d)+"  "+str); //���������� �����
			printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
   
}

//����� ����������� ����� ��� �������� ������
class ConnectFile extends Thread{
@SuppressWarnings("resource")
	public void run(){		//����� run ������������ ��� ������ ������, � ������ main ������ ChatServer
		ServerSocket ss=null;
		int portin = 2155;
		try {
			ss = new ServerSocket(portin);	//������� ��������� �����
		} catch (IOException e3) {
			e3.printStackTrace();
		}

		Socket socketin=null;
		System.out.println("Wait connect...port"+portin);
		while(true){		//���� ��� �������� ������
			try {
				socketin = ss.accept();	//�������� ��������
				int portout = 2154;
			    Socket socketout = null;
			    ChatServer ch= new ChatServer(); //|������� ����� ��� ��������������� ������ ���� ������� �������
			    //�������� IP �������, ������� ���� �� ����� � Hashmap � ���� ��� �������� �� ������� � ������ �� �������(2154)
				socketout = new Socket(ChatServer.connectedUser.get(ChatServer.userFile).getInetAddress(),portout);
					
				InputStream in = socketin.getInputStream(); //������� Input ����� � ����������� � ������
                DataInputStream din = new DataInputStream(in);//������������ ����� � �������� ������ ������
                OutputStream out = socketout.getOutputStream();
                DataOutputStream dout = new DataOutputStream(out);
                
                int filesCount = din.readInt();//�������� ���������� ������ �� �����������
                dout.writeInt(filesCount);//���������� ���������� ������ ����������
                
                for(int j = 0; j<filesCount; j++){
                	long fileSize = din.readLong(); // �������� ������ �����
                    dout.writeLong(fileSize);//�������� ������ �����  
                    String fileName = din.readUTF(); //����� ����� �����
                    dout.writeUTF(fileName); //���������� ����� �����

                    byte[] buffer = new byte[64*1024];//������� ����� �� 64 ����� 
                    int count, total = 0;
                    while ((count = din.read(buffer)) != -1){               
                        total += count;
                        dout.write(buffer, 0, count);
                        if(total == fileSize){  //���������� ��� �� ����� ��������
                        	break;
                        }
                    } 
                    dout.close();//��������� ������
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