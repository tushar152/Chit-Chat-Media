import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ClientForServer extends JFrame implements ActionListener, KeyListener, MouseListener, WindowListener
{
	private static final long serialVersionUID = 1L;
	int port = "PK".hashCode();
	static String ip = null;
	static ServerSocket mutex = null;
	Socket client = null;
	DataInputStream inBuffer = null;
	DataOutputStream outBuffer = null;
	String info = "";
	static String name = "";
	int updateCount = 0;
	
	JTextArea msg = new JTextArea();
	JScrollPane mdata = new JScrollPane(msg);
	
	JButton sendB = new JButton();
	
	DefaultListModel<String> clientModel = new DefaultListModel<String>();
	JList<String> clientList = new JList<String>(clientModel);
	JScrollPane data = new JScrollPane(clientList);
	
	HashMap<String, NewTextArea> textArea = new HashMap<String, NewTextArea>();
	JScrollPane chatPane = new JScrollPane();
	
	public ClientForServer() throws Exception
	{
		setLocationByPlatform(true);
		setSize(610, 600);
		setLocationRelativeTo(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(false);
		
		DefaultListCellRenderer renderer = (DefaultListCellRenderer) clientList.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		clientList.setForeground(Color.GREEN.darker());
		clientList.setFont(new Font("NSimSun", 1, 25));
		clientList.setBackground(getContentPane().getBackground());
		clientList.setSelectionBackground(Color.BLACK);
		clientList.setSelectionForeground(Color.GREEN);
		clientList.addMouseListener(this);
		data.setBounds(0, 0, 250, 500);
		data.setBackground(null);
		data.setBorder(BorderFactory.createLineBorder(getBackground(), 5));
		add(data);
		
		msg.setBackground(Color.WHITE);
		msg.setFont(new Font("NSimSun", 1, 16));
		msg.setForeground(Color.RED);
		msg.setWrapStyleWord(true);
		msg.setLineWrap(true);
		msg.addKeyListener(this);
		msg.setSelectedTextColor(Color.WHITE);
		msg.setSelectionColor(Color.BLUE);
		mdata.setBounds(1, 500, 525, 73);
		msg.setEnabled(false);
		msg.setBorder(BorderFactory.createBevelBorder(NORMAL));
		add(mdata);
		
		sendB.setBounds(535, 505, 64, 64);
		sendB.setIcon(new ImageIcon(Main.class.getResource("/images/sendIcon.png")));
		sendB.addActionListener(this);
		sendB.setCursor(new Cursor(12));
		sendB.setEnabled(false);
		sendB.setToolTipText("Press ctrl+Enter to send the message.");
		add(sendB);
		
		chatPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chatPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		chatPane.setBounds(250, 0, 350, 500);
		chatPane.setVisible(true);
		add(chatPane);
		
		this.addWindowListener(this);
		setVisible(true);
	}
	
	public String stringBefore(String data, String cr)
	{
		String name = "";
		for(int i=0; i<data.length(); i++)
		{
			if(String.valueOf(data.charAt(i)).equals(cr))
				break;
			name+=String.valueOf(data.charAt(i));
		}
		return name;
	}
	
	public String replaceBefore(String msg, char ch)
	{
		String data = msg.replace(name+"::@", "");
		for(int i=0; i<data.length(); i++)
		{
			if(data.charAt(i)==ch)
			{
				return ("YOU: "+data.substring(i+1, data.length()));
			}
		}
		return null;
	}
	
	public void setTextIntoField(String data)
	{
	
		String cName = stringBefore(data, ":");
		try
		{
			if(data.contains("::") && cName.equals(name))
			{
				textArea.get(clientList.getSelectedValue()).insertLeft(replaceBefore(data, ':')+"\n");
			}
			else 
			{
				textArea.get(cName).insertRight(data.replace("::@"+name, "")+"\n");
			}
		}catch(ArrayIndexOutOfBoundsException e) {}
		catch(NullPointerException e) {}
	}
	
	public void run(String name) throws IOException
	{
		client = new Socket(ip, port);
		setTitle(name+"_"+InetAddress.getLocalHost()+":"+port);
		inBuffer = new DataInputStream(client.getInputStream());
		outBuffer = new DataOutputStream(client.getOutputStream());	
		outBuffer.writeUTF(name);
		while(true)
		{
			try
			{
				String str = inBuffer.readUTF();
				info = (str);
				if(info.contains("Online Users Are:") && updateCount==0) 
				{
					intializeUserName(info);
					revalidate();
					updateCount=5;
				}
				else if(info.contains("Only you are online now.")) { }
				else if(info.toLowerCase().contains("!getonlineusers")) { }
				else if(info.contains("Connection closed by:"))
				{
					removeElement(info);
				}
				else if(info.contains("New Client "))
				{
					addElement(info);
				}
				else
				{
					setTextIntoField(info);
				}
			}catch(SocketException e) {
				JOptionPane.showMessageDialog(this, "Server Has Stopped/Closed.");
				break;
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		try {
			mutex = new ServerSocket(11111);
		}catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Application already Running.", "Error_Message:", JOptionPane.WARNING_MESSAGE);
			System.exit(EXIT_ON_CLOSE);
		}
		try{
			name = JOptionPane.showInputDialog("Enter Your Name: ");
			ip = JOptionPane.showInputDialog("Enter Server IP Address: ");
			if(name.isEmpty() || name.isEmpty())
				System.exit(EXIT_ON_CLOSE);
			name = name.toUpperCase();
			try{
				UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
				new ClientForServer().run(name);
			}catch(ConnectException e){
				JOptionPane.showMessageDialog(null, "There is no server to establish the connection.");
				System.exit(EXIT_ON_CLOSE);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}catch(Exception e) {
			System.exit(EXIT_ON_CLOSE);
		}
	}
	
	public void addElement(String text)
	{
		String name = text.replaceAll("New Client", "").replaceAll(" ", "").replaceAll("Connected.", "");
		NewTextArea userM = new NewTextArea();
		clientModel.addElement(name);
		clientList.setSelectedIndex(clientModel.size());
		chatPane.getViewport().add(textArea.get(name));
		textArea.put(name, userM);
		msg.setEnabled(true);
		sendB.setEnabled(true);
	}
	
	public void removeElement(String info)
	{
		String name = info.replaceAll("Connection closed by: Client__", "").replaceAll(" ", "").replace(".", "");
		for(int i=0; i<clientModel.size(); i++)
		{
			if(clientModel.get(i).equals(name))
			{
				try {
					clientModel.remove(i);
					textArea.remove(name);
				}catch(ArrayIndexOutOfBoundsException ex) {}
			}
		}
		if(clientModel.size()<1)
		{
			msg.setEnabled(false);
			sendB.setEnabled(false);	
		}
	}
	
	public void sendAll() throws IOException
	{
		outBuffer.flush();
		String dt = msg.getText();
		outBuffer.writeUTF(dt);
		msg.setText("");
	}
	
	public String[] getUserName(String arg)
	{
		String users[] = null;
		String str = arg.replace("Online Users Are:", "").replace(')', ' ').replace('(', ' ').replace(name, "");
		String name = "";
		int count = 0, j=0;
		for(int i=0; i<str.length(); i++)
		{
			if(str.charAt(i)==',')
				count++;
		}
		users = new String[count];
		for(int i=0; i<str.length(); i++)
		{
			if(str.charAt(i) != ' ')
				name += str.charAt(i);
			if(str.charAt(i)==',')
			{
				String uname = name.replace(",", "");
				users[j] = uname;
				name = "";
				j++;
			}
		}
		return users;
	}
	
	public void intializeUserName(String arg) 
	{
		String users[] = getUserName(arg);
		for(int i=0; i<users.length-1; i++)
		{
			NewTextArea userM = new NewTextArea();
			clientModel.addElement(users[i]);
			textArea.put(users[i], userM);
		}
		clientList.setSelectedIndex(0);
		msg.setEnabled(true);
		sendB.setEnabled(true);
	}
	
	public void send(String name) throws IOException
	{
		outBuffer.flush();
		String data = msg.getText();
		data = "@"+name+": "+data;
		outBuffer.writeUTF(data);
		msg.setText("");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == sendB && msg.getText().length() != 0)
		{
			try {
				send(clientList.getSelectedValue());
			} catch (IOException e1) {}
		}
	}
	@Override
	public void keyPressed(KeyEvent e) 
	{
		if(e.isControlDown() && e.getKeyCode()==10)
		{
			e.consume();
			try {
				send(clientList.getSelectedValue());
			} catch (IOException e1) {}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(e.getSource() == clientList)
		{
			try {
				chatPane.getViewport().add(textArea.get(clientList.getSelectedValue()));
			}catch(IndexOutOfBoundsException ex) {}
		}
	}
	@Override
	public void windowClosing(WindowEvent e) 
	{
		try {
			mutex.close();
		} catch (IOException e1) {}
	}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
}

class NewTextArea extends JTextPane
{
	private static final long serialVersionUID = 1L;
	StyledDocument doc = getStyledDocument();
    SimpleAttributeSet left = new SimpleAttributeSet();
    SimpleAttributeSet right = new SimpleAttributeSet();
    
    public NewTextArea()
    {
    	StyleConstants.setForeground(left, Color.BLUE);
	    StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
	    
	    StyleConstants.setForeground(right, Color.RED);
	    StyleConstants.setAlignment(right, StyleConstants.ALIGN_LEFT);
	    setFont(new Font("NSimSun", 1, 15));
	    setEditable(false);
	    setBackground(getBackground());
    }
    
    public void insertLeft(String str)
    {
	    try
	    {
	        doc.insertString(doc.getLength(), str, left );
	        doc.setParagraphAttributes(doc.getLength(), 1, left, false);
	    }
	    catch(Exception e) { }
    }
    
    public void insertRight(String str)
    {
    	try {
	        doc.insertString(doc.getLength(), str, right );
	        doc.setParagraphAttributes(doc.getLength(), 1, right, false);
    	}catch(Exception e) {}
    }
}
