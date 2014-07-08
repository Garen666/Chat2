import java.rmi.*;
import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;

import java.awt.event.*;

import javax.swing.event.*;
 
public class RMIClient extends JFrame implements ActionListener, Runnable
{
    private Chatting c;
    static ConnectFile2 connectFiles2;  
 
    private static String ipAddress;
    private int port;
    private static String ipAddress2;
    private int port2;
    private BufferedReader in = null;
    private Thread thread;
    private  JButton jButtonConnect;
    private  JButton jButtonSend;
    private  JButton jButtonHistory;
    private  JButton jButtonFile;
    private  JLabel jLabelUserList;
    private  JList jListUser;
    private  JScrollPane jScrollPaneListUser;
    private  JScrollPane jScrollPaneMessage;
    public  JTextArea jTextAreaMessage;
    private JTextField jTextSendMessage;
    private JTextField jTextUserName;
    private  JTextField jTextFindUser;
    
    private Socket socket = null;
    private Socket socketIn=null;
    private  DefaultListModel listClient;
    private String message;
 
    private final String SEPARATOR = "\\*\\*\\*";
    private final String PUBLICMESSAGE = "PUBLICMESSAGE";
    private final String PRIVATE = "PRIVATE";
    private final String IPRIVATE = "IPRIVATE";
    private final String ONLINE = "ONLINE";
    private final String OFFLINE = "OFFLINE";
    private final static String HOST = "192.168.1.1";
 
    //Запускает инициализацию формы
    public RMIClient(){
        initComponents();
        thread = new Thread(this);
    }
 
