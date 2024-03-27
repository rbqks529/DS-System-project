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
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;
    private JTextPane consoleTextPane = new JTextPane();
    private JButton loginButton = new JButton("Login");
    private JButton logoutButton = new JButton("Logout");


    public class DrawingPanel extends JPanel {
        private StringBuilder shapes;
        private String currentShape;
        private int xBegin, yBegin, xEnd, yEnd;
        private String shapeType = "line";
        private Color lineColor = Color.BLACK; // Default line color
        private Color fillColor = Color.WHITE; // Default fill color
        private int currentThickness = 1; // Default thickness
        private boolean fillShape = false; // Default no fill

        public DrawingPanel() {
            setPreferredSize(new Dimension(600, 400));
            setBackground(Color.WHITE);
            shapes = new StringBuilder();
            currentShape = "line";

            // Add buttons for selecting shapes
            JButton lineButton = new JButton("Line");
            JButton circleButton = new JButton("Circle");
            JButton rectangleButton = new JButton("Rectangle");

            lineButton.addActionListener(e -> currentShape = "line");
            circleButton.addActionListener(e -> currentShape = "circle");
            rectangleButton.addActionListener(e -> currentShape = "rectangle");

            JPanel shapeButtonPanel = new JPanel();
            shapeButtonPanel.add(lineButton);
            shapeButtonPanel.add(circleButton);
            shapeButtonPanel.add(rectangleButton);
            add(shapeButtonPanel, BorderLayout.NORTH);

            // Add buttons for line color and fill color
            JButton lineColorButton = new JButton("Line Color");
            JButton fillColorButton = new JButton("Fill Color");

            lineColorButton.addActionListener(e -> {
                lineColor = JColorChooser.showDialog(this, "Choose Line Color", lineColor);
                repaint();
            });

            fillColorButton.addActionListener(e -> {
                fillColor = JColorChooser.showDialog(this, "Choose Fill Color", fillColor);
                repaint();
            });

            JPanel colorButtonPanel = new JPanel();
            colorButtonPanel.add(lineColorButton);
            colorButtonPanel.add(fillColorButton);
            add(colorButtonPanel, BorderLayout.SOUTH);

            // Add slider for line thickness
            JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
            thicknessSlider.setMajorTickSpacing(1);
            thicknessSlider.setPaintTicks(true);
            thicknessSlider.setPaintLabels(true);
            thicknessSlider.addChangeListener(e -> currentThickness = thicknessSlider.getValue());

            JPanel thicknessPanel = new JPanel();
            thicknessPanel.add(new JLabel("Thickness:"));
            thicknessPanel.add(thicknessSlider);
            add(thicknessPanel, BorderLayout.CENTER);

            // Add mouse event handler
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    xBegin = e.getX();
                    yBegin = e.getY();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    xEnd = e.getX();
                    yEnd = e.getY();
                    String shape = currentShape + "," + xBegin + "," + yBegin + "," + xEnd + "," + yEnd + ","
                            + colorToHex(lineColor) + "," + colorToHex(fillColor) + "," + currentThickness + ","
                            + fillShape + ";";
                    shapes.append(shape);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
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

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(lc);
                    g2d.setStroke(new BasicStroke(thickness));
                    switch (type) {
                        case "line":
                            g2d.drawLine(x1, y1, x2, y2);
                            break;
                        case "circle":
                            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                            g2d.setColor(fc);
                            g2d.fillOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
                            g2d.setColor(lc);
                            g2d.drawOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
                            break;
                        case "rectangle":
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

        private String colorToHex(Color color) {
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }

        private Color hexToColor(String hex) {
            return Color.decode(hex);
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

        printMessage("====== test CMDummyEvent in current group\n");

        if(message == null) return;


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