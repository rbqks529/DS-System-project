import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerApp m_server;
    private CMServerStub m_serverStub;

    public CMServerEventHandler(CMServerStub serverStub, CMServerApp server) {
        m_server = server;
        m_serverStub = serverStub;
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            default:
                break;
        }
    }

    private void processSessionEvent(CMEvent cme) {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
        switch(se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("[" + se.getUserName() + "] requests login.");
                if(confInfo.isLoginScheme()){
                    boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), m_serverStub.getCMInfo());
                    if(!ret){
                        System.out.println("[" + se.getUserName() + "] authentication fails!");
                        m_serverStub.replyEvent(se, 0);
                    } else {
                        System.out.println("[" + se.getUserName() + "] authentication succeeded.");
                        m_serverStub.replyEvent(se, 1);
                    }
                }
            default:
                return;
        }
    }


}