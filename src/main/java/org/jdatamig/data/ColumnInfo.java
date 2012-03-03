package org.jdatamig.data;

/**
 *
 * @author Babji, Chetty
 */
public class ColumnInfo {
    private boolean identity;
    private boolean nullable;

    /**
     * @return the identity
     */
    public boolean isIdentity() {
        return identity;
    }

    /**
     * @param identity the identity to set
     */
    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    /**
     * @return the nullable
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * @param nullable the nullable to set
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}
