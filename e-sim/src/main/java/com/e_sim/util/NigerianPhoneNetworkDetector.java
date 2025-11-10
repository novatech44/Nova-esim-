package com.e_sim.util;

import lombok.experimental.UtilityClass;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@UtilityClass
public class NigerianPhoneNetworkDetector {

    private static final Map<String, Pattern> NETWORK_PATTERNS = new HashMap<>();

    static {
        // MTN: 0803, 0806, 0703, 0706, 0813, 0816, 0810, 0814, 0903, 0906, 0913, 0916
        NETWORK_PATTERNS.put("MTN", Pattern.compile("^(080[36]|070[36]|081[0346]|090[36]|091[36])\\d+$"));

        // Airtel: 0802, 0808, 0708, 0701, 0812, 0901, 0902, 0904, 0907, 0912
        NETWORK_PATTERNS.put("Airtel", Pattern.compile("^(080[28]|070[18]|0812|090[1247]|0912)\\d+$"));

        // Glo: 0805, 0807, 0705, 0811, 0815, 0905, 0915
        NETWORK_PATTERNS.put("Glo", Pattern.compile("^(080[57]|0705|081[15]|0905|0915)\\d+$"));

        // 9mobile: 0809, 0817, 0818, 0908, 0909
        NETWORK_PATTERNS.put("9mobile", Pattern.compile("^(0809|081[78]|090[89])\\d+$"));

        // MTEL: 0804
        NETWORK_PATTERNS.put("MTEL", Pattern.compile("^0804\\d+$"));
    }

    public static String detectNetwork(String phoneNumber) {
        // Check against each network pattern
        for (Map.Entry<String, Pattern> entry : NETWORK_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(phoneNumber).matches()) {
                return entry.getKey();
            }
        }

        throw new IllegalArgumentException("Invalid phone number");
    }
}