import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;

public class CMClientApp {
    public DrawingPanel drawingPanel = new DrawingPanel();
    private CMClientStub m_clientStub;//CM 클라이언트 서언
    private CMClientEventHandler m_eventHandler; //CM 이벤트 헨들러 선언
    private JTextPane consoleTextPane = new JTextPane(); //콘솔을 보여주기위한 텍스트 패널 선언
    private JButton loginButton = new JButton("Login"); //로그인 버튼
    private JButton logoutButton = new JButton("Logout"); //로그아웃 버튼
    private boolean loggedIn = false; //로그인 상태를 나타냄

    //도형을 그리기 위한 패널
    public class DrawingPanel extends JPanel {
        private StringBuilder shapes; // 도형 목록을 저장하는 문자열 빌더
        private String currentShape; // 현재 선택된 도형
        private int xBegin, yBegin, xEnd, yEnd; // 도형의 시작점과 끝점 좌표
        private String shapeType = "line"; // 기본 도형 타입은 선
        private Color lineColor = Color.BLACK; // 기본 선 색상은 검정색
        private Color fillColor = null; // 기본 채우기 색상은 투명
        private int currentThickness = 1; // 기본 선 두께는 1
        private boolean fillShape = false; // 도형을 채울지 여부

        public DrawingPanel() {
            setPreferredSize(new Dimension(600, 400)); // 패널 크기 설정
            setBackground(Color.WHITE); // 배경색 설정
            shapes = new StringBuilder(); // 도형 목록 초기화
            currentShape = "line"; // 기본 도형은 선
            fillColor = null;

            // 도형 선택을 위한 버튼 추가
            JButton lineButton = new JButton("Line");
            JButton circleButton = new JButton("Circle");
            JButton rectangleButton = new JButton("Rectangle");

            // 각 버튼에 대한 액션 리스너 등록
            lineButton.addActionListener(e -> currentShape = "line");
            circleButton.addActionListener(e -> currentShape = "circle");
            rectangleButton.addActionListener(e -> currentShape = "rectangle");

            JPanel shapeButtonPanel = new JPanel(); // 도형 버튼 패널 생성
            shapeButtonPanel.add(lineButton); // 선 버튼 추가
            shapeButtonPanel.add(circleButton); // 원 버튼 추가
            shapeButtonPanel.add(rectangleButton); // 사각형 버튼 추가
            add(shapeButtonPanel, BorderLayout.NORTH); // 도형 버튼 패널을 패널 상단에 추가

            // 선 색상과 채우기 색상 선택을 위한 버튼 추가
            JButton lineColorButton = new JButton("Line Color");
            JButton fillColorButton = new JButton("Fill Color");

            // 각 버튼에 대한 액션 리스너 등록
            lineColorButton.addActionListener(e -> {
                lineColor = JColorChooser.showDialog(this, "Choose Line Color", lineColor);
                //repaint();
            });
            fillColorButton.addActionListener(e -> {
                fillColor = JColorChooser.showDialog(this, "Choose Fill Color", fillColor);
                //repaint();
            });

            JPanel colorButtonPanel = new JPanel(); // 색상 버튼 패널 생성
            colorButtonPanel.add(lineColorButton); // 선 색상 버튼 추가
            colorButtonPanel.add(fillColorButton); // 채우기 색상 버튼 추가
            add(colorButtonPanel, BorderLayout.SOUTH); // 색상 버튼 패널을 패널 하단에 추가

            // 선 두께를 설정하기 위한 슬라이더 추가
            JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
            thicknessSlider.setMajorTickSpacing(1);
            thicknessSlider.setPaintTicks(true);
            thicknessSlider.setPaintLabels(true);
            thicknessSlider.addChangeListener(e -> currentThickness = thicknessSlider.getValue());

            // 두께 버튼을 만들고 버튼에 슬라이더 등록
            JPanel thicknessPanel = new JPanel();
            thicknessPanel.add(new JLabel("Thickness:"));
            thicknessPanel.add(thicknessSlider);
            add(thicknessPanel, BorderLayout.CENTER);

            // 마우스 이벤트 핸들러 등록
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!loggedIn)
                        return; // 로그인되지 않은 상태에서는 그림을 그리지 않음
                    xBegin = e.getX();
                    yBegin = e.getY();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!loggedIn)
                        return; // 로그인되지 않은 상태에서는 그림을 그리지 않음
                    xEnd = e.getX();
                    yEnd = e.getY();
                    // 도형 정보를 문자열로 생성하여 도형 목록에 추가
                    String shape = currentShape + "," + xBegin + "," + yBegin + "," + xEnd + "," + yEnd + ","
                            + colorToHex(lineColor) + "," + colorToHex(fillColor) + "," + currentThickness + ","
                            + fillShape + ";";
                    shapes.append(shape);
                    repaint();

