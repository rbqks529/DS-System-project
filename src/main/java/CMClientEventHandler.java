import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.util.ArrayList;
import java.awt.*;

public class CMClientEventHandler implements CMAppEventHandler {
	private CMClientStub m_clientStub;
	private CMClientApp m_client;
	private long m_lStartTime;

	public void setStartTime(long time)
	{
		m_lStartTime = time;
	}

	public long getStartTime()
	{
		return m_lStartTime;
	}

	public CMClientEventHandler(CMClientStub clientStub, CMClientApp client) {
		m_client = client;
		m_clientStub = clientStub;
		m_lStartTime = 0;

	}
	@Override
	public void processEvent(CMEvent cme) {
		switch(cme.getType())
		{
			case CMInfo.CM_SESSION_EVENT:
				processSessionEvent(cme);
				break;
			case CMInfo.CM_DUMMY_EVENT:
				processDummyEvent(cme);
				break;
			case CMInfo.CM_DATA_EVENT:
				processDataEvent(cme);
				break;
		}
	}

	private void processDataEvent(CMEvent cme)
	{
		CMDataEvent de = (CMDataEvent) cme;
		switch(de.getID())
		{
			case CMDataEvent.NEW_USER:
				printMessage("["+de.getUserName()+"] enters WhiteBoard group\n");

				CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
				// 로그인 성공 후 기존 클라이언트의 그림 정보를 요청
				CMDummyEvent due = new CMDummyEvent();
				due.setHandlerSession(interInfo.getMyself().getCurrentSession());
				due.setHandlerGroup(interInfo.getMyself().getCurrentGroup());
				due.setDummyInfo("request");
				m_clientStub.cast(due, interInfo.getMyself().getCurrentSession(), interInfo.getMyself().getCurrentGroup());

				break;
			case CMDataEvent.REMOVE_USER:
				printMessage("["+de.getUserName()+"] leaves WhiteBoard group\n");
				break;
			default:
				return;
		}
	}
	private void processSessionEvent(CMEvent cme) {
		long lDelay = 0;
		CMSessionEvent se = (CMSessionEvent)cme;
		switch(se.getID())
		{
			case CMSessionEvent.LOGIN_ACK:
				lDelay = System.currentTimeMillis() - m_lStartTime;
				printMessage("LOGIN_ACK delay: "+lDelay+" ms.\n");

				if(se.isValidUser() == 0)
				{
					printMessage("This client fails authentication by the default server!\n");
				}
				else if(se.isValidUser() == -1)
				{
					printMessage("This client is already in the login-user list!\n");
				}
				else
				{
					printMessage("This client successfully logs in to the default server.\n");
					CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
					m_client.setButtonsAccordingToClientState();

					// 로그인 성공 후 기존 클라이언트의 그림 정보를 요청
					CMDummyEvent due = new CMDummyEvent();
					due.setHandlerSession(interInfo.getMyself().getCurrentSession());
					due.setHandlerGroup(interInfo.getMyself().getCurrentGroup());
					due.setDummyInfo("request");
					m_clientStub.cast(due, interInfo.getMyself().getCurrentSession(), interInfo.getMyself().getCurrentGroup());
				}
				break;
			case CMSessionEvent.RESPONSE_SESSION_INFO:
				lDelay = System.currentTimeMillis() - m_lStartTime;
				printMessage("RESPONSE_SESSION_INFO delay: "+lDelay+" ms.\n");
				break;
			case CMSessionEvent.SESSION_TALK:
				printMessage("("+se.getHandlerSession()+")\n");
				printMessage("<"+se.getUserName()+">: "+se.getTalk()+"\n");
				break;
			case CMSessionEvent.JOIN_SESSION_ACK:
				lDelay = System.currentTimeMillis() - m_lStartTime;
				printMessage("JOIN_SESSION_ACK delay: "+lDelay+" ms.\n");
				m_client.setButtonsAccordingToClientState();
				break;
			case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
				if(se.getReturnCode() == 0)
				{
					printMessage("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
							+") failed at the server!\n");
				}
				else
				{
					printMessage("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
							+") succeeded at the server!\n");
				}
				break;
			case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
				if(se.getReturnCode() == 0)
				{
					printMessage("Adding a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
							+") failed at the server!\n");
				}
				else
				{
					printMessage("Adding a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
							+") succeeded at the server!\n");
				}
				break;
			case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
				if(se.getReturnCode() == 0)
				{
					printMessage("Removing a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
							+") failed at the server!\n");
				}
				else
				{
					printMessage("Removing a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
							+") succeeded at the server!\n");
				}
				break;
			case CMSessionEvent.REGISTER_USER_ACK:
				if( se.getReturnCode() == 1 )
				{
					printMessage("User["+se.getUserName()+"] successfully registered at time["
							+se.getCreationTime()+"].\n");
				}
				else
				{
					printMessage("User["+se.getUserName()+"] failed to register!\n");
				}
				break;
			case CMSessionEvent.DEREGISTER_USER_ACK:
				if( se.getReturnCode() == 1 )
				{
					printMessage("User["+se.getUserName()+"] successfully deregistered.\n");
				}
				else
				{
					printMessage("User["+se.getUserName()+"] failed to deregister!\n");
				}
				break;
			case CMSessionEvent.FIND_REGISTERED_USER_ACK:
				if( se.getReturnCode() == 1 )
				{
					printMessage("User profile search succeeded: user["+se.getUserName()
							+"], registration time["+se.getCreationTime()+"].\n");
				}
				else
				{
					printMessage("User profile search failed: user["+se.getUserName()+"]!\n");
				}
				break;
			case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
				m_client.printStyledMessage("Unexpected disconnection from ["
						+se.getChannelName()+"] with key["+se.getChannelNum()+"]!\n", "bold");
				m_client.setButtonsAccordingToClientState();
				break;
			case CMSessionEvent.INTENTIONALLY_DISCONNECT:
				m_client.printStyledMessage("Intentionally disconnected all channels from ["
						+se.getChannelName()+"]!\n", "bold");
				m_client.setButtonsAccordingToClientState();
				break;
			default:
				return;
		}
	}
	private void printMessage(String strText)
	{
		m_client.printMessage(strText);
	}

