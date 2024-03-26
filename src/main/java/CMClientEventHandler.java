import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

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
					// Set the appearance of buttons in the client frame window
					m_client.setButtonsAccordingToClientState();
				}
				break;
			case CMSessionEvent.RESPONSE_SESSION_INFO:
				lDelay = System.currentTimeMillis() - m_lStartTime;
				printMessage("RESPONSE_SESSION_INFO delay: "+lDelay+" ms.\n");
				break;
			case CMSessionEvent.SESSION_TALK:
				//System.out.println("("+se.getHandlerSession()+")");
				printMessage("("+se.getHandlerSession()+")\n");
				//System.out.println("<"+se.getUserName()+">: "+se.getTalk());
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
				//lDelay = System.currentTimeMillis() - m_lStartTime;
				//printMessage("ADD_BLOCK_SOCKET_CHANNEL_ACK delay: "+lDelay+" ms.\n");
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
				//lDelay = System.currentTimeMillis() - m_lStartTime;
				//printMessage("REMOVE_BLOCK_SOCKET_CHANNEL_ACK delay: "+lDelay+" ms.\n");
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
					// user registration succeeded
					//System.out.println("User["+se.getUserName()+"] successfully registered at time["
					//			+se.getCreationTime()+"].");
					printMessage("User["+se.getUserName()+"] successfully registered at time["
							+se.getCreationTime()+"].\n");
				}
				else
				{
					// user registration failed
					//System.out.println("User["+se.getUserName()+"] failed to register!");
					printMessage("User["+se.getUserName()+"] failed to register!\n");
				}
				break;
			case CMSessionEvent.DEREGISTER_USER_ACK:
				if( se.getReturnCode() == 1 )
				{
					// user deregistration succeeded
					//System.out.println("User["+se.getUserName()+"] successfully deregistered.");
					printMessage("User["+se.getUserName()+"] successfully deregistered.\n");
				}
				else
				{
					// user registration failed
					//System.out.println("User["+se.getUserName()+"] failed to deregister!");
					printMessage("User["+se.getUserName()+"] failed to deregister!\n");
				}
				break;
			case CMSessionEvent.FIND_REGISTERED_USER_ACK:
				if( se.getReturnCode() == 1 )
				{
					//System.out.println("User profile search succeeded: user["+se.getUserName()
					//		+"], registration time["+se.getCreationTime()+"].");
					printMessage("User profile search succeeded: user["+se.getUserName()
							+"], registration time["+se.getCreationTime()+"].\n");
				}
				else
				{
					//System.out.println("User profile search failed: user["+se.getUserName()+"]!");
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
		System.out.println("--> dummy msg ["+due.getSender()+"]: "+due.getDummyInfo());
		return;
	}


}