    //Инициализация компонентов
    @SuppressWarnings("unchecked")
    private  void initComponents(){
        listClient = new DefaultListModel();
        jScrollPaneMessage = new JScrollPane();
        jTextAreaMessage = new JTextArea();
        jTextUserName = new JTextField();
        jTextFindUser = new JTextField();
        jButtonConnect = new JButton();
        jScrollPaneListUser = new JScrollPane();
        jListUser = new JList(listClient);
        jTextSendMessage = new JTextField();
        jButtonSend = new JButton();
        jLabelUserList = new JLabel();
        jButtonHistory = new JButton();
        jButtonFile= new JButton();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Client");
        setResizable(false);
        getContentPane().setLayout(null);
 
        jTextAreaMessage.setColumns(20);
        jTextAreaMessage.setEditable(false);
        jTextAreaMessage.setRows(5);
        jTextAreaMessage.setAutoscrolls(false);
        jScrollPaneMessage.setViewportView(jTextAreaMessage);
        getContentPane().add(jScrollPaneMessage);
        jScrollPaneMessage.setBounds(10, 10, 340, 240);
 
        jTextUserName.addActionListener(this);
        getContentPane().add(jTextUserName);
        jTextUserName.setBounds(360, 10, 140, 20);
        
        jTextFindUser.addActionListener(this);
        getContentPane().add(jTextFindUser);
        jTextFindUser.setBounds(360, 230, 230, 20);
        jTextFindUser.addKeyListener(new KeyAdapter(){
        	public void keyPressed(KeyEvent e) {
        		if (e.getKeyChar() == KeyEvent.VK_ENTER) {
        	        findUserInList();
        	      }
            }
        });
 
        jButtonConnect.setFont(new java.awt.Font("Times New Roman", 0, 11));
        jButtonConnect.setText("Connect");
        getContentPane().add(jButtonConnect);
        jButtonConnect.setBounds(510, 10, 80, 21);
 
        jListUser.setToolTipText("List of User");
        jScrollPaneListUser.setViewportView(jListUser);
 
        getContentPane().add(jScrollPaneListUser);
        jScrollPaneListUser.setBounds(360, 50, 230, 180);
 
        jTextSendMessage.addActionListener(this);
 
        getContentPane().add(jTextSendMessage);
        jTextSendMessage.setBounds(10, 260, 340, 30);
 
        jButtonSend.setFont(new java.awt.Font("Times New Roman", 0, 11));
        jButtonSend.setText("Send");
        jButtonSend.addActionListener(this);
        jButtonSend.addActionListener(this);
       
        getContentPane().add(jButtonSend);
        jButtonSend.setBounds(360, 261, 70, 30);
 
        jLabelUserList.setFont(new java.awt.Font("Arial Black", 0, 11));
        jLabelUserList.setText("User List");
        getContentPane().add(jLabelUserList);
        jLabelUserList.setBounds(360, 30, 55, 17);
 
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-570)/2, (screenSize.height-330)/2, 605, 330);
 
        jButtonConnect.addActionListener(this);
        jButtonSend.setEnabled(false);
        
        jButtonHistory.setFont(new java.awt.Font("Times New Roman", 0, 11));
        jButtonHistory.setText("History");
        getContentPane().add(jButtonHistory);
        jButtonHistory.addActionListener(this);
        jButtonHistory.setBounds(520, 261, 70, 30);
        
        jButtonFile.setFont(new java.awt.Font("Times New Roman", 0, 11));
        jButtonFile.setText("File");
        getContentPane().add(jButtonFile);
        jButtonFile.addActionListener(this);
        jButtonFile.setBounds(440, 261, 70, 30);
        
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
            	JList theList = (JList) mouseEvent.getSource();
            	if (mouseEvent.getClickCount() == 2) {
            		int index =  theList.locationToIndex(mouseEvent.getPoint());
            		if (index >= 0) {
            			Object o =  theList.getModel().getElementAt(index);
            			jTextSendMessage.setText("[private "+o.toString()+"]");
            		}
            	}
            }
        };
        jListUser.addMouseListener(mouseListener);
        jTextAreaMessage.updateUI();
        }
 
    //Создает второй поток для обработки файлов
    //Вызывает вызов создания формы
    //Инициализирует данные для сокета
    //Вызывает создание заглушки для работы с сервером по RMI
    public static void main(String args[]){
    	connectFiles2 = new ConnectFile2();	
        connectFiles2.start(); 
        try{
        	RMIClient rm = new RMIClient();
            rm.setIPAddress(HOST); //инициализация пеерменной ipAddress
            rm.setPort(4444);		//инициализация порта
            rm.setServer(ipAddress);	//создание заглушки
            rm.setVisible(true);	//форма
        }catch (ArrayIndexOutOfBoundsException ae){
            JOptionPane.showMessageDialog(null, "Please insert the port", "ATTENTION", JOptionPane.INFORMATION_MESSAGE);
            System.exit(-1);
        }
    }
 
    //инициализация Ip
    private void setIPAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }
 
    //инициализация порта
    private void setPort(int port){
        this.port = port;
    }
 
    //возвращает переменную ip
    private String getIPAddress(){
        return ipAddress;
    }
 
    //возвращат порт
    private int getPort(){
        return port;
    }
 
    //Создает заглушку для работы с сервером по RMI
    private void setServer(String ipAddress){
        try{  //Возвращает ссылку, заглушки, для удаленного объекта, связанного с указанным name 
        	c = (Chatting) Naming.lookup("rmi://" + ipAddress + "/ChatService");
        }catch (MalformedURLException murle){
            System.out.println();
            System.out.println("MalformedURLException");
            System.out.println(murle);
        }catch (RemoteException re){
            System.out.println();
            System.out.println("RemoteException");
            System.out.println(re);
        }catch (NotBoundException nbe){
            System.out.println();
            System.out.println("NotBoundException");
            System.out.println(nbe);
        }catch (Exception e){
            System.out.println(e);
        }
    }
 
    //выводим полученое сообщение и пишем в историю
    public void updateMessage(String username, String message) throws RemoteException{
    	Date d = new Date();
 	    SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
 	    Font font = new Font("Verdana", Font.BOLD, 12);
        jTextAreaMessage.append(format1.format(d)+ "  "+username + " >> " + message + "\n");
        writeFile(username + ": " + message);
    }
   //Запускаеться при подключение к серверу
   //Ловит сообщения и отправляет на вывод в UpdateMessage 
    public void run(){
        try{
        	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	while((message = in.readLine()) != null){
                System.out.println(message);
                String[] fromServer = message.split(SEPARATOR);
                if (fromServer[0].equals(ONLINE) || fromServer[0].equals(OFFLINE)){
                    updateClient(c.getClientList());
                }
                else if (fromServer[0].equals(PUBLICMESSAGE)){
                    String sender = fromServer[1];
                    String content = fromServer[2];
                    updateMessage(sender, content);
                }
                else if (fromServer[0].equals(PRIVATE)){
                    String sender = "["+fromServer[1]+" private] ";
                    String content = fromServer[2];
                    updateMessage(sender, content);
                }
                else if (fromServer[0].equals(IPRIVATE)){
                    String sender = "[private "+fromServer[1]+"] ";
                    String content = fromServer[2];
                    updateMessage(sender, content);
                }
        	}
        	in.close();
            socket.close();
        }
        catch(java.net.UnknownHostException e) {}
        catch(IOException e) {}
    }
 
    //Обновляет список клиентов на форме
    public void updateClient(ArrayList allClientList) throws RemoteException{
        listClient.clear();
        int i = 0;
        String username;
        for(i=0; i<allClientList.size(); i++){
            username = allClientList.get(i).toString();
            listClient.addElement(username);
        }
    }
 
    //Слушатель по нажатию на кнопки
    @SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent ae){
        if (ae.getActionCommand().equals("Connect") && !jTextUserName.getText().equals("")){
            try{
                c.connect(jTextUserName.getText()); //вызов через интерфейс серверу, записывает имя клиента в переменную Username
                socket = new Socket(ipAddress, port);//создание сокета
                jTextUserName.setEditable(false);
                jButtonConnect.setText("Disconnect");//меняем текст кнопки
                jTextAreaMessage.append("You are connect to server"+'\n');
                thread.start();							//запуск run для отловки сообщений
                jButtonSend.setEnabled(true);
            }
            catch (RemoteException re) {}
            catch(java.net.UnknownHostException e){
                JOptionPane.showMessageDialog(null, "Can not connect to server " + ipAddress ,"WARNING", JOptionPane.WARNING_MESSAGE);
                System.exit(-1);
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(null, "The server " + ipAddress + " on port " + port + " is not found!", "ERROR",JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        }
        else if (ae.getActionCommand().equals("Disconnect")){
            try{
                c.disconnect(jTextUserName.getText());	//вызываем через интерфейс на сервере
                jTextUserName.setText("");
                jTextUserName.setEditable(true);
                jButtonSend.setEnabled(false);
                jButtonConnect.setText("Connect");
                listClient.clear();
                thread.interrupt();		//останавливаем поток
            }catch (RemoteException re) {}
        }
        
        else if (ae.getSource() == jButtonSend  && !jTextSendMessage.getText().equals("")){ 		
        	if(jTextSendMessage.getText().length() > 10 && jTextSendMessage.getText().substring(0, 9).equals("[private ")){
        		try{	//если сообщение приватное
        			String user=userOfPrivateMessage(jTextSendMessage.getText());//пишем имя получателя
                	writeFile("[PRIVATE "+user+"] "+jTextUserName.getText()+": "+ClearMessage(jTextSendMessage.getText()));//в историю
                    c.sendPrivateMessage(user, jTextUserName.getText(), ClearMessage(jTextSendMessage.getText()));//вызов через интерфейс
                    jTextSendMessage.setText("");
                }catch (RemoteException re) {}
        		
        	}else{		//если сообщение публичное
        		try{
            	writeFile(jTextUserName.getText()+": "+jTextSendMessage.getText());//пишем в историю
                c.sendPublicMessage(PUBLICMESSAGE, jTextUserName.getText(), jTextSendMessage.getText());//вызов через интерфейс
                jTextSendMessage.setText("");
        		}catch (RemoteException re) {}
        	}
        }
        else if (ae.getSource() == jButtonHistory){
        	try {//открываем историю в блокноте
        		Runtime.getRuntime().exec("notepad.exe " + System.getProperty("user.dir")+ "\\History.txt");
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        else if (ae.getSource() == jButtonFile){
        	String user=userOfPrivateMessage(jTextSendMessage.getText());//кому отправляем файл
        	try {
				c.sendFileUser(user);//пишем на сервере в userFile
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
        	//////////////////////////////////////////////////
        	JFileChooser fileopen = new JFileChooser();	
        	int ret = fileopen.showDialog(null, "Открыть файл");   //выбор файла  для отправки        
        	if (ret == JFileChooser.APPROVE_OPTION) {
        	    File file = fileopen.getSelectedFile(); //создаем обьект класса файл и присваиваем ему наш файл
        	    JOptionPane.showMessageDialog(null, file.getName());
        	    port2 = 2155;
        	    ipAddress2 = HOST;
        	    InetAddress ipAddress22 = null;
        	    try {
        	    	ipAddress22 = InetAddress.getByName(ipAddress2);
        	    	socketIn = new Socket(ipAddress22, port2); //создаем сокет для отправки серверу
        	    }catch (UnknownHostException e) {
                e.printStackTrace();
        	    }catch (IOException e) {
                e.printStackTrace();
        	    }
            //----------------------------------------------
        	    int countFiles = 1;
        	    DataOutputStream outD; 
        	    try{
        	    	outD = new DataOutputStream(socketIn.getOutputStream());//создаем поток файлов и цепляем к сокету
        	    	outD.writeInt(countFiles);//отсылаем количество файлов
        	    	for(int i = 0; i<countFiles; i++){
        	    		outD.writeLong(file.length());//отсылаем размер файла
        	    		String fileName = file.getName();
        	    		outD.writeUTF(fileName);//отсылаем имя файла
        	    		jTextAreaMessage.append("Отправляем файл "+ fileName);
        	    		jTextAreaMessage.append("Размером в  "+ file.length()+" ,байт");
        	    		FileInputStream in = new FileInputStream(file);
        	    		byte [] buffer = new byte[64*1024];//делим файл по 64 байта
        	    		int count;
                    
        	    		while((count = in.read(buffer)) != -1){
        	    			outD.write(buffer, 0, count);	//отправляем кусок файла
        	    		}
        	    		jTextAreaMessage.append("Файл Отправлен________________");
        	    		outD.close();//закрываем потоки
        	    		in.close();
        	    	}           
        	    	socketIn.close(); //закрываем сокет
        	    }catch(IOException e){
                e.printStackTrace();
        	    }   
        	}
        }
    }
    
    
    //пишет в историю
    public void writeFile(String str){
    	Date d = new Date();
    	SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    	PrintStream printStream;
		try {
			printStream = new PrintStream(new FileOutputStream(System.getProperty("user.dir")+ "\\History.txt", true), true);
			printStream.println(format1.format(d)+"  "+str);
			printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
    }
    
    
    //Выделяет из сообщения имя получателя и возвращает его
    public String userOfPrivateMessage (String message){
    	String str="";
		int len=message.length();
		int i =9;
		while(i<len-1){
			if(message.substring(i,i+1).equals("]")){
				break;
			}
			else{
				str=str+message.substring(i,i+1);
				i++;
			}
		}
		return str;
    }
    
    //Вовзращает чистое сообщение без ключевых слов и имен
    public String ClearMessage (String message){
		int len=message.length();
		int i =9;
		while(i<len-1){
			if(message.substring(i,i+1).equals("]")){
				break;
			}
			else{
				i++;
			}
		}
		if(message.substring(i+1,i+2).equals(" ")){
			i++;
		}
		return message.substring(i+1);
    }
    
    //Поиск онлайн юзеров
    public void findUserInList(){
    	int count = listClient.getSize();
    	for(int i=0;i<count;i++){
    		if(listClient.getElementAt(i).toString().equals(jTextFindUser.getText())){
    			jListUser.setSelectedIndex(i);
    			JOptionPane.showMessageDialog(null,"Ok");
    			return;
    		}
    	}
    	JOptionPane.showMessageDialog(null,"Искомый пользователь не найден.");
    }
    
    
    
}


//Класс для получения файлов 
//Создает свой поток
//Вызываеться в main
class ConnectFile2 extends Thread{
	
	public void run(){
		ServerSocket ss=null;
		int port2 = 2154;
		RMIClient rmi = new RMIClient();
		try {
			ss= new ServerSocket(port2);	//создаем серверный сокет
			while(true){	
				Socket socketOut = ss.accept();	//сокет ждет клиентов
				
				InputStream in = socketOut.getInputStream();					//создает входящий поток для сокета
				DataInputStream din = new DataInputStream(in);
         
				int filesCount = din.readInt();//получаем количество файлов
				//rmi.jTextAreaMessage.append("Передается " + filesCount + " файлов\n");
         rmi.updateMessage("sdfds", "sdfds");;
				for(int i = 0; i<filesCount; i++){
					//rmi.jTextAreaMessage.append("Прием " + (i+1) + "вого файла: \n");
                 
					long fileSize = din.readLong(); // получаем размер файла
                         
					String fileName = din.readUTF(); //прием имени файла
					//rmi.jTextAreaMessage.append("Имя файла: " + fileName+"\n");
					//rmi.jTextAreaMessage.append("Размер файла: " + fileSize + " байт\n");
     
					byte[] buffer = new byte[64*1024];
					FileOutputStream outF = new FileOutputStream(fileName);
					int count, total = 0;
             
					while ((count = din.read(buffer)) != -1){      //получаем части         
						total += count;
						outF.write(buffer, 0, count);
						if(total == fileSize){
							break;
						}
					}
					outF.flush();//закрываем потоки
					outF.close();
					//rmi.jTextAreaMessage.append("Файл принят\n---------------------------------\n");   
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}