package com.acasframework;


import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;

public class ACASListModuleReceiver {
	
    static final String TAG = ACASListModuleReceiver.class.getSimpleName();

    private ArrayList<Pair<ACASOnListModuleReceiver, String>> mListeners = new ArrayList<Pair<ACASOnListModuleReceiver, String>>();

    /**
     * Add an listener for received modules
     *
     * @param listener
     */
    void setOnListModuleReceivedListener(ACASOnListModuleReceiver listener) {
        mListeners.add(new Pair<ACASOnListModuleReceiver, String>(listener, null));
        deliveryModules();
    }

    /**
     * Add an listener for received modules with specific entryPoint
     *
     * @param listener
     * @param entryPoint
     */
    void setOnListModuleReceivedListener(ACASOnListModuleReceiver listener, String entryPoint) {
        mListeners.add(new Pair<ACASOnListModuleReceiver, String>(listener, entryPoint));
        deliveryModules();
    }

    /**
     * Delete an listener from list
     *
     * @param listener
     */
    void removeOnListModuleReceiverListener(ACASOnListModuleReceiver listener) {
      //  mListeners.remove(listener);
    }

    /**
     * Delivery one message to all alive listener
     */
    void deliveryModules() {
        int nbrDelivery = 0;
        Iterator<Pair<ACASOnListModuleReceiver, String>> itr = mListeners.iterator();
        while (itr.hasNext()) {
            final Pair<ACASOnListModuleReceiver, String> elem = itr.next();
            if (elem == null) {
                if (ACAS.DEBUG_MODE) {
                    Log.d(TAG, "Listener is null, remove it.");
                }
                itr.remove();
            } else if (ACAS.mModuleList != null && ACAS.mModuleList.size()>0) {
                if (elem.second == null) {
                    nbrDelivery++;
                    elem.first.onListModuleReceived(ACAS.mModuleList);
                } else {
                    nbrDelivery++;
                    ArrayList<ACASModule> filtered = new ArrayList<ACASModule>();
                    for (Iterator<ACASModule> it=ACAS.mModuleList.iterator(); it.hasNext();) {
                        if (it.next().mEntryPoint.equals(elem.second)) {
                            filtered.add(it.next());
                        }
                    }
                    elem.first.onListModuleReceived(filtered);
                }
            }
        }

        if (nbrDelivery > 0) {
            if (ACAS.DEBUG_MODE) {
                Log.i(TAG, "Module list has been delivered");
            }
        } else if (ACAS.DEBUG_MODE) {
            Log.w(TAG, "Module list not delivered...");
        }
    }
}
