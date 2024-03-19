import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;

    public CMClientApp(){
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub);
    }

    public CMClientStub getClientStub(){
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler(){
        return m_eventHandler;
    }

    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);
        CMClientApp client = new CMClientApp();
        CMClientStub clientStub = client.getClientStub();
        CMClientEventHandler eventHandler = client.getClientEventHandler();
        boolean ret = false;

        //initalize CM
        clientStub.setAppEventHandler(eventHandler);
        ret = clientStub.startCM();

        if(ret){
            System.out.println("init success");
        } else {
            System.err.println("init error!");
            return;
        }

        //login CM server
        System.out.println("user name: ccslab");
        System.out.println("password: ccslab");
        ret = clientStub.loginCM("ccslab", "ccslab");

        if(ret){
            System.out.println("successfully sent the login request.");
        } else {
            System.err.println("failed the login request!");
            return;
        }

        //wait before excuting next API
        System.out.println("Press enter to excute net API:");
        scanner.nextLine();

    }
}
