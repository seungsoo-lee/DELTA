package org.deltaproject.odlagent.tests;

/**
 * Created by seungsoo on 09/07/2017.
 */
class CPUex extends Thread {
    int result = 1;

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (true) {
            result = result ^ 2;
        }
    }
}
