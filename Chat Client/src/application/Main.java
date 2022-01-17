package application;
import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


//Ŭ���̾�Ʈ ���α׷��̶� ������ �ٸ� ������Ʈ���� ����� ���� :
//Ŭ���̾�Ʈ ������Ʈ�� ���� ������Ʈ�� �����ؼ� ä���ϴ� �����̱� ������ �ٸ� ������Ʈ���� ����������� 
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class Main extends Application {
	
	Socket socket;
	//server�� �ٸ��� threadPool�� �̿����� �ʴ� ���� :
	//�̰� Ŭ���̾�Ʈ �����Լ��̱⿡ ���̻� ���� thread�� ���� �ٹ������� �۵��ϴ� ��찡 ����.
	//�� ����ϴ� Thread�� ���� �����κ��� message�� �޴� Thread�Ѱ�, ������ message�� �����ϴ� Thread �Ѱ���
	//�� �ΰ��� Thread�� ����Ѵ�
	TextArea textArea;
	
	//Ŭ���̾�Ʈ ���α׷� ���۸޼ҵ��Դϴ�.
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port); //��Ĺ �ʱ�ȭ
					receive(); //�����κ��� �޼��� ���޹ް� receive�Լ��ҷ�����
				} catch(Exception e) {//���� �߻���
					if(!socket.isClosed()) {
						stopClient(); //���������
						System.out.println("[���� ���� ����]");
						Platform.exit(); //�ƿ� �����Ű��
					}
				}
			}
		};
		thread.start();//thread�� ���۽�Ŵ
	}
	
	//Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ��Դϴ�.
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
				//������ ���� �ִٸ� ������ ����
			}
		} catch(Exception e) {
			e.printStackTrace();
			//�����߻��� ǥ��
		}
	}
	
	//�����κ��� �޼����� ���޹޴� �޼ҵ� �Դϴ�.
	public void receive() {
		while(true) {//������ ������ ���·� ����ؼ� �ݺ��Ͽ� �޼����� ���޹���
			try {
				InputStream in = socket.getInputStream();//�Է¹޴µ� ���Ǵ� inputstream
				byte[] buffer = new byte[512];//���� ������ buffer
				int length = in.read(buffer);//read�Լ��� �̿��ؼ� buffer�� �ִ� ���� �޾ƿ�
				if(length == -1) throw new IOException(); // �Է¹޴� �߿� ������ ����ٸ� ioexception�߻�
				String message = new String(buffer, 0, length, "UTF-8");//�޼����� ������ ������ �ְ���
				Platform.runLater(()-> {//GUI��Ҵ� �ٷ� �Է��� �Ұ��ϹǷ� runLater�Լ��� �̿��ؼ� �ؽ�Ʈâ ����
					textArea.appendText(message);
				});
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	//������ �޼����� �����ϴ� �޼ҵ��Դϴ�.
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();//������ �޼����� ���� �� �ֵ��� ���ִ� ��Ʈ��
					byte[] buffer = message.getBytes("UTF-8");//���� �޼��� ���� �����ų buffer
					out.write(buffer);
					out.flush();//�� ������ ��� �������� �˸��� �ڵ�
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	
	//������ ���α׷��� ���۽�Ű�� �޼ҵ��Դϴ�.
	@Override
	public void start(Stage primaryStage) {
		BorderPane map = new BorderPane();
		Button newthing = new Button("��������");
		newthing.minWidth(400);
		map.setLeft(newthing);
		newthing.setOnAction(e->{map();});
		Label t = new Label("�Ʒ� �ؽ�Ʈâ�� �޸����Դϴ�");
		map.setTop(t);
		TextArea field = new TextArea();
		map.setCenter(field);
		
		
		
		
		BorderPane root = new BorderPane();//�� �߰�
		root.setPadding(new Insets(5));//������ �ε巴�����ִ� ��
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("�г����� �Է��ϼ���.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		//����������  hbox�ȿ� ������ �ؽ�Ʈ �ʵ尡 ����� ���� �ֵ��� ����
		root.setTop(hbox);
		//borderpane�� ���ʿ� �޼� �ְ� �ٲ�
		
		textArea = new TextArea();
		textArea.setEditable(false);//���� �Ұ�
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");//�ٽ� �Է��� �� �ֵ��� �ؽ�Ʈ â�� �����
			input.requestFocus(); //�ٽ� �޼��� ���� �� �ְ� fucus����
		});
		
		Button sendButton = new Button("������");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionButton = new Button("�����ϱ�");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("�����ϱ�")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(), port); //Ư�� IP�� ��� port��ȣ�θ� �����ϴ��ϰ� �������
				Platform.runLater(() ->{
					textArea.appendText("[ ä�ù� ����]\n");
				});
				connectionButton.setText("�����ϱ�");//���� ���°� ����� �����̹Ƿ� �����ϱ�� �ٲ���
				input.setDisable(false); //����ڰ� ��ư���� ��� ó���� �ϰ� ��
				sendButton.setDisable(false);
				input.requestFocus();
			}	else {//���� ���������� �ٸ� ��ư�̰ų� �Ѵٸ�
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ ä�ù� ����]\n");
				});
				connectionButton.setText("�����ϱ�");
				input.setDisable(true);
				sendButton.setDisable(true);				
			}
		});
		BorderPane pane = new BorderPane(); //pane����
		pane.setLeft(connectionButton);//���ʿ� connectionButton�� ��
		pane.setCenter(input);
		pane.setRight(sendButton);
		map.setMinHeight(50);
		
		map.setBottom(root);//root�� �Ʒ��ʿ��� ��ݸ��� 3������ ��
		root.setBottom(pane);//root�� �Ʒ��ʿ��� ��ݸ��� 3������ ��
		Scene scene = new Scene(map, 700, 500);
		primaryStage.setTitle("[ ä�� Ŭ���̾�Ʈ ]");
		primaryStage.setScene(scene); //�ŵ��
		primaryStage.setOnCloseRequest(event -> stopClient()); //����ڰ� ȭ�� �ޱ� ��ư�� ������ Ŭ���̾�Ʈ ����
		primaryStage.show();//�������� �����
		
		connectionButton.requestFocus();//�⺻������ ���α׷� ����� �����ϱ� ��ư�� ��Ŀ�̵ǰ� ����
		
		
		
		
	}
	public void map() {
   	 // Creating and running Chromium engine
       EngineOptions options =
               EngineOptions.newBuilder(HARDWARE_ACCELERATED).licenseKey("1BNDHFSC1G1IVW65PORHPPAMDGDPCA0WYK59V2VE3XUTMKYAVK74KBZBSKNPMIQ5SC92JW").build();
       Engine engine = Engine.newInstance(options);
       Browser browser = engine.newBrowser();

       SwingUtilities.invokeLater(() -> {
           // Creating Swing component for rendering web content
           // loaded in the given Browser instance.
           BrowserView view = BrowserView.newInstance(browser);

           // Creating and displaying Swing app frame.
           JFrame frame = new JFrame("Hello World");
           
           // Close Engine and onClose app window
           frame.addWindowListener(new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent e) {
                   engine.close();
               }
           });
           
           
           frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
           JTextField addressBar = new JTextField("https://www.google.co.kr/maps/@35.2026027,126.8788276,15z?hl=ko");
           addressBar.addActionListener(e ->
                   browser.navigation().loadUrl(addressBar.getText()));
           frame.add(addressBar, BorderLayout.NORTH);
           frame.add(view, BorderLayout.CENTER);
           frame.setSize(800, 500);
           frame.setLocationRelativeTo(null);
           frame.setVisible(true);

           browser.navigation().loadUrl(addressBar.getText());
       });
		
   }
	
	//���α׷��� ������ �Դϴ�.
	public static void main(String[] args) {
		launch(args);
	}
}
