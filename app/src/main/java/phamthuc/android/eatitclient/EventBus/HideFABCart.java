package phamthuc.android.eatitclient.EventBus;

public class HideFABCart {
    private boolean hidden;

    public HideFABCart(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
