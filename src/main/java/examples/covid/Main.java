package examples.covid;

import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.CloudHub;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    static final String DKMS_NAME = "test_network";
    static final String COVID_MICROLEDGER_NAME = "covid_ledger_test3";

    static CloudHub.Config steward = new CloudHub.Config();
    static CloudHub.Config labConfig = new CloudHub.Config();
    static CloudHub.Config airCompanyConfig = new CloudHub.Config();
    static CloudHub.Config airportConfig = new CloudHub.Config();

    static final String LAB_DID = "X1YdguoHBaY1udFQMbbKKG";
    static final String AIRCOMPANY_DID = "XwVCkzM6sMxk87M2GKtya6";
    static final String AIRPORT_DID = "Ap29nQ3Kf2bGJdWEV3m4AG";

    static final Entity labEntity = new Entity(
            "Laboratory",
            "",
            "HMf57wiWK1FhtzLbm76o37tEMJvaCbWfGsaUzCZVZwnT",
            "X1YdguoHBaY1udFQMbbKKG");

    static final Entity aircompanyEntity = new Entity(
            "AirCompany",
            "",
            "Hs4FPfB1d7nFUcqbMZqofFg4qoeGxGThmSbunJYpVAM6",
            "XwVCkzM6sMxk87M2GKtya6");

    static final Entity airportEntity = new Entity(
            "Airport",
            "",
            "6M8qgMdkqGzQ2yhryV3F9Kvk785qAFny5JuLp1CJCcHW",
            "Ap29nQ3Kf2bGJdWEV3m4AG");

    static {
        steward.serverUri = "https://demo.socialsirius.com";
        steward.credentials = "ez8ucxfrTiV1hPX99MHt/C/MUJCo8OmN4AMVmddE/sew8gBzsOg040FWBSXzHd9hDoj5B5KN4aaLiyzTqkrbD3uaeSwmvxVsqkC0xl5dtIc=".getBytes(StandardCharsets.UTF_8);
        steward.p2p = new P2PConnection(
                "6QvQ3Y5pPMGNgzvs86N3AQo98pF5WrzM1h6WkKH3dL7f",
                "28Au6YoU7oPt6YLpbWkzFryhaQbfAcca9KxZEmz22jJaZoKqABc4UJ9vDjNTtmKSn2Axfu8sT52f5Stmt7JD4zzh",
                "6oczQNLU7bSBzVojkGsfAv3CbXagx7QLUL7Yj1Nba9iw");

        labConfig.serverUri = "https://demo.socialsirius.com";
        labConfig.credentials = "BXXwMmUlw7MTtVWhcVvbSVWbC1GopGXDuo+oY3jHkP/4jN3eTlPDwSwJATJbzwuPAAaULe6HFEP5V57H6HWNqYL4YtzWCkW2w+H7fLgrfTLaBtnD7/P6c5TDbBvGucOV".getBytes(StandardCharsets.UTF_8);
        labConfig.p2p = new P2PConnection(
                "EzJKT2Q6Cw8pwy34xPa9m2qPCSvrMmCutaq1pPGBQNCn",
                "273BEpAM8chzfMBDSZXKhRMPPoaPRWRDtdMmNoKLmJUU6jvm8Nu8caa7dEdcsvKpCTHmipieSsatR4aMb1E8hQAa",
                "342Bm3Eq9ruYfvHVtLxiBLLFj54Tq6p8Msggt7HiWxBt");

        airCompanyConfig.serverUri = "https://demo.socialsirius.com";
        airCompanyConfig.credentials = "/MYok4BSllG8scfwXVVRK8V47I1PC44mktwiJKKduf38Yb7UgIsq8n4SXVBrRwIzHMQA/6sdiKgrB20Kbw9ieHbOGlxx3UVlWNM0Xfc9Rgk85cCLSHWM2vqlNQSGwHAM+udXpuPwAkfKjiUtzyPBcA==".getBytes(StandardCharsets.UTF_8);
        airCompanyConfig.p2p = new P2PConnection(
                "BhDMxfvhc2PZ4BpGTExyWHYkJDFPhmXpaRvUoCoNJ8rL",
                "2wwakvFwBRWbFeLyDbsH6cYVve6FBH6DL133sPNN87jWYbc6rHXj7Q3dnAsbB6EuNwquucsDzSBhNcpxgyVLCCYg",
                "8VNHw79eMTZJBasgjzdwyKyCYA88ajm9gvP98KGcjaBt");

        airportConfig.serverUri = "https://demo.socialsirius.com";
        airportConfig.credentials = "/MYok4BSllG8scfwXVVRK3NATRRtESRnhUHOU3nJxxZ+gg81/srwEPNWfZ+3+6GaEHcqghOJvRoV7taA/vCd2+q2hIEpDO/yCPfMr4x2K0vC/pom1gFRJwJAKI3LpMy3".getBytes(StandardCharsets.UTF_8);
        airportConfig.p2p = new P2PConnection(
                "HBEe9KkPCK4D1zs6UBzLqWp6j2Gj88zy3miqybvYx42p",
                "23jutNJBbgn8bbX53Qr36JSeS2VtZHvY4DMqazXHq6mDEPNkuA3FkKVGAMJdjPznfizLg9nh448DXZ7e1724qk1a",
                "BNxpmTgs9B3yMURa1ta7avKuBA5wcBp5ZmXfqPFPYGAP");
    }

    public static void main(String[] args) {
        CredInfo medCredInfo;
        try (Context c = new Context(labConfig)) {
            medCredInfo = Laboratory.createMedCreds(c, LAB_DID, DKMS_NAME);
            if (medCredInfo != null) {
                System.out.println("Covid test credentials registered successfully");
            } else {
                System.out.println("Covid test credentials was not registered");
                return;
            }
        }

        CredInfo boardingPassCredInfo;
        try (Context c = new Context(airCompanyConfig)) {
            boardingPassCredInfo = AirCompany.createBoardingPassCreds(c, AIRCOMPANY_DID, DKMS_NAME);
            if (boardingPassCredInfo != null) {
                System.out.println("Boarding pass credentials registered successfully");
            } else {
                System.out.println("Boarding pass credentials was not registered");
                return;
            }
        }

        Pairwise lab2aircompany = Helpers.establishConnection(labConfig, labEntity, airCompanyConfig, aircompanyEntity);
        Pairwise aircompany2lab = Helpers.establishConnection(airCompanyConfig, aircompanyEntity, labConfig, labEntity);

        Laboratory lab = new Laboratory(labConfig, Collections.singletonList(lab2aircompany), COVID_MICROLEDGER_NAME, lab2aircompany.getMe(), medCredInfo);
        AirCompany airCompany = new AirCompany(airCompanyConfig, Collections.singletonList(aircompany2lab), COVID_MICROLEDGER_NAME, aircompany2lab.getMe(), boardingPassCredInfo);
        Airport airport = new Airport(airportConfig, medCredInfo, LAB_DID, boardingPassCredInfo, AIRCOMPANY_DID, DKMS_NAME);

        airCompany.start();
        airport.start();
        lab.start();

        Scanner in = new Scanner(System.in);
        System.out.println("Enter your Name");
        String fullName = in.nextLine();

        boolean loop = true;
        while (loop) {
            System.out.println("Enter your option:");
            System.out.println("1 - Get Covid test");
            System.out.println("2 - Get boarding pass");
            System.out.println("3 - Enter to the terminal");
            System.out.println("4 - Exit");

            int option = in.nextInt();
            switch (option) {
                case 1: {
                    System.out.println("Do you have Covid? (true/false)");
                    boolean hasCovid = in.nextBoolean();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String timestamp = df.format(new Date(System.currentTimeMillis()));
                    CovidTest testRes = new CovidTest().
                            setFullName(fullName).
                            setCovid(hasCovid).
                            setLocation("Nur-Sultan").
                            setBioLocation("Nur-Sultan").
                            setApproved("House M.D.").
                            setTimestamp(timestamp);

                    String qr = lab.issueTestResults(testRes);
                    System.out.println("Scan this QR by Sirius App for receiving the Covid test result " + qr);
                }
                break;
                case 2: {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String timestamp = df.format(new Date(System.currentTimeMillis()));
                    BoardingPass boardingPass = new BoardingPass().
                            setFullName(fullName).
                            setArrival("Nur-Sultan").
                            setDeparture("New York JFK").
                            setClass("first").
                            setDate(timestamp).
                            setFlight("KC 1234").
                            setSeat("1A");
                    String qr = airCompany.register(boardingPass);
                    System.out.println("Scan this QR by Sirius App for receiving the boarding pass " + qr);
                }
                break;
                case 3: {
                    String qr = airport.enterToTerminal();
                    System.out.println("Scan this QR by Sirius App to enter to the terminal " + qr);
                } break;
                case 4: {
                    loop = false;
                } break;
            }
        }

        airCompany.stop();
        airport.stop();
        lab.stop();
    }
}
