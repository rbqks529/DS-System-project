import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class CMClientApp {
    public DrawingPanel drawingPanel = new DrawingPanel();
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;
    private JTextPane consoleTextPane = new JTextPane();
    private JButton loginButton = new JButton("Login");
    private JButton logoutButton = new JButton("Logout");
    private JButton saveButton = new JButton("Save");
    private JButton loadButton = new JButton("Load");
    private boolean loggedIn = false;
    private JToggleButton customizeButton;
    private boolean customizeMode = false;

    // Drawing panel for shapes
    public class DrawingPanel extends JPanel {
        // ArrayList to store shapes
        public ArrayList<Shape> shapesList = new ArrayList<>();
        public String currentShape;
        public int xBegin, yBegin, xEnd, yEnd;
        public Color lineColor = Color.BLACK;
        public Color fillColor = null;
        private String inputText = null;
        public int Thickness = 1;
        public int currentThickness = 1;
        public boolean fillShape = false;
        public boolean currentFillShape = false;
        public Color currentFillColor = null;
        public Color currentlineColor = Color.BLACK;
        private FontMetrics fontMetrics;
        public Shape selectedShape; // 선택된 Shape 객체를 저장할 변수
        private long lastEventTime = 0; // 마지막으로 이벤트를 전송한 시간을 저장할 변수
        private static final int EVENT_INTERVAL = 10; // 이벤트 전송 간격 (밀리초)


        public DrawingPanel() {
            setPreferredSize(new Dimension(600, 400));
            setBackground(Color.WHITE);
            currentShape = "line";
            fillColor = null;
            Font font = new Font("돋움체", Font.CENTER_BASELINE, 40);
            fontMetrics = getFontMetrics(font);

            // Buttons for selecting shapes
            JButton lineButton = new JButton("Line");
            JButton circleButton = new JButton("Circle");
            JButton rectangleButton = new JButton("Rectangle");
            JButton textButton = new JButton("Text");

            // Action listeners for each button
            lineButton.addActionListener(e -> currentShape = "line");
            circleButton.addActionListener(e -> currentShape = "circle");
            rectangleButton.addActionListener(e -> currentShape = "rectangle");
            textButton.addActionListener(e -> currentShape = "text");

            customizeButton = new JToggleButton("Customize");
            customizeButton.addActionListener(e -> {
                if (loggedIn) { // 로그인한 상태일 때만 CustomizeButton 동작
                    customizeMode = customizeButton.isSelected();

                    selectedShape = null;

                    if (customizeMode) {
                        testDummyEvent("CUSTOMIZE_MODE_ENABLED");
                    } else {
                        testDummyEvent("CUSTOMIZE_MODE_DISABLED");
                    }
                } else {
                    customizeButton.setSelected(false); // 로그인하지 않았을 때는 CustomizeButton 선택 해제
                }
            });


            JPanel shapeButtonPanel = new JPanel();
            shapeButtonPanel.add(lineButton);
            shapeButtonPanel.add(circleButton);
            shapeButtonPanel.add(rectangleButton);
            shapeButtonPanel.add(textButton);
            add(shapeButtonPanel, BorderLayout.NORTH);
            shapeButtonPanel.add(customizeButton);

            // Buttons for selecting line and fill colors
            JButton lineColorButton = new JButton("Line & Text Color");
            JButton fillColorButton = new JButton("Fill Color");

            // lineColorButton, fillColorButton 리스너 수정
            lineColorButton.addActionListener(e -> {
                if (customizeMode && selectedShape != null) {
                    lineColor = JColorChooser.showDialog(this, "Choose Line Color", lineColor);
                    selectedShape.setLineColor(lineColor);
                    repaint();
                    testDummyEvent("전송");
                } else {
                    lineColor = JColorChooser.showDialog(this, "Choose Line Color", lineColor);
                }
            });

            fillColorButton.addActionListener(e -> {
                if (customizeMode && selectedShape != null) {
                    Color newFillColor = JColorChooser.showDialog(this, "Choose Fill Color", fillColor);
                    if (newFillColor != null) {
                        fillColor = newFillColor;
                        selectedShape.setFillColor(fillColor);
                        selectedShape.setFillShape(true);
                        repaint();
                        testDummyEvent("전송");
                    }
                } else {
                    Color newFillColor = JColorChooser.showDialog(this, "Choose Fill Color", fillColor);
                    if (newFillColor != null) {
                        fillColor = newFillColor;
                        fillShape = true;
                    }
                }
            });

            JPanel colorButtonPanel = new JPanel();
            colorButtonPanel.add(lineColorButton);
            colorButtonPanel.add(fillColorButton);
            add(colorButtonPanel, BorderLayout.SOUTH);

            Integer[] thicknessOptions = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}; // Thickness options
            JComboBox<Integer> thicknessComboBox = new JComboBox<>(thicknessOptions);
            thicknessComboBox.setSelectedItem(Thickness);

            thicknessComboBox.addActionListener(e ->  {

                if (customizeMode && selectedShape != null) {
                    // Update selected shape's thickness
                    int newThickness = (int) thicknessComboBox.getSelectedItem();
                    selectedShape.setThickness(newThickness);
                    repaint();
                    testDummyEvent("전송");
                } else {
                    Thickness = (int) thicknessComboBox.getSelectedItem();
                }

            });

            JPanel thicknessPanel = new JPanel();
            thicknessPanel.add(new JLabel("Thickness:"));
            thicknessPanel.add(thicknessComboBox);
            add(thicknessPanel, BorderLayout.CENTER);
            // Mouse event handlers
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!loggedIn)
                        return;

                    if (customizeMode) {
                        Point p = e.getPoint();
                        selectedShape = getShapeAtPoint(p);
                        repaint();

                    } else {
                        if (currentShape.equals("text")) {
                            xBegin = e.getX();
                            yBegin = e.getY();
                            inputText = JOptionPane.showInputDialog("Enter text:");
                            if (inputText != null && !inputText.isEmpty()) {
                                Shape shape = new Shape(currentShape, xBegin, yBegin, xEnd, yEnd,
                                        lineColor, fillColor, Thickness, fillShape, inputText);
                                shapesList.add(shape);
                                repaint();
                                testDummyEvent("도형 전송");
                            }
                        } else {
                            xBegin = e.getX();
                            yBegin = e.getY();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!loggedIn)
                        return;

                    if (!customizeMode) {
                        xEnd = e.getX();
                        yEnd = e.getY();
                        Shape shape = new Shape(currentShape, xBegin, yBegin, xEnd, yEnd,
                                lineColor, fillColor, Thickness, fillShape, inputText);
                        shapesList.add(shape);
                        repaint();
                        testDummyEvent("도형 전송");
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!loggedIn)
                        return;

                    if (!customizeMode) {
                        xEnd = e.getX();
                        yEnd = e.getY();

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastEventTime >= EVENT_INTERVAL) {
                            // 이벤트 전송
                            testDummyEvent("DRAW|" + currentShape + "|" + xBegin + "|" + yBegin + "|" + e.getX() + "|" + e.getY() + "|" + lineColor.getRGB() + "|" + (fillShape ? fillColor.getRGB() : 0) + "|" + Thickness);
                            lastEventTime = currentTime; // 마지막 이벤트 전송 시간 업데이트
                        }

                    }
                }
            });


        }
        private Shape getShapeAtPoint(Point p) {
            for (Shape shape : shapesList) {
                if (shape.contains(p, fontMetrics)) {
                    return shape;
                }
            }
            return null;
        }

        // shapesList를 텍스트 파일로 저장하는 메서드
        public void saveShapesToFile(String fileName) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                for (Shape shape : shapesList) {
                    writer.write(shape.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 텍스트 파일에서 shapesList를 로드하는 메서드
        public void loadShapesFromFile(String fileName) {
            shapesList.clear(); // 기존 shapesList 초기화
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Shape shape = Shape.createShapeFromString(line);
                    shapesList.add(shape);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            repaint(); // 패널 다시 그리기
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            for (Shape shape : shapesList) {
                g2d.setColor(shape.getLineColor());
                g2d.setStroke(new BasicStroke(shape.getThickness()));

                // 선택된 Shape일 경우 더 굵은 선으로 그리기
                if (shape == selectedShape) {
                    g2d.setStroke(new BasicStroke(shape.getThickness() + 2));
                    g2d.setColor(Color.RED); // 선택된 Shape의 테두리 색상을 빨간색으로 설정
                }


                switch (shape.getType()) {
                    case "line":
                        g2d.drawLine(shape.getStartPoint().x, shape.getStartPoint().y,
                                shape.getEndPoint().x, shape.getEndPoint().y);
                        break;
                    case "circle":
                        int radius = (int) Math.sqrt(Math.pow(shape.getEndPoint().x - shape.getStartPoint().x, 2) +
                                Math.pow(shape.getEndPoint().y - shape.getStartPoint().y, 2));
                        if (shape.isFill()) {
                            g2d.setColor(shape.getFillColor());
                            g2d.fillOval(shape.getStartPoint().x - radius, shape.getStartPoint().y - radius, radius * 2, radius * 2);
                        }
                        g2d.setColor(shape.getLineColor());
                        g2d.drawOval(shape.getStartPoint().x - radius, shape.getStartPoint().y - radius, radius * 2, radius * 2);
                        break;
                    case "rectangle":
                        int width = Math.abs(shape.getEndPoint().x - shape.getStartPoint().x);
                        int height = Math.abs(shape.getEndPoint().y - shape.getStartPoint().y);
                        int startX = Math.min(shape.getStartPoint().x, shape.getEndPoint().x);
                        int startY = Math.min(shape.getStartPoint().y, shape.getEndPoint().y);
                        if (shape.isFill()) {
                            g2d.setColor(shape.getFillColor());
                            g2d.fillRect(startX, startY, width, height);
                        }
                        g2d.setColor(shape.getLineColor());
                        g2d.drawRect(startX, startY, width, height);
                        break;
                    case "text":
                        g2d.setFont(new Font("돋움체", Font.CENTER_BASELINE, 40));
                        FontMetrics fontMetrics = g2d.getFontMetrics();
                        g2d.drawString(shape.getText(), shape.getStartPoint().x, shape.getStartPoint().y); // Text position adjusted to be centered
                        break;
                }
            }
            // 현재 그리고 있는 도형을 그립니다.
            if (currentShape != null) {
                g2d.setColor(currentlineColor );
                g2d.setStroke(new BasicStroke(currentThickness));

                switch (currentShape) {
                    case "line":
                        g2d.drawLine(xBegin, yBegin, xEnd, yEnd);
                        break;
                    case "circle":
                        int radius = (int) Math.sqrt(Math.pow(xEnd - xBegin, 2) + Math.pow(yEnd - yBegin, 2));
                        if (currentFillShape) {
                            g2d.setColor(currentFillColor);
                            g2d.fillOval(xBegin - radius, yBegin - radius, 2 * radius, 2 * radius);
                        }
                        g2d.setColor(currentlineColor );
                        g2d.drawOval(xBegin - radius, yBegin - radius, 2 * radius, 2 * radius);
                        break;
                    case "rectangle":
                        int width = Math.abs(xEnd - xBegin);
                        int height = Math.abs(yEnd - yBegin);
                        int startX = Math.min(xBegin, xEnd);
                        int startY = Math.min(yBegin, yEnd);
                        if (currentFillShape) {
                            g2d.setColor(currentFillColor);
                            g2d.fillRect(startX, startY, width, height);
                        }
                        g2d.setColor(currentlineColor );
                        g2d.drawRect(startX, startY, width, height);
                        break;
                }
            }
        }

        public void setdisableCustomizeButton (boolean enabled) {
            customizeButton.setEnabled(enabled);
            // customizeMode = false;
        }

        public void setenableCustomizeButton() {
            if (customizeButton != null) {
                customizeButton.setEnabled(true);
                // customizeMode = true;
            }
        }
        public boolean getCustomizeMode(){
            if (customizeButton.isSelected()) {
                return true;
            } else {
                return false;
            }
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
        whiteboardFrame.setSize(900, 800);

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

        saveButton.setEnabled(false);
        saveButton.setPreferredSize(new Dimension(70, 30));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // shapesList 내용을 텍스트 파일로 저장
                drawingPanel.saveShapesToFile("shapes.txt");
                printMessage("저장\n");
            }
        });

        loadButton.setEnabled(false);
        loadButton.setPreferredSize(new Dimension(70, 30));
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 텍스트 파일에서 shapesList 내용 로드
                drawingPanel.loadShapesFromFile("shapes.txt");
                printMessage("불러오기\n");
                testDummyEvent("DRAW|" + null+ "|" + 0 + "|" + 0+ "|" + 0+ "|" + 0 + "|" + 0 + "|" + 0 + "|" + 0);
                testDummyEvent("동기화");
            }
        });

        buttonPanel.add(saveButton, BorderLayout.WEST);
        buttonPanel.add(loadButton, BorderLayout.WEST);
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

                // 로그인 상태 설정
                loggedIn = true;

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


    //로그아웃 함수
    private void logout() {

        // custombutton을 누른 채로 로그아웃 했을 때
        if(customizeButton.isSelected()) {
            testDummyEvent("CUSTOMIZE_MODE_DISABLED");
        }
        // 일반적으로 로그아웃하였을 때 custombutton을 초기화 시켜줌
        customizeButton.setEnabled(true);
        customizeButton.setSelected(false);
        customizeMode = false;


        boolean bRequestResult = false;
        printMessage("====== logout from default server\n");
        bRequestResult = m_clientStub.logoutCM();
        if (bRequestResult) {
            // 로그인 상태 설정
            loggedIn = false;
            printMessage("successfully sent the logout request.\n");
        }else
            printStyledMessage("failed the logout request!\n", "bold");
        printMessage("======\n");


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
                saveButton.setEnabled(false);
                loadButton.setEnabled(false);
                logoutButton.setEnabled(false);
                break;
            case CMInfo.CM_CONNECT: //로그인 버튼
                loginButton.setEnabled(true);
                saveButton.setEnabled(false);
                loadButton.setEnabled(false);
                logoutButton.setEnabled(false);
                break;
            case CMInfo.CM_LOGIN: //로그아웃 버튼
                loginButton.setEnabled(false);
                saveButton.setEnabled(true);
                loadButton.setEnabled(true);
                logoutButton.setEnabled(true);
                break;
            case CMInfo.CM_SESSION_JOIN:    //로그아웃 버튼
                loginButton.setEnabled(false);
                saveButton.setEnabled(true);
                loadButton.setEnabled(true);
                logoutButton.setEnabled(true);
                break;
            default:    //로그인 버튼
                loginButton.setEnabled(true);
                saveButton.setEnabled(false);
                loadButton.setEnabled(false);
                logoutButton.setEnabled(false);
                break;
        }
    }

    //메세지를 보내기 위한 더비 이벤트 함수
    private void testDummyEvent(String message) {
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();

        if (myself.getState() != CMInfo.CM_SESSION_JOIN) {
            printMessage("You should join a session and a group!\n");
            return;
        }

        if (message == null)
            return;

        CMDummyEvent due = new CMDummyEvent();
        due.setHandlerSession(myself.getCurrentSession());
        due.setHandlerGroup(myself.getCurrentGroup());

        if (message.startsWith("DRAW|")) {
            // 그림이 그려지는 과정일 경우
            due.setDummyInfo(message);
        }
        else if(message.equals("CUSTOMIZE_MODE_ENABLED")){
            printMessage("CustomizeButton select\n");
            due.setDummyInfo(message);
        }
        else if(message.equals("CUSTOMIZE_MODE_DISABLED")){
            printMessage("CustomizeButton unselect\n");
            due.setDummyInfo(message);
        }
        else {
            // 최종 그림 정보일 경우
            StringBuilder shapeListString = new StringBuilder();
            for (Shape shape : drawingPanel.shapesList) {
                shapeListString.append(shape.toString()).append("|");
            }
            due.setDummyInfo(shapeListString.toString());
        }

        m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
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