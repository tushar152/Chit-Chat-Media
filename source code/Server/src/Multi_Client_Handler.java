import java.awt.Color;
import java.awt.Font;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class Multi_Client_Handler extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	int port = "PK".hashCode();
	ServerSocket server = null;
	Socket client = null;
	String serverinfo = "";
	String clientinfo = "";
	int clientCount = 0;
	JTextArea serverInfo = new JTextArea();
	JScrollPane data = new JScrollPane(serverInfo);
	JTextArea clientInfo = new JTextArea();
	JScrollPane mdata = new JScrollPane(clientInfo);
	JButton startB = new JButton();
	List<String> clientList = new ArrayList<String>();
	Vector<ClientHandler> users = new Vector<ClientHandler>();
	
	public Multi_Client_Handler() throws Exception 
	{
		super("My_Server: ");
		setLocationByPlatform(true);
		setSize(900-10, 600-13);
		setLocationRelativeTo(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(false);
		
		serverInfo.setBackground(Color.WHITE);
		serverInfo.setFont(new Font("NSimSun", 1, 16));
		serverInfo.setForeground(Color.BLUE);
		serverInfo.setWrapStyleWord(true);
		serverInfo.setLineWrap(true);
		serverInfo.setEditable(false);
		serverInfo.setSelectedTextColor(Color.WHITE);
		serverInfo.setSelectionColor(Color.BLUE);
		data.setBounds(0, 0, 442, 560);
		data.setBorder(BorderFactory.createLineBorder(Color.GRAY, 5));
		add(data);
		
		clientInfo.setBackground(Color.WHITE);
		clientInfo.setFont(new Font("NSimSun", 1, 16));
		clientInfo.setForeground(Color.BLUE);
		clientInfo.setWrapStyleWord(true);
		clientInfo.setLineWrap(true);
		clientInfo.setEditable(false);
		clientInfo.setSelectedTextColor(Color.WHITE);
		clientInfo.setSelectionColor(Color.BLUE);
		mdata.setBounds(442, 0, 442, 560);
		mdata.setBorder(BorderFactory.createLineBorder(Color.GRAY, 5));
		add(mdata);
		
		setVisible(true);
	}

	public void startServer() throws IOException, BindException
	{
		server = new ServerSocket(port);
		serverinfo = "Server Established at:\nport = "+port+"\nIP = "+InetAddress.getLocalHost();
		serverInfo.setText(serverinfo);
		clientInfo.setText(clientinfo);
		
		while(true)
		{
			clientCount++;
			client = server.accept();
			ClientHandler thread = new ClientHandler(client, clientCount);
			thread.start();
			users.add(thread);
		}
	}

	public static void main(String[] args) 
	{
		try
		{
			UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
			new Multi_Client_Handler().startServer();
		}catch(BindException e) {
			JOptionPane.showMessageDialog(null, "Server Already Bind with this port and IP.");
			System.exit(ABORT);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getOnlineUsers()
	{
		String users = "Online Users Are: (";
		for(int i=0; i<clientList.size(); i++)
		{
			users += clientList.get(i)+",";
		}
		
		return users+")";
	}
	
	public void broadCastToAll(String message) throws IOException
	{
		for(ClientHandler c : users)
		{
			c.sendMessage(message);
		}
	}
	
	public String broadCastToOne(String message) throws IOException
	{
		String msg = message;
		for(ClientHandler c: users)
		{
			if(message.contains(c.name))
			{
				msg = message.replaceAll("@"+c.name+":", "");
				c.sendMessage(message);
			}
		}
		return msg;
	}
	
	class ClientHandler extends Thread
	{
		Socket client = null;
		DataInputStream inBuffer = null;
		DataOutputStream outBuffer = null;
		String name = null;
		int clientNo;
		public ClientHandler(Socket client, int id) throws IOException 
		{
			this.client = client;
			clientNo = id;
			inBuffer = new DataInputStream(client.getInputStream());
			outBuffer = new DataOutputStream(client.getOutputStream());
			name = inBuffer.readUTF();
			clientList.add(name);
			serverinfo = "New Client "+name +" Connected.";
			serverInfo.append("\n"+serverinfo);
			broadCastToAll(serverinfo);
			outBuffer.flush();
			if(clientList.size()<2)
			{
				String data = ("Only you are online now.");
				outBuffer.writeUTF(data);
			}
			else
			{
				String data = getOnlineUsers();
				outBuffer.writeUTF(data);
			}
		}
		
		public void sendMessage(String message) throws IOException
		{
			outBuffer.flush();
			outBuffer.writeUTF(message);
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				try {
					String message = inBuffer.readUTF();
					clientinfo = name+"::"+message;
					if(message.toUpperCase().equals("!GETONLINEUSERS")){
						outBuffer.flush();
						if(clientList.size()<2){
							outBuffer.writeUTF(("Only you are online now."));
						}
						else{
							outBuffer.writeUTF((getOnlineUsers()));
						}
					}
					else if(message.contains("@")){
						clientinfo = broadCastToOne(clientinfo);
					}
					else{
						broadCastToAll(clientinfo);
					}
					clientInfo.append("\n"+clientinfo);
				}
				catch(SocketException e) {
					serverinfo = "Connection closed by: Client__"+name+".";
					serverInfo.append("\n"+serverinfo);
					clientList.remove(name);
					users.remove(this);
					try {
						broadCastToAll(serverinfo);
						client.close();
					} catch (IOException e1) {}
					break;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}