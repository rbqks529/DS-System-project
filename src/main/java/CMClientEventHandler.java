import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMEventHandler {
    private CMClientStub m_clientStub;


    @Override
    public boolean processEvent(CMEvent cmEvent) {
        return false;
    }
}
