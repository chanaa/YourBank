package com.hps.esecure.yourbank.common;

/**
 * Created by namri on 20/12/2017.
 */

public class DummyOobTrans {

    private String	acsTransId;
    private String	panMasked;
    private String	amount;
    private boolean	verified;

    public String getAcsTransId() {
        return acsTransId;
    }

    public void setAcsTransId(String acsTransId) {
        this.acsTransId = acsTransId;
    }

    public String getPanMasked() {
        return panMasked;
    }

    public void setPanMasked(String panMasked) {
        this.panMasked = panMasked;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((acsTransId == null) ? 0 : acsTransId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DummyOobTrans other = (DummyOobTrans) obj;
        if (acsTransId == null) {
            if (other.acsTransId != null)
                return false;
        } else if (!acsTransId.equals(other.acsTransId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return panMasked;
    }
}
