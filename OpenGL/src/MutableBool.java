public class MutableBool {
    private boolean value;

    public MutableBool(boolean v) {
        this.value = v;
    }
    
    public synchronized void setMutableBool(boolean v) {
        value = v;
    }

    public synchronized boolean getMutableBool() {
        return value;
    }
}
