package com.acasframework;

import java.util.ArrayList;

/**
 * Implement this interface for receive all notification of message
 * received by ACAS framework.
 */
public interface ACASOnListModuleReceiver {
    /**
     * Use this listener for get the whole list of module
     * @param moduleList
     */
    public void onListModuleReceived(ArrayList<ACASModule> moduleList);
}