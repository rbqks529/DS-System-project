import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;

    public class DrawingPanel extends JPanel {
        private String shapes; // 그려진 도형들을 문자열로 저장
        private String currentShape; // 현재 그리는 도형
        private int xBegin, yBegin, xEnd, yEnd;
        private String shapeType = "line"; // 기본 도형은 선

        public DrawingPanel() {
            setPreferredSize(new Dimension(600, 400));
            setBackground(Color.WHITE);
            shapes = ""; // 초기화
            currentShape = "line"; // 기본 도형은 선

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
                    // 새로운 도형을 shapes에 추가하고 다시 그리기
                    shapes += String.format("%s,%d,%d,%d,%d;", currentShape, xBegin, yBegin, xEnd, yEnd);
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

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(lineButton);
            buttonPanel.add(circleButton);
            buttonPanel.add(rectangleButton);

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

                    switch (type) {
                        case "line":
                            g.drawLine(x1, y1, x2, y2);
                            break;
                        case "circle":
                            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                            g.drawOval(x1 - radius, y1 - radius, radius * 2, radius * 2);
                            break;
                        case "rectangle":
                            // 계산된 좌표값을 사용하여 사각형 그리기
                            int width = Math.abs(x2 - x1);
                            int height = Math.abs(y2 - y1);
                            int startX = Math.min(x1, x2);
                            int startY = Math.min(y1, y2);

                            g.drawRect(startX, startY, width, height);
                            break;
                    }
                }
            }
        }
    }

    public CMClientApp() {
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub);
    }

    public CMClientStub getClientStub() {
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }

    private void displayLoginDialog() {
        JFrame frame = new JFrame("CM Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new GridLayout(3, 2));

        JLabel lbUsername = new JLabel("Username: ");
        JTextField tfUsername = new JTextField();
        JLabel lbPassword = new JLabel("Password: ");
        JPasswordField pfPassword = new JPasswordField();
        JButton btnLogin = new JButton("Login");
        JButton btnCancel = new JButton("Cancel");

        frame.add(lbUsername);
        frame.add(tfUsername);
        frame.add(lbPassword);
        frame.add(pfPassword);
        frame.add(btnLogin);
        frame.add(btnCancel);

        // 버튼 액션 리스너
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = tfUsername.getText();
                String password = new String(pfPassword.getPassword());

                // CM에 로그인 시도
                boolean ret = m_clientStub.loginCM(username, password);
                // 로그인 버튼 액션 내부에서 로그인 성공 시 처리
                if (ret) {
                    // 로그인 성공 메시지 창
                    JOptionPane.showMessageDialog(frame, "Login successful.");
                    frame.dispose(); // 로그인 창 닫기

                    // 공유 화이트 보드 프레임을 생성 및 표시하는 코드
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // 공유 화이트 보드 프레임 생성
                            JFrame whiteboardFrame = new JFrame("공유 화이트 보드");
                            whiteboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            whiteboardFrame.setSize(1000, 800);

                            // 도형 그리는 패널 생성
                            DrawingPanel drawingPanel = new DrawingPanel();
                            whiteboardFrame.add(drawingPanel, BorderLayout.CENTER);

                            // 콘솔을 표시하는 패널 생성
                            JPanel consolePanel = new JPanel();
                            JTextArea consoleTextArea = new JTextArea(10, 70);
                            consoleTextArea.setEditable(true); // 콘솔 편집 불가능하게 설정
                            JScrollPane scrollPane = new JScrollPane(consoleTextArea);
                            consolePanel.add(scrollPane);
                            whiteboardFrame.add(consolePanel, BorderLayout.SOUTH);

                            // 프레임 위치 설정 및 표시
                            whiteboardFrame.setLocationRelativeTo(null); // 화면 중앙에 위치
                            whiteboardFrame.setVisible(true);
                        }
                    });
                } else {
                    // 로그인 실패 메시지 창
                    JOptionPane.showMessageDialog(frame, "Login failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 취소 버튼 액션
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        frame.setLocationRelativeTo(null); // 창을 화면 중앙에 배치
        frame.setVisible(true);
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
        // GUI 로그인 창 표시
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                client.displayLoginDialog();
            }
        });
    }
}