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


//클라이언트 프로그램이랑 서버랑 다른 프로젝트에서 만드는 이유 :
//클라이언트 프로젝트가 서버 프로젝트에 접속해서 채팅하는 형식이기 때문에 다른 프로젝트에서 만들어져야함 
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
	//server와 다르게 threadPool을 이용하지 않는 이유 :
	//이건 클라이언트 메인함수이기에 더이상 여러 thread가 동시 다발적으로 작동하는 경우가 없음.
	//총 사용하는 Thread의 수는 서버로부터 message를 받는 Thread한개, 서버로 message를 전송하는 Thread 한개로
	//총 두개의 Thread를 사용한다
	TextArea textArea;
	
	//클라이언트 프로그램 동작메소드입니다.
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port); //소캣 초기화
					receive(); //서버로부터 메세지 전달받게 receive함수불러오기
				} catch(Exception e) {//오류 발생시
					if(!socket.isClosed()) {
						stopClient(); //끊어버리기
						System.out.println("[서버 접속 실패]");
						Platform.exit(); //아예 종료시키기
					}
				}
			}
		};
		thread.start();//thread를 시작시킴
	}
	
	//클라이언트 프로그램 종료 메소드입니다.
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
				//소켓이 열려 있다면 소켓을 닫음
			}
		} catch(Exception e) {
			e.printStackTrace();
			//에러발생시 표시
		}
	}
	
	//서버로부터 메세지를 전달받는 메소드 입니다.
	public void receive() {
		while(true) {//서버와 유사한 형태로 계속해서 반복하여 메세지를 전달받음
			try {
				InputStream in = socket.getInputStream();//입력받는데 사용되는 inputstream
				byte[] buffer = new byte[512];//값을 저장할 buffer
				int length = in.read(buffer);//read함수를 이용해서 buffer에 있는 값을 받아옴
				if(length == -1) throw new IOException(); // 입력받는 중에 오류가 생긴다면 ioexception발생
				String message = new String(buffer, 0, length, "UTF-8");//메세지란 변수에 담을수 있게함
				Platform.runLater(()-> {//GUI요소는 바로 입력이 불가하므로 runLater함수를 이용해서 텍스트창 조작
					textArea.appendText(message);
				});
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	//서버로 메세지를 전송하는 메소드입니다.
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();//서버로 메세지를 보낼 수 있도록 해주는 스트림
					byte[] buffer = message.getBytes("UTF-8");//보낼 메세지 값을 저장시킬 buffer
					out.write(buffer);
					out.flush();//위 행위가 모드 끝낫음을 알리는 코드
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	
	//실제로 프로그램을 동작시키는 메소드입니다.
	@Override
	public void start(Stage primaryStage) {
		BorderPane map = new BorderPane();
		Button newthing = new Button("지도보기");
		newthing.minWidth(400);
		map.setLeft(newthing);
		newthing.setOnAction(e->{map();});
		Label t = new Label("아래 텍스트창은 메모장입니다");
		map.setTop(t);
		TextArea field = new TextArea();
		map.setCenter(field);
		
		
		
		
		BorderPane root = new BorderPane();//팬 추가
		root.setPadding(new Insets(5));//디자인 부드럽게해주는 거
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		//실질적으로  hbox안에 세개의 텍스트 필드가 만들어 질수 있도록 만듬
		root.setTop(hbox);
		//borderpane의 위쪽에 달수 있게 바꿈
		
		textArea = new TextArea();
		textArea.setEditable(false);//변경 불가
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");//다시 입력할 수 있도록 텍스트 창을 비워줌
			input.requestFocus(); //다시 메세지 보낼 수 있게 fucus설정
		});
		
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(), port); //특정 IP의 어떠한 port번호로만 접속하능하게 만들어줌
				Platform.runLater(() ->{
					textArea.appendText("[ 채팅방 접속]\n");
				});
				connectionButton.setText("종료하기");//현재 상태가 연결된 상태이므로 종료하기로 바꿔줌
				input.setDisable(false); //사용자가 버튼으로 어떠한 처리를 하게 함
				sendButton.setDisable(false);
				input.requestFocus();
			}	else {//만약 오류가나서 다른 버튼이거나 한다면
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ 채팅방 퇴장]\n");
				});
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);				
			}
		});
		BorderPane pane = new BorderPane(); //pane생성
		pane.setLeft(connectionButton);//왼쪽에 connectionButton이 들어감
		pane.setCenter(input);
		pane.setRight(sendButton);
		map.setMinHeight(50);
		
		map.setBottom(root);//root의 아래쪽에는 방금만든 3가지가 들어감
		root.setBottom(pane);//root의 아래쪽에는 방금만든 3가지가 들어감
		Scene scene = new Scene(map, 700, 500);
		primaryStage.setTitle("[ 채팅 클라이언트 ]");
		primaryStage.setScene(scene); //신등록
		primaryStage.setOnCloseRequest(event -> stopClient()); //사용자가 화면 받기 버튼을 누르면 클라이언트 종료
		primaryStage.show();//보여지게 만들기
		
		connectionButton.requestFocus();//기본적으로 프로그램 실행시 접속하기 버튼이 포커싱되게 만듬
		
		
		
		
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
	
	//프로그램의 진입점 입니다.
	public static void main(String[] args) {
		launch(args);
	}
}
