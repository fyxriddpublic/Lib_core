package lib.core.api.adaptor;

import lib.core.api.inter.Transaction;

public class TransactionAdaptor extends Transaction{
    public TransactionAdaptor(String name, long last) {
        super(name, last);
    }

    public TransactionAdaptor(String name, long last, int tipInterval) {
        super(name, last, tipInterval);
    }

    @Override
    public void onTip() {
    }

    @Override
    public void onOperate(String content) {
    }

    @Override
    public void onTimeOut() {
    }

    @Override
    public void onCancel() {
    }
}
