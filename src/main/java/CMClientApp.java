import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;
    private JTextPane consoleTextPane = new JTextPane();
    private JButton loginButton = new JButton("Login");
    private JButton logoutButton = new JButton("Logout");


    public class DrawingPanel extends JPanel {
        private String shapes; // 그려진 도형들을 문자열로 저장
        private String currentShape; // 현재 그리는 도형
        private int xBegin, yBegin, xEnd, yEnd;
        private String shapeType = "line"; // 기본 도형은 선
        private Color lineColor = Color.BLACK; // 선색
        private int lineWidth = 1; // 선굵기
        private Color fillColor = Color.WHITE; // 색채우기 색상

        public DrawingPanel() {
            setPreferredSize(new Dimension(600, 400));
            setBackground(Color.WHITE);
            shapes = ""; // 초기화
            currentShape = "line"; // 기본 도형은 선
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    xBegin = evt.getX();
                    yBegin = evt.getY();
                }
                public void mouseReleased(MouseEvent evt) {
                    xEnd = evt.getX();
                    yEnd = evt.getY();
                    // 새로운 도형을 shapes에 추가하고 다시 그리기
                    shapes += String.format("%s,%d,%d,%d,%d;", currentShape, xBegin, yBegin, xEnd, yEnd);
                    repaint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent evt) {
                    xEnd = evt.getX();
                    yEnd = evt.getY();
                    // 현재 도형을 shapes에 추가하고 다시 그리기
                    repaint();
                }
            });
            // 도형 선택 버튼 리스너 설정
            JButton lineButton = new JButton("Line");
            JButton circleButton = new JButton("Circle");
            JButton rectangleButton = new JButton("Rectangle");
            ButtonGroup group = new ButtonGroup();
            group.add(lineButton);
            group.add(circleButton);
            group.add(rectangleButton);
            lineButton.addActionListener(e -> currentShape = "line");
            circleButton.addActionListener(e -> currentShape = "circle");
            rectangleButton.addActionListener(e -> currentShape = "rectangle");

            // 선색, 선굵기, 색채우기 설정
            JComboBox<String> colorComboBox = new JComboBox<>(new String[]{"Black", "Red", "Green", "Blue"});
            colorComboBox.addActionListener(e -> {
                String color = (String) colorComboBox.getSelectedItem();
                switch (color) {
                    case "Black":
                        lineColor = Color.BLACK;
                        break;
                    case "Red":
                        lineColor = Color.RED;
                        break;
                    case "Green":
                        lineColor = Color.GREEN;
                        break;
                    case "Blue":
                        lineColor = Color.BLUE;
                        break;
                }
            });

            JComboBox<Integer> widthComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
            widthComboBox.addActionListener(e -> lineWidth = (Integer) widthComboBox.getSelectedItem());

            JButton fillColorButton = new JButton("Fill Color");
            fillColorButton.addActionListener(e -> {
                fillColor = JColorChooser.showDialog(null, "Choose a color", fillColor);
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(lineButton);
            buttonPanel.add(circleButton);
            buttonPanel.add(rectangleButton);
            buttonPanel.add(colorComboBox);
            buttonPanel.add(widthComboBox);
            buttonPanel.add(fillColorButton);
            add(buttonPanel, BorderLayout.NORTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 저장된 도형을 파싱하여 그림을 그립니다.
            String[] shapeArray = shapes.split(";");
            for (String shape : shapeArray) {
                String[] tokens = shape.split(",");
                if (tokens.length == 5) {
                    String type = tokens[0];
                    int x1 = Integer.parseInt(tokens[1]);
                    int y1 = Integer.parseInt(tokens[2]);
                    int x2 = Integer.parseInt(tokens[3]);
                    int y2 = Integer.parseInt(tokens[4]);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(lineColor);
                    g2d.setStroke(new BasicStroke(lineWidth));
                    switch (type) {
                        case "line":
                            g2d.drawLine(x1, y1, x2, y2);
                            break;
                        case "circle":
                            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                            g2d.drawOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
                            g2d.setColor(fillColor);
                            g2d.fillOval(x1 - radius, y1 - radius, radius * 2, radius * 2); // 원 채우기
                            break;
                        case "rectangle":
                            // 계산된 좌표값을 사용하여 사각형 그리기
                            int width = Math.abs(x2 - x1);
                            int height = Math.abs(y2 - y1);
                            int startX = Math.min(x1, x2);
                            int startY = Math.min(y1, y2);
                            g2d.drawRect(startX, startY, width, height);
                            g2d.setColor(fillColor);
                            g2d.fillRect(startX, startY, width, height);
                            break;
                    }
                    g2d.dispose();
                }
            }
        }
        public class Shape {
            private String type;
            private int x1, y1, x2, y2;
            private Color lineColor;
            private int lineWidth;
            private Color fillColor;

            // 생성자
            public Shape(String type, int x1, int y1, int x2, int y2, Color lineColor, int lineWidth, Color fillColor) {
                this.type = type;
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.lineColor = lineColor;
                this.lineWidth = lineWidth;
                this.fillColor = fillColor;
            }

            // toString 메서드 오버라이드
            @Override
            public String toString() {
                return String.format("%s,%d,%d,%d,%d,%s,%d,%s",
                        type, x1, y1, x2, y2, getColorString(lineColor), lineWidth, getColorString(fillColor));
            }

            // Color 객체를 문자열로 변환하는 보조 메서드
            private String getColorString(Color color) {
                return String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue());
            }
        }
    }

    public CMClientApp() {
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub, this);
        createWhiteboard();
    }

    public CMClientStub getClientStub() {
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }

    public void printMessage(String strText)
    {
        StyledDocument doc = consoleTextPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), strText, null);
            consoleTextPane.setCaretPosition(consoleTextPane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return;
    }
    public void printStyledMessage(String strText, String strStyleName)
    {
        StyledDocument doc = consoleTextPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
            consoleTextPane.setCaretPosition(consoleTextPane.getDocument().getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return;
    }

    private void createWhiteboard() {
        // 공유 화이트 보드 프레임 생성
        JFrame whiteboardFrame = new JFrame("공유 화이트 보드");
        whiteboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        whiteboardFrame.setLayout(new BorderLayout());
        whiteboardFrame.setSize(800, 800);

        // 도형 그리는 패널 생성
        DrawingPanel drawingPanel = new DrawingPanel();
        whiteboardFrame.add(drawingPanel, BorderLayout.CENTER);

        // 클래스 멤버 변수로 설정
        consoleTextPane.setBackground(new Color(245, 245, 245));
        consoleTextPane.setEditable(false); // 편집 불가능하도록 설정

        // JTextPane을 JScrollPane으로 감싸기
        JScrollPane scrollPane = new JScrollPane(consoleTextPane);
        // JScrollPane의 preferredSize를 설정하여 크기 늘리기
        Dimension preferredSize = new Dimension(300, 200); // 예시로 너비 300, 높이 200 설정
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
    }

    private void logout() {
        boolean bRequestResult = false;
        printMessage("====== logout from default server\n");
        bRequestResult = m_clientStub.logoutCM();
        if (bRequestResult)
            printMessage("successfully sent the logout request.\n");
        else
            printStyledMessage("failed the logout request!\n", "bold");
        printMessage("======\n");

        setButtonsAccordingToClientState();
    }

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