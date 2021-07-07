package examples.covid;

import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.CloudHub;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseParticipant {

    boolean loop = false;
    CloudHub.Config config;
    List<Pairwise> pairwises;
    Thread thread;
    String covidMicroledgerName;
    Pairwise.Me me;
    List<String> covidMicroledgerParticipants;

    public BaseParticipant(CloudHub.Config config, List<Pairwise> pairwises, String covidMicroledgerName, Pairwise.Me me) {
        this.config = config;
        this.pairwises = pairwises;
        this.covidMicroledgerName = covidMicroledgerName;
        this.me = me;
        covidMicroledgerParticipants = new ArrayList<>();
        if (me != null) {
            covidMicroledgerParticipants.add(me.getDid());
        }
        if (pairwises != null) {
            for (Pairwise pw : pairwises) {
                covidMicroledgerParticipants.add(pw.getTheir().getDid());
            }
        }
    }

    public void start() {
        loop = true;
        if (thread == null) {
            thread = new Thread(() -> routine());
            thread.start();
        }
    }

    public void stop() {
        loop = false;
        thread.interrupt();
        thread = null;
    }

    protected abstract void routine();
}
