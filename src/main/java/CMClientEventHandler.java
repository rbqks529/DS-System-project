import kr.ac.konkuk.ccslab.cm.entity.CMUser;
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

	public void setStartTime(long time) {
		m_lStartTime = time;
	}

	public long getStartTime() {
		return m_lStartTime;
	}

	public CMClientEventHandler(CMClientStub clientStub, CMClientApp client) {
		m_client = client;
		m_clientStub = clientStub;
		m_lStartTime = 0;

	}

	@Override
	public void processEvent(CMEvent cme) {
		switch (cme.getType()) {
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

	private void processDataEvent(CMEvent cme) {
		CMDataEvent de = (CMDataEvent) cme;
		switch (de.getID()) {
			case CMDataEvent.NEW_USER:
				printMessage("[" + de.getUserName() + "] enters WhiteBoard group\n");

				CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
				// 로그인 성공 후 기존 클라이언트의 그림 정보를 요청
				CMDummyEvent due = new CMDummyEvent();
				due.setHandlerSession(interInfo.getMyself().getCurrentSession());
				due.setHandlerGroup(interInfo.getMyself().getCurrentGroup());
				due.setDummyInfo("request");
				m_clientStub.cast(due, interInfo.getMyself().getCurrentSession(), interInfo.getMyself().getCurrentGroup());

				if (m_client.drawingPanel.getCustomizeMode()) {
					CMDummyEvent customizeStatusEvent = new CMDummyEvent();
					customizeStatusEvent.setHandlerSession(interInfo.getMyself().getCurrentSession());
					customizeStatusEvent.setHandlerGroup(interInfo.getMyself().getCurrentGroup());
					customizeStatusEvent.setDummyInfo("CHECK_CUSTOMIZE_STATUS");
					m_clientStub.cast(customizeStatusEvent, interInfo.getMyself().getCurrentSession(), interInfo.getMyself().getCurrentGroup());
				}
				break;
			case CMDataEvent.REMOVE_USER:
				printMessage("[" + de.getUserName() + "] leaves WhiteBoard group\n");
				break;
			default:
				return;
		}
	}

	private void processSessionEvent(CMEvent cme) {
		long lDelay = 0;
		CMSessionEvent se = (CMSessionEvent) cme;
		switch (se.getID()) {
			case CMSessionEvent.LOGIN_ACK:
				lDelay = System.currentTimeMillis() - m_lStartTime;
				printMessage("LOGIN_ACK delay: " + lDelay + " ms.\n");

				if (se.isValidUser() == 0) {
					printMessage("This client fails authentication by the default server!\n");
				} else if (se.isValidUser() == -1) {
					printMessage("This client is already in the login-user list!\n");
				} else {
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
				printMessage("RESPONSE_SESSION_INFO delay: " + lDelay + " ms.\n");
				break;
			case CMSessionEvent.SESSION_TALK:
				printMessage("(" + se.getHandlerSession() + ")\n");
				printMessage("<" + se.getUserName() + ">: " + se.getTalk() + "\n");
				break;
			case CMSessionEvent.JOIN_SESSION_ACK:
				lDelay = System.currentTimeMillis() - m_lStartTime;
				printMessage("JOIN_SESSION_ACK delay: " + lDelay + " ms.\n");
				m_client.setButtonsAccordingToClientState();
				break;
			case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
				if (se.getReturnCode() == 0) {
					printMessage("Adding a nonblocking SocketChannel(" + se.getChannelName() + "," + se.getChannelNum()
							+ ") failed at the server!\n");
				} else {
					printMessage("Adding a nonblocking SocketChannel(" + se.getChannelName() + "," + se.getChannelNum()
							+ ") succeeded at the server!\n");
				}
				break;
			case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
				if (se.getReturnCode() == 0) {
					printMessage("Adding a blocking socket channel (" + se.getChannelName() + "," + se.getChannelNum()
							+ ") failed at the server!\n");
				} else {
					printMessage("Adding a blocking socket channel(" + se.getChannelName() + "," + se.getChannelNum()
							+ ") succeeded at the server!\n");
				}
				break;
			case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
				if (se.getReturnCode() == 0) {
					printMessage("Removing a blocking socket channel (" + se.getChannelName() + "," + se.getChannelNum()
							+ ") failed at the server!\n");
				} else {
					printMessage("Removing a blocking socket channel(" + se.getChannelName() + "," + se.getChannelNum()
							+ ") succeeded at the server!\n");
				}
				break;
			case CMSessionEvent.REGISTER_USER_ACK:
				if (se.getReturnCode() == 1) {
					printMessage("User[" + se.getUserName() + "] successfully registered at time["
							+ se.getCreationTime() + "].\n");
				} else {
					printMessage("User[" + se.getUserName() + "] failed to register!\n");
				}
				break;
			case CMSessionEvent.DEREGISTER_USER_ACK:
				if (se.getReturnCode() == 1) {
					printMessage("User[" + se.getUserName() + "] successfully deregistered.\n");
				} else {
					printMessage("User[" + se.getUserName() + "] failed to deregister!\n");
				}
				break;
			case CMSessionEvent.FIND_REGISTERED_USER_ACK:
				if (se.getReturnCode() == 1) {
					printMessage("User profile search succeeded: user[" + se.getUserName()
							+ "], registration time[" + se.getCreationTime() + "].\n");
				} else {
					printMessage("User profile search failed: user[" + se.getUserName() + "]!\n");
				}
				break;
			case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
				m_client.printStyledMessage("Unexpected disconnection from ["
						+ se.getChannelName() + "] with key[" + se.getChannelNum() + "]!\n", "bold");
				m_client.setButtonsAccordingToClientState();
				break;
			case CMSessionEvent.INTENTIONALLY_DISCONNECT:
				m_client.printStyledMessage("Intentionally disconnected all channels from ["
						+ se.getChannelName() + "]!\n", "bold");
				m_client.setButtonsAccordingToClientState();

				break;
			default:
				return;
		}
	}

	private void printMessage(String strText) {
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

			m_client.drawingPanel.currentShape = shapeType;
			m_client.drawingPanel.currentThickness = thickness;
			m_client.drawingPanel.lineColor = lineColor;
			if (parts[7].equals("0")) {
				m_client.drawingPanel.fillShape = false;
			} else {
				m_client.drawingPanel.fillColor = fillColor;
				m_client.drawingPanel.fillShape = true;
			}
			m_client.drawingPanel.xBegin = xBegin;
			m_client.drawingPanel.yBegin = yBegin;
			m_client.drawingPanel.xEnd = xEnd;
			m_client.drawingPanel.yEnd = yEnd;

			m_client.drawingPanel.repaint();
			// 자기는 enable이니 다른 클라이언트를 disable 시키기
		} else if (dummyInfo.equals("CUSTOMIZE_MODE_ENABLED")) {
			if (!due.getSender().equals(m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName())) {
				m_client.drawingPanel.setdisableCustomizeButton(false);
			}
			// 자기가 disable이니 다른 클라리언트를 enable 시키기
		} else if (dummyInfo.equals("CUSTOMIZE_MODE_DISABLED")) {
			// Customize 모드 비활성화 시, customize 버튼 활성화
			m_client.drawingPanel.setenableCustomizeButton();
		} else if (dummyInfo.equals("CHECK_CUSTOMIZE_STATUS")) {
			// 현재 클라이언트의 CustomizeButton 상태 전송
			if (!due.getSender().equals(m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName())) {
				m_client.drawingPanel.setdisableCustomizeButton(false);
			}
		}else {
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