                    testDummyEvent(shape);
                }
            });
        }

        //그림을 그리는 함수
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[] shapeArray = shapes.toString().split(";");
            for (String shape : shapeArray) {
                String[] tokens = shape.split(",");
                if (tokens.length == 9) {
                    String type = tokens[0];
                    int x1 = Integer.parseInt(tokens[1]);
                    int y1 = Integer.parseInt(tokens[2]);
                    int x2 = Integer.parseInt(tokens[3]);
                    int y2 = Integer.parseInt(tokens[4]);
                    Color lc = hexToColor(tokens[5]);
                    Color fc = hexToColor(tokens[6]);
                    int thickness = Integer.parseInt(tokens[7]);
                    boolean fill = Boolean.parseBoolean(tokens[8]);

                    // 그래픽스 2D 객체로 형변환
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(lc); //선 색 설정
                    g2d.setStroke(new BasicStroke(thickness)); //선 두께 설정
                    switch (type) {
                        case "line":    //선 그리기
                            g2d.drawLine(x1, y1, x2, y2);
                            break;
                        case "circle":  //원 그리기
                            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                            g2d.setColor(fc);
                            g2d.fillOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
                            g2d.setColor(lc);
                            g2d.drawOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
                            break;
                        case "rectangle":   //사각형 그리기
                            int width = Math.abs(x2 - x1);
                            int height = Math.abs(y2 - y1);
                            int startX = Math.min(x1, x2);
                            int startY = Math.min(y1, y2);
                            g2d.setColor(fc);
                            g2d.fillRect(startX, startY, width, height);
                            g2d.setColor(lc);
                            g2d.drawRect(startX, startY, width, height);
                            break;
                    }
                }
            }
        }

        // 색상을 16진수 문자열로 변환하는 메서드ㄴ
        private String colorToHex(Color color) {
            if(color == null)   //초기 상태일때 투명
                return "";
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }

        // 16진수 문자열을 Color 객체로 변환하는 메서드
        private Color hexToColor(String hex) {
            if(hex.isEmpty())
                return new Color(0, 0, 0, 0);

            return Color.decode(hex);
        }
    }

    public CMClientApp() {
        m_clientStub = new CMClientStub();  // 클라이언트 스텁 초기화
        m_eventHandler = new CMClientEventHandler(m_clientStub, this);  // 클라이언트 이벤트 핸들러 초기화
        createWhiteboard(); // 화이트 보드 생성 메서드 호출
    }

    public CMClientStub getClientStub() {
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }

    // 메시지를 텍스트로 출력하는 메서드
    public void printMessage(String strText)
    {
        StyledDocument doc = consoleTextPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), strText, null);
            consoleTextPane.setCaretPosition(consoleTextPane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // 스타일이 적용된 메시지를 출력하는 메서드
    public void printStyledMessage(String strText, String strStyleName)
    {
        StyledDocument doc = consoleTextPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
            consoleTextPane.setCaretPosition(consoleTextPane.getDocument().getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void createWhiteboard() {
        // 공유 화이트 보드 프레임 생성
        JFrame whiteboardFrame = new JFrame("공유 화이트 보드");
        whiteboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        whiteboardFrame.setLayout(new BorderLayout());
        whiteboardFrame.setSize(800, 800);

        // 도형 그리는 패널 생성
        whiteboardFrame.add(drawingPanel, BorderLayout.CENTER);

        // 클래스 멤버 변수로 설정
        consoleTextPane.setBackground(new Color(245, 245, 245));
        consoleTextPane.setEditable(false); // 편집 불가능하도록 설정

        // JTextPane을 JScrollPane으로 감싸기
        JScrollPane scrollPane = new JScrollPane(consoleTextPane);
        // JScrollPane의 preferredSize를 설정하여 크기 늘리기
        Dimension preferredSize = new Dimension(300, 200); // 너비 300, 높이 200 설정
        scrollPane.setPreferredSize(preferredSize);
        // JScrollPane을 whiteboardFrame에 추가
        whiteboardFrame.add(scrollPane, BorderLayout.SOUTH);

        // 로그인 및 로그아웃 버튼 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // 오른쪽 정렬 설정

        logoutButton.setPreferredSize(new Dimension(90, 30));
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 로그아웃 메서드 호출
                logout();
            }
        });

        loginButton.setPreferredSize(new Dimension(70, 30));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 로그인 창 표시
                login();
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(logoutButton);
        whiteboardFrame.add(buttonPanel, BorderLayout.NORTH);

        logoutButton.setEnabled(false);

        // 프레임 위치 설정 및 표시
        whiteboardFrame.setLocationRelativeTo(null); // 화면 중앙에 위치
        whiteboardFrame.setVisible(true);
    }
    
    //로그인 함수
    private void login()
    {
        String strUserName = null;
        String strPassword = null;
        boolean bRequestResult = false;

        printMessage("====== login to default server\n");
        JTextField userNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
                "User Name:", userNameField,
                "Password:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
        {
            strUserName = userNameField.getText();
            strPassword = new String(passwordField.getPassword()); // security problem?

            m_eventHandler.setStartTime(System.currentTimeMillis());
            bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
            long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
            if(bRequestResult)
            {
                printMessage("successfully sent the login request.\n");
                printMessage("return delay: "+lDelay+" ms.\n");
            }
            else
            {
                printStyledMessage("failed the login request!\n", "bold");
                m_eventHandler.setStartTime(0);
            }
        }
        printMessage("======\n");

        // 로그인 상태 설정
        loggedIn = true;

    }
    
    
    //로그아웃 함수
    private void logout() {
        boolean bRequestResult = false;
        printMessage("====== logout from default server\n");
        bRequestResult = m_clientStub.logoutCM();
        if (bRequestResult)
            printMessage("successfully sent the logout request.\n");
        else
            printStyledMessage("failed the logout request!\n", "bold");
        printMessage("======\n");

        // 로그인 상태 설정
        loggedIn = false;

        setButtonsAccordingToClientState();
    }
    
    
    //클라이언트 상태에 따라 버튼을 활성화, 비활성화 하는 함수
    public void setButtonsAccordingToClientState()
    {
        int nClientState;
        nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();

        switch(nClientState)
        {
            case CMInfo.CM_INIT: //로그인 버튼
                loginButton.setEnabled(true);
                logoutButton.setEnabled(false);
                break;
            case CMInfo.CM_CONNECT: //로그인 버튼
                loginButton.setEnabled(true);
                logoutButton.setEnabled(false);;
                break;
            case CMInfo.CM_LOGIN: //로그아웃 버튼
                loginButton.setEnabled(false);
                logoutButton.setEnabled(true);
                break;
            case CMInfo.CM_SESSION_JOIN:    //로그아웃 버튼
                loginButton.setEnabled(false);
                logoutButton.setEnabled(true);
                break;
            default:    //로그인 버튼
                loginButton.setEnabled(true);
                logoutButton.setEnabled(false);
                break;
        }
    }
    
    //메세지를 보내기 위한 더비 이벤트 함수
    private void testDummyEvent(String message)
    {
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();
        String strInput = null;

        if(myself.getState() != CMInfo.CM_SESSION_JOIN)
        {
            printMessage("You should join a session and a group!\n");
            return;
        }

        printMessage("draw message\n");

        if(message == null)
            return;

        CMDummyEvent due = new CMDummyEvent();
        due.setHandlerSession(myself.getCurrentSession());
        due.setHandlerGroup(myself.getCurrentGroup());
        due.setDummyInfo(message);
        m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
        due = null;

        printMessage("======\n");
    }

    public static void main(String[] args) {
        CMClientApp client = new CMClientApp();
        CMClientStub clientStub = client.getClientStub();
        CMClientEventHandler eventHandler = client.getClientEventHandler();

        // CM 초기화
        clientStub.setAppEventHandler(eventHandler);
        boolean ret = clientStub.startCM();
        if (!ret) {
            System.err.println("CM initialization error!");
            return;
        }
        System.out.println("CM initialization succeeds.");
    }
}