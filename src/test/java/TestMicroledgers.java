import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.microledgers.Transaction;
import helpers.ConfTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestMicroledgers {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testInitLedger() {
        Agent agent4 = confTest.getAgent("agent4");
        String ledgerName = confTest.ledgerName();
        agent4.open();

        try {
            List<Transaction> genesisTxns = Arrays.asList(
                    new Transaction(new JSONObject().
                            put("reqId", 1).
                            put("identifier", "5rArie7XKukPCaEwq5XGQJnM9Fc5aZE3M9HAPVfMU2xC").
                            put("op", "op1")),
                    new Transaction(new JSONObject().
                            put("reqId", 2).
                            put("identifier", "2btLJAAb1S3x6hZYdVyAePjqtQYi2ZBSRGy4569RZu8h").
                            put("op", "op2")),
                    new Transaction(new JSONObject().
                            put("reqId", 3).
                            put("identifier", "CECeGXDi6EHuhpwz19uyjjEnsRGNXodFYqCRgdLmLRkt").
                            put("op", "op3"))
                    );
            //agent4.getMicroledgers()
        } finally {
            agent4.close();
        }
    }
}
