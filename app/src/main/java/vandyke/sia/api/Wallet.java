package vandyke.sia.api;

import vandyke.sia.SiaRequest;

import java.math.BigDecimal;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;

public class Wallet {

    public static void wallet(SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, "/wallet", callback).send();
    }

    public static void unlock(String password, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/unlock", callback)
                .addParam("encryptionpassword", password)
                .send();
    }

    public static void lock(SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/lock", callback).send();
    }

    public static void newAddress(SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, "/wallet/address", callback).send();
    }

    public static void addresses(SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, "/wallet/addresses", callback).send();
    }

    /**
     * note that currently does not let you choose a dictionary, so the server will use english by default
     */
    public static void init(String password, boolean force, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/init", callback)
                .addParam("encryptionpassword", password)
                .addParam("force", force ? "true" : "false")
                .send();
    }

    public static void initSeed(String password, boolean force, String seed, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/init", callback)
                .addParam("encryptionpassword", password)
                .addParam("force", force ? "true" : "false")
                .addParam("seed", seed)
                .send();
    }

    public static void seed(String password, String dictionary, String seed, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/seed", callback)
                .addParam("encryptionpassword", password)
                .addParam("dictionary", dictionary)
                .addParam("seed", seed)
                .send();
    }

    public static void seeds(String dictionary, SiaRequest.VolleyCallback callback) {
        new SiaRequest(GET, String.format("/wallet/seeds?dictionary=%s", dictionary), callback)
                .send();
    }


    public static void changePassword(String currentPassword, String newPassword, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/changepassword", callback)
                .addParam("encryptionpassword", currentPassword)
                .addParam("newpassword", newPassword)
                .send();
    }

    public static void sendSiacoins(String amount, String recipient, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/siacoins", callback)
                .addParam("amount", amount)
                .addParam("destination", recipient)
                .send();
    }

    public static void sendSiafunds(String amount, String recipient, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/siafunds", callback)
                .addParam("amount", amount)
                .addParam("destination", recipient)
                .send();
    }

    public static void sweepSeed(String dictionary, String seed, SiaRequest.VolleyCallback callback) {
        new SiaRequest(POST, "/wallet/sweep/seed", callback)
                .addParam("dictionary", dictionary)
                .addParam("seed", seed)
                .send();
    }

    public static void transactions(SiaRequest.VolleyCallback callback) {
        // TODO: maybe use actual value instead of really big literal lol
        new SiaRequest(GET, String.format("/wallet/transactions?startheight=%s&endheight=%s", "0", "1000000000"), callback)
                .send();
    }

    public static BigDecimal hastingsToSC(String hastings) {
        return new BigDecimal(hastings).divide(new BigDecimal("1000000000000000000000000"));
    }

    public static BigDecimal hastingsToSC(BigDecimal hastings) {
        return hastings.divide(new BigDecimal("1000000000000000000000000"));
    }

    public static BigDecimal scToHastings(String sc) {
        return new BigDecimal(sc).multiply(new BigDecimal("1000000000000000000000000"));
    }

    public static BigDecimal scToHastings(BigDecimal sc) {
        return sc.multiply(new BigDecimal("1000000000000000000000000"));
    }
}
