import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

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
				printMessage("["+de.getUserName()+"] enters group("+de.getHandlerGroup()+") in session("
						+de.getHandlerSession()+").\n");
				break;
			case CMDataEvent.REMOVE_USER:
				printMessage("["+de.getUserName()+"] leaves group("+de.getHandlerGroup()+") in session("
						+de.getHandlerSession()+").\n");
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

		// 도형 정보를 파싱하여 각각의 속성을 추출
		String[] shapeArray = dummyInfo.toString().split(";");
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
				Graphics2D g2d = (Graphics2D) m_client.drawingPanel.getGraphics();
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

	// 16진수 문자열을 Color 객체로 변환하는 메서드
	private Color hexToColor(String hex) {
		if(hex.isEmpty()) {
			return new Color(0, 0, 0, 0);
		}
		return Color.decode(hex);
	}

}