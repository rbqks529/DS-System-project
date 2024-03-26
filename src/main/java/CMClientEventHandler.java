import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMAppEventHandler {
    private CMClientStub clientStub;
	private long m_lStartTime;
	public void setStartTime(long time)
	{
		m_lStartTime = time;
	}

	public long getStartTime()
	{
		return m_lStartTime;
	}

    public CMClientEventHandler(CMClientStub clientStub) {
        this.clientStub = clientStub;
    }

    @Override
    public void processEvent(CMEvent cme) {
		switch(cme.getType())
		{
			case CMInfo.CM_SESSION_EVENT:
				processSessionEvent(cme);
				break;
			default:
				break;
		}
    }

	private void processSessionEvent(CMEvent cme) {
		CMSessionEvent se = (CMSessionEvent)cme;
		switch(se.getID())
		{
		case CMSessionEvent.LOGIN_ACK:
			if(se.isValidUser() == 0)
			{
				System.err.println("--> This client fails authentication by the default server!");
			}
			else if(se.isValidUser() == -1)
			{
				System.err.println("--> This client is already in the login-user list!");
			}
			else
			{
				System.out.println("--> This client successfully logs in to the default server.");
			}
			break;
		default:
			break;
		}
	}

}
