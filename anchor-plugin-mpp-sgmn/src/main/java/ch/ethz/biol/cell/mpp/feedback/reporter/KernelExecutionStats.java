/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.io.Serializable;
import org.anchoranalysis.core.arithmetic.RunningSum;

class KernelExecutionStats implements Serializable {

    /** */
    private static final long serialVersionUID = 6960573557987225176L;

    private RunningSum[] kernelExecutionTimeNotProposed;
    private RunningSum[] kernelExecutionTimeAccepted;
    private RunningSum[] kernelExecutionTimeRejected;

    private long totalExecutionTime;

    public KernelExecutionStats(int numKernel) {
        kernelExecutionTimeNotProposed = new RunningSum[numKernel];
        kernelExecutionTimeAccepted = new RunningSum[numKernel];
        kernelExecutionTimeRejected = new RunningSum[numKernel];

        initRunningSum(kernelExecutionTimeNotProposed);
        initRunningSum(kernelExecutionTimeAccepted);
        initRunningSum(kernelExecutionTimeRejected);
    }

    private static RunningSum sum(RunningSum[] arr) {

        RunningSum total = new RunningSum();
        for (int i = 0; i < arr.length; i++) {
            total.increment(arr[i].getSum(), arr[i].getCount());
        }
        return total;
    }

    public RunningSum sumNotProposed() {
        return sum(kernelExecutionTimeNotProposed);
    }

    public RunningSum sumAccepted() {
        return sum(kernelExecutionTimeAccepted);
    }

    public RunningSum sumRejected() {
        return sum(kernelExecutionTimeRejected);
    }

    private void initRunningSum(RunningSum[] arr) {

        for (int i = 0; i < arr.length; i++) {
            arr[i] = new RunningSum();
        }
    }

    public void incrNotProposed(int kernelID, double executionTime) {
        if (kernelID >= 0) {
            kernelExecutionTimeNotProposed[kernelID].increment(executionTime);
        }
    }

    public void incrAccepted(int kernelID, double executionTime) {
        if (kernelID >= 0) {
            kernelExecutionTimeAccepted[kernelID].increment(executionTime);
        }
    }

    public void incrRejected(int kernelID, double executionTime) {
        if (kernelID >= 0) {
            kernelExecutionTimeRejected[kernelID].increment(executionTime);
        }
    }

    public double getProposedTimeForKernelSum(int kernelID) {
        return kernelExecutionTimeAccepted[kernelID].getSum()
                + kernelExecutionTimeRejected[kernelID].getSum();
    }

    public int getProposedTimeForKernelCnt(int kernelID) {
        return kernelExecutionTimeAccepted[kernelID].getCount()
                + kernelExecutionTimeRejected[kernelID].getCount();
    }

    // Proposed + non-proposed (but can be smaller than TotalExecutionTime due to reporting)
    public double getExecutionTimeForKernelSumExclReporting(int kernelID) {
        return getProposedTimeForKernelSum(kernelID)
                + kernelExecutionTimeNotProposed[kernelID].getSum();
    }

    public int getExecutionTimeForKernelCntExclReporting(int kernelID) {
        return getProposedTimeForKernelCnt(kernelID)
                + kernelExecutionTimeNotProposed[kernelID].getCount();
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public RunningSum[] getKernelExecutionTimeNotProposed() {
        return kernelExecutionTimeNotProposed;
    }

    public RunningSum[] getKernelExecutionTimeAccepted() {
        return kernelExecutionTimeAccepted;
    }

    public RunningSum[] getKernelExecutionTimeRejected() {
        return kernelExecutionTimeRejected;
    }
}