	private void processDummyEvent(CMEvent cme) {
		CMDummyEvent due = (CMDummyEvent) cme;
		String dummyInfo = due.getDummyInfo();

		if (dummyInfo.equals("request")) {
			// 그림 정보 요청일 경우, 현재 클라이언트의 그림 정보를 전송
			StringBuilder shapeListString = new StringBuilder();
			for (Shape shape : m_client.drawingPanel.shapesList) {
				shapeListString.append(shape.toString()).append("|");
			}
			CMDummyEvent responseEvent = new CMDummyEvent();
			responseEvent.setHandlerSession(due.getHandlerSession());
			responseEvent.setHandlerGroup(due.getHandlerGroup());
			responseEvent.setDummyInfo(shapeListString.toString());
			m_clientStub.cast(responseEvent, due.getHandlerSession(), due.getHandlerGroup());

		} else if (dummyInfo.startsWith("DRAW|")) {
			// 그림이 그려지는 과정일 경우
			String[] parts = dummyInfo.split("\\|");
			String shapeType = parts[1];
			int xBegin = Integer.parseInt(parts[2]);
			int yBegin = Integer.parseInt(parts[3]);
			int xEnd = Integer.parseInt(parts[4]);
			int yEnd = Integer.parseInt(parts[5]);
			Color lineColor = new Color(Integer.parseInt(parts[6]));
			Color fillColor = new Color(Integer.parseInt(parts[7]));
			int thickness = Integer.parseInt(parts[8]);

			// 현재 클라이언트의 drawingPanel 초기화
			m_client.drawingPanel.repaint();

			// 현재 그려지고 있는 도형 그리기
			Graphics2D g2d = (Graphics2D) m_client.drawingPanel.getGraphics();
			g2d.setColor(lineColor);
			g2d.setStroke(new BasicStroke(thickness));

			switch (shapeType) {
				case "line":
					g2d.drawLine(xBegin, yBegin, xEnd, yEnd);
					break;
				case "circle":
					int radius = (int) Math.sqrt(Math.pow(xEnd - xBegin, 2) + Math.pow(yEnd - yBegin, 2));
					if (fillColor.getRGB() != 0) {
						g2d.setColor(fillColor);
						g2d.fillOval(xBegin - radius, yBegin - radius, radius * 2, radius * 2);
						g2d.setColor(lineColor);
					}
					g2d.drawOval(xBegin - radius, yBegin - radius, radius * 2, radius * 2);
					break;
				case "rectangle":
					int width = Math.abs(xEnd - xBegin);
					int height = Math.abs(yEnd - yBegin);
					int startX = Math.min(xBegin, xEnd);
					int startY = Math.min(yBegin, yEnd);
					if (fillColor.getRGB() != 0) {
						g2d.setColor(fillColor);
						g2d.fillRect(startX, startY, width, height);
						g2d.setColor(lineColor);
					}
					g2d.drawRect(startX, startY, width, height);
					break;
			}

			try {
				Thread.sleep(10); // 10 밀리초 대기
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		} else {
			// 파이프 문자(|)로 분리하여 Shape 객체 생성
			String[] shapeStrings = dummyInfo.split("\\|");
			ArrayList<Shape> shapeList = new ArrayList<>();
			for (String shapeString : shapeStrings) {
				if (!shapeString.isEmpty()) {
					shapeList.add(Shape.createShapeFromString(shapeString));
				}
			}

			// 클라이언트의 drawingPanel에 shapeList 적용
			m_client.drawingPanel.shapesList = shapeList;
			m_client.drawingPanel.repaint();
		}
	}

}